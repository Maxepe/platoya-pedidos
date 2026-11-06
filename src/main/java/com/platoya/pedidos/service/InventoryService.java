package com.platoya.pedidos.service;

import com.platoya.pedidos.exception.BusinessRuleException;
import com.platoya.pedidos.exception.InsufficientStockException;
import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.Product;
import com.platoya.pedidos.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Reserva y repone stock. Debe ejecutarse dentro de la transaccion del pedido. */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<OrderItem> reserve(List<OrderLineRequest> lines) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderLineRequest line : lines) {
            Product product = findAvailableProduct(line);
            product.decreaseStock(line.quantity());
            items.add(OrderItem.of(product, line.quantity()));
        }
        return items;
    }

    public void release(Order order) {
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId())
                    .ifPresent(product -> product.increaseStock(item.getQuantity()));
        }
    }

    private Product findAvailableProduct(OrderLineRequest line) {
        validateLine(line);
        Product product = productRepository.findById(line.productId())
                .orElseThrow(() -> new BusinessRuleException("Producto inexistente: " + line.productId()));
        if (!product.hasStock(line.quantity())) {
            throw new InsufficientStockException(product.getName());
        }
        return product;
    }

    private void validateLine(OrderLineRequest line) {
        if (line == null || line.productId() == null) {
            throw new BusinessRuleException("Item invalido");
        }
        if (line.quantity() <= 0) {
            throw new BusinessRuleException("La cantidad debe ser mayor a cero");
        }
    }
}
