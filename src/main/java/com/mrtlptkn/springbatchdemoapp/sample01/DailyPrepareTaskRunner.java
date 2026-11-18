package com.mrtlptkn.springbatchdemoapp.sample01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
// mvn spring-boot:run -Dspring-boot.run.arguments="--batch.run=daily-prepare-task"
// eğer projeyi bu parametre ile run edersem bu servis @ConditionalOnProperty üzerinden eşleşip run methodunu çalıştıracak
@Component
@ConditionalOnProperty(name = "batch.run", havingValue = "daily-prepare-task")
public class DailyPrepareTaskRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher; // Job çalıştırmak için kullandılan bir application nesnesi
    private final Job prepareDailyTaskJob;
    private static  final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");
    private static  final Logger LOGGER = LoggerFactory.getLogger(DailyPrepareTaskRunner.class);

    public DailyPrepareTaskRunner(JobLauncher jobLauncher,Job prepareDailyTaskJob) {
        this.jobLauncher = jobLauncher;
        this.prepareDailyTaskJob = prepareDailyTaskJob;
    }

    @Override
    public void run(String... args) throws Exception {

        // Job Parameters bugün tarihli job çalıştıralım ki yarında yarın tarihli jon çalışsın.
        // Job eğer completed olursa aynı günde tekrar çalışmaz.
        String today =  LocalDate.now(ISTANBUL).toString();
        JobParameters params = new JobParametersBuilder()
                .addString("runDate",today)
                .toJobParameters();

        JobExecution jobExecution =  jobLauncher.run(prepareDailyTaskJob,params); // JobLauncher ile job çalıştırılır. Job ve parametreler verilir.
        LOGGER.warn("Job çalıştırma sonucu: status={}, runDate={}", jobExecution.getStatus(), today);

    }
}
