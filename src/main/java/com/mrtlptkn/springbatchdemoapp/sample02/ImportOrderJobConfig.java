package com.mrtlptkn.springbatchdemoapp.sample02;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ImportOrderJobConfig {

    private final OrderItemReader orderItemReader;
    private final OrderItemMappingProcessor orderItemProcessor;
    private final OrderItemWriter orderItemWriter;


    public ImportOrderJobConfig(OrderItemReader orderItemReader, OrderItemMappingProcessor orderItemProcessor, OrderItemWriter orderItemWriter) {
        this.orderItemReader = orderItemReader;
        this.orderItemProcessor = orderItemProcessor;
        this.orderItemWriter = orderItemWriter;
    }

    // JdbcBatchUpdateException
    @Bean
    public Step importOrderStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return  new StepBuilder("importOrderStep",jobRepository)
                .<RawOrder,Order>chunk(2,platformTransactionManager)
                .reader(orderItemReader)
                .processor(orderItemProcessor)
                .writer(orderItemWriter)
                .build();
    }

    // Sequential Flow ->

    @Bean("importOrderJob")
    public Job importOrderJob(JobRepository jobRepository,Step importOrderStep, Step prepareDailyTaskStep){
        return  new JobBuilder("importOrderJob",jobRepository)
                //.preventRestart() // Restart etmeyi engelle
                .start(prepareDailyTaskStep)
                .next(importOrderStep)
                .build();
    }

}
