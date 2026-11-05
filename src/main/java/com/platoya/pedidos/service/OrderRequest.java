package com.platoya.pedidos.service;

import com.platoya.pedidos.exception.BusinessRuleException;
import com.platoya.pedidos.model.CustomerInfo;
import com.platoya.pedidos.model.DeliveryAddress;

import java.util.List;

public record OrderRequest(CustomerInfo customer, DeliveryAddress deliveryAddress, List<OrderLineRequest> lines) {

    public OrderRequest {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessRuleException("El pedido no tiene items");
        }
    }
}
