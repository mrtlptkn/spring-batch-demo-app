package com.mrtlptkn.springbatchdemoapp.sample01;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DailyPrepareJobConfig {

    // JobRepository ve PlatformTransactionManager Beanleri otomatik olarak hazır sadece bu beanlere erişim yapıyoruz.



//    @Bean
//    public Tasklet taskletDemo () {
//        return new Tasklet() {
//            @Override
//            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//                return RepeatStatus.FINISHED;
//            }
//        };
//    }
//
//    @Bean
//    public Step stepDemo(JobRepository jobRepository,PlatformTransactionManager platformTransactionManager) {
//        return  new StepBuilder("stepDemo",jobRepository)
//                .tasklet(taskletDemo(),platformTransactionManager)
//                .build();
//    }

    @Bean
    public Step prepareDailyTaskStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            DailyPrepareTasklet dailyPrepareTasklet){

        return  new StepBuilder("prepareDailyTaskStep",jobRepository)
                .tasklet(dailyPrepareTasklet,platformTransactionManager)
                .build();
    }


    // Spring Context çözümlemesi için bean isimlerine dikkat edelimç
    @Bean
    public Job prepareDailyTaskJob(JobRepository jobRepository,Step prepareDailyTaskStep){
        return new JobBuilder("prepareDailyTaskJob",jobRepository)
                .start(prepareDailyTaskStep).build();
    }

}
