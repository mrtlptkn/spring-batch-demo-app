package com.mrtlptkn.springbatchdemoapp.sample03;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ODataResponse(@JsonProperty("value") List<ProductResponse> value) {
}
