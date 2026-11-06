package com.platoya.pedidos.model;

public record PriceBreakdown(double subtotal, double discount, double tax, double shippingCost, double total) {
}
