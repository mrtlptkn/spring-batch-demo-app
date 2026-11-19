package com.mrtlptkn.springbatchdemoapp.sample03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestClient;

@Configuration
public class ProductResponseApiClientConfig {

    private final  static Logger log = LoggerFactory.getLogger(ProductResponseApiClientConfig.class);

    @Bean
    @StepScope
    public ODataPagedItemReader itemReader(RestClient.Builder restClientBuilder) {
        return new ODataPagedItemReader(restClientBuilder.build(),"https://services.odata.org/northwind/northwind.svc/Products",10);
    }

    @Bean
    @StepScope
    public ItemWriter<ProductResponse> productItemWriter() {
        return items -> {

            for (ProductResponse item : items) {
                log.info("Product: {}", item.productName());
            }

            log.info("Yazılacak ürün sayısı: {}", items.size());
        };
    }

    @Bean
    public Step productStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, ODataPagedItemReader itemReader, ItemWriter<ProductResponse> itemWriter){
        return new StepBuilder("productStep",jobRepository)
                .<ProductResponse,ProductResponse>chunk(2,platformTransactionManager)
                .reader(itemReader)
                .writer(itemWriter)
                .build();
    }

    @Bean
    public Job productJob(JobRepository jobRepository, Step productStep){
        return new JobBuilder("productJob",jobRepository)
                .start(productStep)
                .build();
    }

}
