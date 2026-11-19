package com.mrtlptkn.springbatchdemoapp.sample02;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@StepScope
public class OrderItemMappingProcessor implements ItemProcessor<RawOrder,Order> {

    private static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");


    @Override
    public Order process(RawOrder item) throws Exception {
        // id validation
        Long id;
        try {
            id = Long.parseLong(item.orderId().trim());
        } catch (NumberFormatException e) {
            throw new InvalidOrderException("order_id sayi değil");
        }
        // Müşteri alanı kontrolü
        if(item.customer() == null || item.customer().isBlank()) {
            throw new InvalidOrderException("müşteri boş");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(item.amount().trim());
        } catch (Exception e) {
            // Exception durumunda Job failed olur.
            throw new InvalidOrderException("tutar boş veya sayı formatında değil");
        }
        LocalDate orderDate;
        try{
            orderDate = LocalDate.parse(item.orderDate().trim(), DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new InvalidOrderException("tarih formatı geçersiz");
        }
        return new Order(id, item.customer().trim(), amount, orderDate, LocalDate.now(ISTANBUL));
    }
}
