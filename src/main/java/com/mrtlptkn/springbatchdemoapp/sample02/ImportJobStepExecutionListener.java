package com.mrtlptkn.springbatchdemoapp.sample02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class ImportJobStepExecutionListener implements StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(ImportJobStepExecutionListener.class);


    private final JdbcTemplate jdbcTemplate;

    public ImportJobStepExecutionListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        // arşivlenmesi için işin bitmesi lazım.
        if(stepExecution.getStatus().equals("COMPLETED")) {
            logger.info("Step completed successfully. Step name: {}", stepExecution.getStepName());
            // write archieve csv

           // jdbcTemplate.execute("Select * from orders"); Order nesnesine maple -> order da csv dosyasında toCsv ile maple Path üzerinden kaydet.

            // Neden böyle yapmadık ? Zaten hazır FlatFileItemWriter var Hazır JdbcCursorItemReader okuma yapıp sorgulamadan direk olarak veriyi mapleyip flatfile gönderebiliriz. Job üzerinden step başarılı ise başka bir step ile süreci devam ettir var.
        }

        return StepExecutionListener.super.afterStep(stepExecution);
    }
}
