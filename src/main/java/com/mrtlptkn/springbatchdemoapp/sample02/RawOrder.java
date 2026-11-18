package com.mrtlptkn.springbatchdemoapp.sample02;

/*
CSV üzerinden verileri öncelikle bu record sınıfı üzerinden mapleyeceğiz
 */

public record RawOrder(String orderId, String customer, String amount, String orderDate) {

    public String toCsv() {
        return String.join(",", orderId, customer, amount, orderDate);
    }
}


/* Dosyada okunamayan yada process yapılamayaca  RawOrder Order mapleyemeyeceğim veriler InValidOrderException fırlatacağız*/

