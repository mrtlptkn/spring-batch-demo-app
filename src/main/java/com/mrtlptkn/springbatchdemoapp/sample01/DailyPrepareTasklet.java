package com.mrtlptkn.springbatchdemoapp.sample01;


import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@StepScope // StepScope ile her bir step içerisinde stepdeki bilgilere erişim ve yeniden step bazlı oturum yönetimi
public class DailyPrepareTasklet implements Tasklet {

    // Step akışını yönettiğimiz yer
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        throw  new IllegalStateException("DailyPrepareTasklet çalıştırıldı. Bu taskletin amacı job parametrelerini hazırlamak ve jobu başlatmak. Bu taskletin kendisi bir iş yapmaz.");

        // Logic -> Günlük çalışma dosyamız var mı ?
        // contibution yapılan step ilgili işlemlere ait sayaçları güncellemek veya okumak için kullanılır.
        // Tasklet işlemleri manuel yönetir.
        // chunkContext -> ilgili StepContext üzerinden bilgi okumamız gerekn durumlarda eriştiğimiz
        // StepContext -> Step içerisinde yer alan bilgiler, job parametreleri, step execution context gibi bilgilere erişim sağlar.

        //return RepeatStatus.FINISHED;
    }
}
