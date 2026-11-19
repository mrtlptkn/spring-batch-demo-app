package com.mrtlptkn.springbatchdemoapp.sample03;

import org.springframework.batch.item.database.ItemPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public class ProductEntityItemSetter implements ItemPreparedStatementSetter<ProductResponse> {

    @Override
    public void setValues(ProductResponse item, PreparedStatement ps) throws SQLException {
        ps.setInt(1, item.productId());
        ps.setString(2, item.productName());
        ps.setBigDecimal(3, item.unitPrice());
        ps.setInt(4, item.unitsInStock());
    }
}
