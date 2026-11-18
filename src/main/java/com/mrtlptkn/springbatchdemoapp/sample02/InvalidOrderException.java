package com.mrtlptkn.springbatchdemoapp.sample02;

public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
