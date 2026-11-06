package com.platoya.pedidos.exception;

public class InsufficientStockException extends BusinessRuleException {

    public InsufficientStockException(String productName) {
        super("Stock insuficiente para " + productName);
    }
}
