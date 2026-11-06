package com.platoya.pedidos.model;

import com.platoya.pedidos.exception.BusinessRuleException;
import com.platoya.pedidos.support.Texts;

public record DeliveryAddress(String street, String city, String zipCode) {

    public DeliveryAddress {
        if (Texts.isBlank(street) || Texts.isBlank(city)) {
            throw new BusinessRuleException("La direccion de entrega es obligatoria");
        }
    }
}
