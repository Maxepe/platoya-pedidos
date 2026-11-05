package com.platoya.pedidos.exception;

/** El pedido solicitado no existe. La API la traduce a 404 Not Found. */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Pedido no encontrado: " + orderId);
    }
}
