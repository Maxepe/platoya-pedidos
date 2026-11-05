package com.platoya.pedidos.model;

import com.platoya.pedidos.exception.BusinessRuleException;
import com.platoya.pedidos.support.Texts;

public record CustomerInfo(String name, String email, String phone, String type) {

    public CustomerInfo {
        if (Texts.isBlank(name)) {
            throw new BusinessRuleException("El nombre del cliente es obligatorio");
        }
        if (Texts.isBlank(email) || !email.contains("@")) {
            throw new BusinessRuleException("El email del cliente es invalido");
        }
    }
}
