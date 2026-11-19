package com.mrtlptkn.springbatchdemoapp.sample03;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProductResponse(
        @JsonProperty("ProductID") Integer productId,
        @JsonProperty("ProductName") String productName,
        @JsonProperty("UnitPrice")BigDecimal unitPrice,
        @JsonProperty("UnitsInStock") Integer unitsInStock
        ) {

}
