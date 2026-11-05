package com.platoya.pedidos.model;

import com.platoya.pedidos.support.Texts;

public record DeliveryAddress(String street, String city, String zipCode) {

    public DeliveryAddress {
        if (Texts.isBlank(street) || Texts.isBlank(city)) {
            throw new RuntimeException("La direccion de entrega es obligatoria");
        }
    }
}
