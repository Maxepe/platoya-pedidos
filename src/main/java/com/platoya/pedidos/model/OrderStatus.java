package com.platoya.pedidos.model;

import java.util.Arrays;
import java.util.Optional;

public enum OrderStatus {
    CONFIRMADO,
    ENTREGADO,
    CANCELADO;

    public static Optional<OrderStatus> fromName(String name) {
        return Arrays.stream(values())
                .filter(status -> status.name().equals(name))
                .findFirst();
    }
}
