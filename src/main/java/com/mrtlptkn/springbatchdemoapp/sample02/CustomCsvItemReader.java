package com.mrtlptkn.springbatchdemoapp.sample02;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@StepScope
public class CustomCsvItemReader extends FlatFileItemReader<RawOrder> {


    @Value("${batch.input-dir}")
    private String inputDir;

    @Value("#{jobParameters['runDate']}")
    private String runDate;

    // Bean özellikleri (@Value alanları) enjekte edildikten sonra reader'ı kendimiz yapılandırıyoruz.
    // Böylece FlatFileItemReader'ın open/read/close akışı normal şekilde çalışır.
    @Override
    public void afterPropertiesSet() throws Exception {
        setName("csvItemReader");
        setResource(new FileSystemResource(Path.of(inputDir, "orders-" + runDate + ".csv")));
        setLinesToSkip(1); // başlık satırını atlar
        setLineMapper(createLineMapper());
        super.afterPropertiesSet();
    }

    private DefaultLineMapper<RawOrder> createLineMapper() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("order_id", "customer", "amount", "order_date");

        DefaultLineMapper<RawOrder> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fs -> new RawOrder(
                fs.readString("order_id"),
                fs.readString("customer"),
                fs.readString("amount"),
                fs.readString("order_date")));
        return lineMapper;
    }

    @Override
    public RawOrder read() throws Exception {
        return super.read();
    }
}
