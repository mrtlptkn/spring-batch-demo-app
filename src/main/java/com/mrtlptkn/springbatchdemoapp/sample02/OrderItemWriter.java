package com.mrtlptkn.springbatchdemoapp.sample02;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

// Writer Processordan geçen yazılacak veri ipini bekler.

@Component
@StepScope
public class OrderItemWriter implements ItemWriter<Order> {
    @Override
    public void write(Chunk<? extends Order> items) throws Exception {
        // chunkda kaç order var.
        List<? extends Order> orders = items.getItems();

        // Chunk içindeki orderlarda dönüp bunu yazdırıyoruz.
        for (Order order : orders) {
            System.out.println("Writing order: " + order);
        }
    }

}
