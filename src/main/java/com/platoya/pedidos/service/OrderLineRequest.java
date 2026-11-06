package com.platoya.pedidos.service;

public record OrderLineRequest(Long productId, int quantity) {
}
