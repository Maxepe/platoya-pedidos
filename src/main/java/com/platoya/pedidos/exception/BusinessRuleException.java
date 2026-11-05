package com.platoya.pedidos.exception;

/** Violacion de una regla de negocio. La API la traduce a 400 Bad Request. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
