package com.platoya.pedidos.model;

import com.platoya.pedidos.support.Texts;

public record CustomerInfo(String name, String email, String phone, String type) {

    public CustomerInfo {
        if (Texts.isBlank(name)) {
            throw new RuntimeException("El nombre del cliente es obligatorio");
        }
        if (Texts.isBlank(email) || !email.contains("@")) {
            throw new RuntimeException("El email del cliente es invalido");
        }
    }
}
