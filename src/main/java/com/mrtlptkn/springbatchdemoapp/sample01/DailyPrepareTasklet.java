package com.mrtlptkn.springbatchdemoapp.sample01;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@StepScope // StepScope ile her bir step içerisinde stepdeki bilgilere erişim ve yeniden step bazlı oturum yönetimi
// Günlük dosyamız ilgili klasör altında var mı kontrolü yapsın
public class DailyPrepareTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(DailyPrepareTasklet.class);

    @Value("${batch.input-dir}") // $application yml dosyasındaki değerleri $ ile okuruz
    private String inputDir;

    @Value("#{jobParameters['runDate']}") // step veya job üzerinden bir değer okuyacak isek # ifadesi kullanırız
    private String runDate;

    // Step akışını yönettiğimiz yer
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        Path expectedInput = Path.of(inputDir, "orders-" + runDate + ".csv");

        if(Files.exists(expectedInput)){
            log.info("Girdi dosyası doğrulandı: {}", expectedInput);
            contribution.incrementReadCount(); // dosya okundu sayacını bir artırır.
            contribution.incrementWriteCount(7); // örnek sayaç değerlerini artırdık.
            return RepeatStatus.FINISHED;
        }
        else{
            throw new IllegalStateException("Girdi dosyası bulunamadı: " + expectedInput.toAbsolutePath());
        }

        // Logic -> Günlük çalışma dosyamız var mı ?
        // contibution yapılan step ilgili işlemlere ait sayaçları güncellemek veya okumak için kullanılır.
        // Tasklet işlemleri manuel yönetir.
        // chunkContext -> ilgili StepContext üzerinden bilgi okumamız gerekn durumlarda eriştiğimiz
        // StepContext -> Step içerisinde yer alan bilgiler, job parametreleri, step execution context gibi bilgilere erişim sağlar.

    }
}
