package com.mrtlptkn.springbatchdemoapp.sample03;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class ProductItemProcessor implements ItemProcessor<ProductResponse,ProductResponse> {

    // Burada kritik stok eşik değeri bir jobParameters olarak dışarıdan alınabilir.

    @Override
    public ProductResponse process(ProductResponse item) throws Exception {

        if(item.unitsInStock() <= 20) // Burada verilerimiz yazılmadan önce kritik bir stok eşik değerine sahip ise filtreliyoruz
            return null;

        return item;
    }
}
