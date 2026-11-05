package com.platoya.pedidos.service;

import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.PriceBreakdown;
import com.platoya.pedidos.notification.OrderNotifier;
import com.platoya.pedidos.pricing.OrderPricingService;
import com.platoya.pedidos.repository.OrderRepository;
import com.platoya.pedidos.support.Texts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Coordina el ciclo de vida del pedido. Delega stock, precios y avisos en componentes dedicados. */
@Service
public class OrderProcessor {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final OrderPricingService pricingService;
    private final OrderNotifier notifier;

    public OrderProcessor(OrderRepository orderRepository, InventoryService inventoryService,
                          OrderPricingService pricingService, OrderNotifier notifier) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.pricingService = pricingService;
        this.notifier = notifier;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        List<OrderItem> items = inventoryService.reserve(request.lines());
        PriceBreakdown price = pricingService.price(items, request.customer().type());
        Order order = Order.confirm(request.customer(), request.deliveryAddress(), items, price);
        orderRepository.save(order);
        notifier.orderConfirmed(order);
        return order;
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Order> listOrders(String status) {
        List<Order> all = orderRepository.findAll();
        if (Texts.isBlank(status)) {
            return all;
        }
        List<Order> res = new ArrayList<>();
        for (Order x : all) {
            if (x.getStatus().equals(status)) {
                res.add(x);
            }
        }
        return res;
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        order.cancel();
        inventoryService.release(order);
        notifier.orderCancelled(order);
        return order;
    }

    @Transactional
    public Order deliverOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        order.deliver();
        notifier.orderDelivered(order);
        return order;
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + orderId));
    }
}
