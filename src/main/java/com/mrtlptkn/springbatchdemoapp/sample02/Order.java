package com.mrtlptkn.springbatchdemoapp.sample02;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Order(Long orderId, String customer, BigDecimal amount, LocalDate orderDate) {
    public String toCsv() {
        return String.join(",", orderId.toString(), customer, amount.toPlainString(), orderDate.toString());
    }
}
