package com.mrtlptkn.springbatchdemoapp.sample04;

import com.mrtlptkn.springbatchdemoapp.sample03.CriticalStockProductsDecider;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

// Amaç hem importOrder hemde NonCriticalStockProduct Steplerini tek bir job altında paralel olarak işlemek
// Paralel işlemlerde dikkat edilmesi gereken noktalardan biri split edilecek akışların aynı veri kümesi üzerinde
// çalışmaması
// Eğer aynı veri kümesi üzerinden farklı threadler çalışmaya kalkarsa, race condition durumları meydana gelebilir.


@Configuration
public class SplitJobFlowConfig {

    private final CriticalStockProductsDecider decider;

    public SplitJobFlowConfig(CriticalStockProductsDecider decider) {
        this.decider = decider;
    }

    // Split akışı için bu bean kullanılacak

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Thread pooldan bana 2 tane bu iş thread tahsis et
        executor.setMaxPoolSize(2);
        executor.initialize();
        return executor;
    }

    // DaiyPrepare -> Import -> Archieve Akışı

    @Bean
    public SimpleFlow importJobFlow(Step prepareDailyTaskStep, Step importOrderDataFromCsvStep,Step archiveStep){
        return  new FlowBuilder<SimpleFlow>("importJobFlow")
                .start(prepareDailyTaskStep)
                .next(importOrderDataFromCsvStep)
                .on("COMPLETED")
                .to(archiveStep)
                .end();
    }

    // NonCriticalStockProduct Flow
    @Bean
    public SimpleFlow importNonCriticalProductFromApi(Step productStep,Step criticalStockProductsStep, Step productReportStep) {
        return new FlowBuilder<SimpleFlow>("importNonCriticalProductFromApi")
                .start(productStep)
                .next(decider)
                .on("HIGH_CRITICAL_STOCK")
                .to(criticalStockProductsStep)
                .from(decider)
                .on("LOW_CRITICAL_STOCK")
                .to(productReportStep)
                .end();
    }

    @Bean
    public Tasklet startTasklet() {
        return (contribution, chunkContext) -> {
            StepExecution stepExecution = contribution.getStepExecution();
            System.out.println("Starting the split job flow...");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step startTaskletStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,Tasklet startTasklet) {
        return new StepBuilder("startTaskletStep", jobRepository)
                .tasklet(startTasklet, platformTransactionManager)
                .build();
    }

    // Şağıdaki 2 farklı işin birbirinde Thread olarak ayrılması için bir init step ihtiyacımız var bu sebeple
    // ayrıma işlemi yaptık
    // add(importNonCriticalProductFromApi,importJobFlow) -> farklı threadlere ayırmayı sağlar
    //  .split(threadPoolTaskExecutor) -> verilen ayara göre split eden bean

    @Bean
    public Job splitJob(JobRepository jobRepository, ThreadPoolTaskExecutor threadPoolTaskExecutor, SimpleFlow importJobFlow, SimpleFlow importNonCriticalProductFromApi,Step startTaskletStep) {
        return new JobBuilder("splitJob",jobRepository)
                .start(startTaskletStep)
                .split(threadPoolTaskExecutor)
                .add(importNonCriticalProductFromApi,importJobFlow)
                .end()
                .build();
    }



}
