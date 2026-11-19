package com.mrtlptkn.springbatchdemoapp.sample03;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
@RequestMapping("api/v1/products")
public class ProductJobController {

    private final Job productJob;
    private final JobLauncher jobLauncher;

    public ProductJobController(Job productJob, JobLauncher jobLauncher) {
        this.productJob = productJob;
        this.jobLauncher = jobLauncher;
    }

    @PostMapping
    public ProductEntityResponse start() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        var params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution result =  this.jobLauncher.run(productJob, params);

        long readCount = result.getStepExecutions().stream().mapToLong(stepExecution -> stepExecution.getReadCount()).sum();

        return new ProductEntityResponse(result.getStatus().name(), readCount);

    }

    public record  ProductEntityResponse(String status,Long readCount){}

}
