package com.mrtlptkn.springbatchdemoapp.sample02;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.BatchUpdateException;

@Configuration
public class RealImportOrderJobConfig {

    // Burada FlatFileItemReader ile csv okuyup JdbcBatchItemWriter ile db yazdıracağız

    private final OrderItemMappingProcessor orderItemProcessor;
//    private final CustomCsvItemReader customCsvItemReader;

    public RealImportOrderJobConfig(OrderItemMappingProcessor orderItemProcessor, CustomCsvItemReader customCsvItemReader) {
        this.orderItemProcessor = orderItemProcessor;
//        this.customCsvItemReader = customCsvItemReader;
    }


    @Bean
    @StepScope
    public FlatFileItemReader<RawOrder> csvItemReader(
            @Value("${batch.input-dir}") String inputDir,
            @Value("#{jobParameters['runDate']}") String runDate){
        return  new FlatFileItemReaderBuilder<RawOrder>()
                .name("csvItemReader")
                .resource(new FileSystemResource(Path.of(inputDir,"orders-"+runDate+".csv")))
                .linesToSkip(1) // başlık alanını atlar
                .delimited()
                .names("order_id","customer","amount","order_date")
                .fieldSetMapper(fs -> new RawOrder( // Okunup aktarılacak nesneye maple
                        fs.readString("order_id"),
                        fs.readString("customer"),
                        fs.readString("amount"),
                        fs.readString("order_date")))
                .build();

    }


    @Bean
    @StepScope
    public JdbcBatchItemWriter<Order> jdbcBatchItemWriter(DataSource dataSource){
        return new JdbcBatchItemWriterBuilder<Order>()
                .dataSource(dataSource)
                .sql("INSERT INTO orders (order_id, customer, amount, order_date) " +
                        "VALUES (:orderId, :customer, :amount, :orderDate)")
                .itemSqlParameterSourceProvider(o -> new MapSqlParameterSource()
                        .addValue("orderId", o.orderId())
                        .addValue("customer", o.customer())
                        .addValue("amount", o.amount())
                        .addValue("orderDate", o.orderDate()))
                .build();
    }


    @Bean
    public Step importOrderDataFromCsvStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            FlatFileItemReader<RawOrder> csvItemReader,
            JdbcBatchItemWriter<Order> jdbcBatchItemWriter
    ){
        return  new StepBuilder("importOrderDataFromCsvStep",jobRepository)
                .<RawOrder,Order>chunk(2,platformTransactionManager)
                .reader(csvItemReader)
                .processor(orderItemProcessor)
                .writer(jdbcBatchItemWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class) // FlatFile Okuma hatası
                .skip(InvalidOrderException.class) // Bide Process işleminde eğer exception fırlatılırsa o satırı atlat
                .skip(BatchUpdateException.class) // JdbcBatchItemWriter'da hata olursa o satırı atlat
                .skip(DuplicateKeyException.class)
                .skipLimit(Integer.MAX_VALUE) // Skip Limit vermezsek default 0 olarak gördü hata toleransı kalmadı.
                .listener(new SkippedOrderListener())
                .build();
    }


    @Bean
    public Job importDataFromCsvOrderJob(JobRepository jobRepository, Step importOrderDataFromCsvStep){
        return  new JobBuilder("importDataFromCsvOrderJob",jobRepository)
                //.preventRestart() // Restart etmeyi engelle
                .start(importOrderDataFromCsvStep)
                .build();
    }


}
