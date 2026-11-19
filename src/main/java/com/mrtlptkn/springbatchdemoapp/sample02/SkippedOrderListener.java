package com.mrtlptkn.springbatchdemoapp.sample02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

// Hata tolere çalışıyorsa -> Dosya üzerinde skip işlemleri yapıyorsa SkipListener ile atlatılan kayıtları kesinlikle loglamalıyız yada bir yerde bir dosyada saklamalıyız.
@Component
@StepScope
public class SkippedOrderListener implements SkipListener<RawOrder,Order> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkippedOrderListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        LOGGER.warn("Okumada atlanan kayıt: {}", t.getMessage());
        SkipListener.super.onSkipInRead(t);
    }

    @Override
    public void onSkipInProcess(RawOrder item, Throwable t) {
        LOGGER.warn("Okuma atlatması orderId {}, customer {}, message:{}",
                item.orderId(), item.customer(), t.getMessage());
        SkipListener.super.onSkipInProcess(item, t);
    }

    @Override
    public void onSkipInWrite(Order item, Throwable t) {
        LOGGER.warn("Yazmada atlanan kayıt: orderId {}, customer {}, message:{}",
                item.orderId(), item.customer(), t.getMessage());
        SkipListener.super.onSkipInWrite(item, t);
    }
}
