package com.mrtlptkn.springbatchdemoapp.sample02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

// Hata tolere çalışıyorsa -> Dosya üzerinde skip işlemleri yapıyorsa SkipListener ile atlatılan kayıtları kesinlikle loglamalıyız yada bir yerde bir dosyada saklamalıyız.
@Component
@StepScope
public class SkippedOrderListener implements SkipListener<RawOrder,Order> {

    @Value("${batch.output-dir}")
    private String outputDir;
    @Value("#{jobParameters['runDate']}")
    private String runDate;

    private static final Logger LOGGER = LoggerFactory.getLogger(SkippedOrderListener.class);
    private static final String  FILE_HEADER = "raw_data,reason";  // csv dosyasının başlığı

    @Override
    public void onSkipInRead(Throwable t) {
        LOGGER.warn("Okumada atlanan kayıt: {}", t.getMessage());

        String rawData = (t instanceof FlatFileParseException ex) ? ex.getInput() : "(bilinmiyor)";
        write(rawData, t.getMessage());
        SkipListener.super.onSkipInRead(t);
    }

    @Override
    public void onSkipInProcess(RawOrder item, Throwable t) {
        LOGGER.warn("Process atlatması orderId {}, customer {}, message:{}",
                item.orderId(), item.customer(), t.getMessage());
        write(item.toCsv(), t.getMessage());
        SkipListener.super.onSkipInProcess(item, t);
    }

    @Override
    public void onSkipInWrite(Order item, Throwable t) {
        LOGGER.warn("Yazmada atlanan kayıt: orderId {}, customer {}, message:{}",
                item.orderId(), item.customer(), t.getMessage());
        write(item.toCsv(), t.getMessage());
        SkipListener.super.onSkipInWrite(item, t);
    }

    private void  write(String rawData,String reason) {

        try {
            Path file = Path.of(outputDir, "skipped-lines-" + runDate + ".csv");
            Files.createDirectories(file.getParent());

            if(!Files.exists(file)) {
                Files.writeString(file, FILE_HEADER + System.lineSeparator());
            }

            String line = rawData + "," + reason + System.lineSeparator();
            Files.writeString(file, line, StandardOpenOption.APPEND);
            LOGGER.info("Atlanan satır dosyaya yazıldı: {} -> {}", rawData, reason);

        } catch (Exception e) {
            LOGGER.error("Atlanan satır dosyaya yazılamadı: {} -> {}", rawData, reason, e);
        }

    }
}
