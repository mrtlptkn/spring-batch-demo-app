package com.mrtlptkn.springbatchdemoapp.sample02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// JobExecutionAlreadyRunningException : Job çalışsırken tekrar gönderilise bu hatayı verir
// JobInstanceAlreadyCompleteException : Daha önceden completed bir job çalışırsa hata verir.
// JobParametersInvalidException : Parametre hatası varsa
// JobRestartException : Restrat edilebilir bir job değilse.

@RestController
@RequestMapping("api/v1/orders")
public class OrderJobController {

    private final JobLauncher jobLauncher;
    private final Job importOrderJob;
    private final Job importDataFromCsvOrderJob;

    private  static final Logger LOGGER = LoggerFactory.getLogger(OrderJobController.class);
    public OrderJobController(
            JobLauncher jobLauncher,
            Job importOrderJob,
            Job importDataFromCsvOrderJob
    ) {
        this.jobLauncher = jobLauncher;
        this.importOrderJob = importOrderJob;
        this.importDataFromCsvOrderJob = importDataFromCsvOrderJob;
    }

    // http:/localhost:8080/api/v1/orders?runDate=2026-11-18 HTTP GET
    //@GetMapping
    @PostMapping
    public ResponseEntity<String> startOrderJob(@RequestParam("runDate") String runDate) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        // Job başlatma işlemleri burada yapılabilir
        var params = new JobParametersBuilder()
                .addString("runDate", runDate).toJobParameters();

      JobExecution execution =  jobLauncher.run(importOrderJob, params);
        LOGGER.warn("Order job started with status: {}, runDate={}", execution.getStatus(), runDate);


        return ResponseEntity.ok("Order job started successfully.");
    }


    // Veritabanına yazdığımız versiyon.
    @PostMapping("/real")
    public ResponseEntity<String> startRealImportOrderJob(@RequestParam("runDate") String runDate) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        // Job başlatma işlemleri burada yapılabilir
        var params = new JobParametersBuilder()
                .addString("runDate", runDate).toJobParameters();

        JobExecution execution =  jobLauncher.run(importDataFromCsvOrderJob, params);
        LOGGER.warn("Order job started with status: {}, runDate={}", execution.getStatus(), runDate);


        return ResponseEntity.ok("ImportDataFromCsvOrderJob job started successfully.");
    }

}
