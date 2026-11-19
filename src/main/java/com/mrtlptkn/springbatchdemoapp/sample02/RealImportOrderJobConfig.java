package com.mrtlptkn.springbatchdemoapp.sample02;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
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
import java.time.LocalDate;

@Configuration
public class RealImportOrderJobConfig {

    // Burada FlatFileItemReader ile csv okuyup JdbcBatchItemWriter ile db yazdıracağız

    private final OrderItemMappingProcessor orderItemProcessor;
//    private final CustomCsvItemReader customCsvItemReader;
    private final SkippedOrderListener skippedOrderListener;
    private final  ImportJobStepExecutionListener importJobStepExecutionListener;

    public RealImportOrderJobConfig(OrderItemMappingProcessor orderItemProcessor, CustomCsvItemReader customCsvItemReader, SkippedOrderListener skippedOrderListener, ImportJobStepExecutionListener importJobStepExecutionListener) {
        this.orderItemProcessor = orderItemProcessor;
//        this.customCsvItemReader = customCsvItemReader;
        this.skippedOrderListener = skippedOrderListener;
        this.importJobStepExecutionListener = importJobStepExecutionListener;
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
    public JdbcBatchItemWriter<Order> jdbcBatchItemWriter(DataSource dataSource, @Value("#{jobParameters['runDate']}") String runDate){
        return new JdbcBatchItemWriterBuilder<Order>()
                .dataSource(dataSource)
                .sql("INSERT INTO orders (order_id, customer, amount, order_date, runDate) " +
                        "VALUES (:orderId, :customer, :amount, :orderDate, :runDate)")
                .itemSqlParameterSourceProvider(o -> new MapSqlParameterSource()
                        .addValue("orderId", o.orderId())
                        .addValue("customer", o.customer())
                        .addValue("amount", o.amount())
                        .addValue("orderDate", o.orderDate())
                        .addValue("runDate", LocalDate.parse(runDate)))
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
                .listener(skippedOrderListener)
                .listener(importJobStepExecutionListener)
                .build();
    }


    // Archieve Step
    // JdbcCursorItemReader -> Tek seferde SQL den filtrelenmiş bir veri okuyacak isek bu durumda kullanabiliriz.
    // Okunan tüm veriler tek seferde okunup memory alınır. Dezavantajı Memory Şişer
    // JdbcPagedItemReader var Milyonluk kayıtları okurken server side paging yapar
    @Bean
    @StepScope
    public JdbcCursorItemReader<Order> jdbcCursorItemReader(DataSource dataSource,
                                                            @Value("#{jobParameters['runDate']}") String runDate) {

        LocalDate importDate = LocalDate.parse(runDate);

        return new JdbcCursorItemReaderBuilder<Order>()
                .name("archieveReader")
                .dataSource(dataSource).sql("SELECT * FROM orders WHERE runDate = ? ORDER BY order_id")
                .preparedStatementSetter((ps) -> ps.setObject(1, importDate))
                .rowMapper((rs, rowNum) -> new Order(
                        rs.getLong("order_id"),
                        rs.getString("customer"),
                        rs.getBigDecimal("amount"),
                        rs.getObject("order_date",LocalDate.class),
                        rs.getObject("runDate",LocalDate.class)))
                .build();

    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Order> archiveWriter(@Value("#{jobParameters['runDate']}") String runDate, @Value("${batch.archive-dir}") String archieveDir){
        return  new FlatFileItemWriterBuilder<Order>()
                .name("archiveWriter")
                .resource(new FileSystemResource(Path.of(archieveDir, "orders-"+runDate+".csv")))
                .delimited()
                .delimiter(",")
                .names("orderId","customer","amount","orderDate","runDate")
                .build();
    }


    @Bean
    public Step archiveStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            JdbcCursorItemReader<Order> jdbcCursorItemReader,
            FlatFileItemWriter<Order> archieveWriter
    ){
        return  new StepBuilder("archiveStep",jobRepository)
                .<Order,Order>chunk(10,platformTransactionManager)
                .reader(jdbcCursorItemReader)
                .writer(archieveWriter)
                .build();
    }


    // ImportStep eğer başarılı bir şekilde tamamlanırsa biz order tablosundan bugünün import edilen verilerini
    // arşiv olarak csv dosyasında saklayabiliriz.
    // COMPLETED Step.getExecution.getStatus() üzerinden gelir.

    @Bean
    public Job importDataFromCsvOrderJob(JobRepository jobRepository, Step importOrderDataFromCsvStep,Step archiveStep){
        return  new JobBuilder("importDataFromCsvOrderJob",jobRepository)
                //.preventRestart() // Restart etmeyi engelle
//                .start(importOrderDataFromCsvStep)
//                .next(archiveStep) // hata dahi olsa tetiklenir.
                .start(importOrderDataFromCsvStep)
                .on("COMPLETED")
                .to(archiveStep)
                .end()
                .build();
    }


}
