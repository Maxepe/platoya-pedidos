package com.platoya.pedidos.service;

import com.platoya.pedidos.controller.OrderRequestDto;
import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.Product;
import com.platoya.pedidos.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Reserva y repone stock dentro de la transaccion del pedido. */
@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<OrderItem> reserve(List<OrderRequestDto.ItemDto> lines) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderRequestDto.ItemDto line : lines) {
            Product product = findAvailableProduct(line);
            product.decreaseStock(line.getQuantity());
            items.add(OrderItem.of(product, line.getQuantity()));
        }
        return items;
    }

    public void release(Order order) {
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId())
                    .ifPresent(product -> product.increaseStock(item.getQuantity()));
        }
    }

    private Product findAvailableProduct(OrderRequestDto.ItemDto line) {
        validateLine(line);
        Product product = productRepository.findById(line.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto inexistente: " + line.getProductId()));
        if (!product.hasStock(line.getQuantity())) {
            throw new RuntimeException("Stock insuficiente para " + product.getName());
        }
        return product;
    }

    private void validateLine(OrderRequestDto.ItemDto line) {
        if (line == null || line.getProductId() == null) {
            throw new RuntimeException("Item invalido");
        }
        if (line.getQuantity() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }
    }
}
