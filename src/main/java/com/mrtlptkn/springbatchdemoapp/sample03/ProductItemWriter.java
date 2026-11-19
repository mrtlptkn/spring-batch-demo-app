package com.mrtlptkn.springbatchdemoapp.sample03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@StepScope
public class ProductItemWriter implements ItemWriter<ProductResponse> {

    private final DataSource dataSource;
    private final static Logger LOGGER = LoggerFactory.getLogger(ProductItemWriter.class);
    // private final Map<String, Integer> attemps = new HashMap<>();

    public ProductItemWriter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void write(Chunk<? extends ProductResponse> chunk) throws Exception {
        // Filtered Products Gelicek

//        int call = attemps.merge("TransientDataAccessException", 1, Integer::sum);
//        LOGGER.info("Veritabanına yazma denemesi: call={}", call);
//
//        String tableName = "productsv2";
//
//        if(call >= 3){
//            tableName = "non_criticals_products";
//        }

        // ItemPreparedStatementSetter kullanıldığı için JDBC '?' yer tutucularını bekler; ':isim' söz dizimi burada geçersizdir.
//        String sqlQuery = MessageFormat.format("INSERT INTO {0}(productId, productName, unitPrice, unitsInStock) VALUES(?, ?, ?, ?)", tableName);

//        try {
            List<ProductResponse> products = chunk.getItems().stream().collect(Collectors.toUnmodifiableList());

            JdbcBatchItemWriter<ProductResponse> writer = new JdbcBatchItemWriterBuilder<ProductResponse>().sql(
                    "INSERT INTO non_criticals_products(productId, productName, unitPrice, unitsInStock) VALUES(?, ?, ?, ?)"
            ).itemPreparedStatementSetter(new ProductEntityItemSetter()).dataSource(dataSource).build();

            writer.write(new Chunk<>(products));
            LOGGER.info("{} adet ürün veritabanına yazıldı.", products.size());

//        } catch (RuntimeException e) {
//            // Transient DbException simüle etmek için productsv2 tablo ismini kullanıyoruz.
//            throw new TransientDataAccessException("Veritabanına yazma sırasında geçici bir hata oluştu. İşlem tekrar denenebilir.", e) {};
//            // Bu throw edilince retry tekrar tetiklenir.
//        }

    }
}
