package com.mrtlptkn.springbatchdemoapp.sample02;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class OrderItemReader implements ItemReader<RawOrder> {
    private final RawOrder[] orders = {
            new RawOrder("1001", "Alice", "250.75", "2023-01-15"),
            new RawOrder("1002", "Bob", "150.50", "2023-01-16"),
            new RawOrder("1003", "Charlie", "300.00", "2023-01-17"),
            new RawOrder("1004", "David", "200.25", "2023-01-18"),
            new RawOrder("1005", "Eve", "400.00", "2023-01-19")
    };

    private int index = 0;

    @Override
    public RawOrder read() {
        if (index < orders.length) {
            return orders[index++];
        } else {
            return null; // No more items to read
        }
    }
}
