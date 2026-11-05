package com.platoya.pedidos.support;

public final class Texts {

    private Texts() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
