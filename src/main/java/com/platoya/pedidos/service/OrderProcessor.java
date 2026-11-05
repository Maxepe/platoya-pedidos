package com.platoya.pedidos.service;

import com.platoya.pedidos.controller.OrderRequestDto;
import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.PriceBreakdown;
import com.platoya.pedidos.notification.OrderNotifier;
import com.platoya.pedidos.pricing.OrderPricingService;
import com.platoya.pedidos.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public Order processOrder(String customerName, String customerEmail, String customerPhone,
                              String customerType, String street, String city, String zipCode,
                              List<OrderRequestDto.ItemDto> items) {
        validateRequest(customerName, customerEmail, street, city, items);
        List<OrderItem> orderItems = inventoryService.reserve(items);
        PriceBreakdown price = pricingService.price(orderItems, customerType);

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setCustomerPhone(customerPhone);
        order.setCustomerType(customerType);
        order.setStreet(street);
        order.setCity(city);
        order.setZipCode(zipCode);
        order.setItems(orderItems);
        applyPrice(order, price);
        order.setStatus("CONFIRMADO");
        order.setCreatedAt(LocalDateTime.now());
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
        if (status == null || status.trim().isEmpty()) {
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

    private void validateRequest(String customerName, String customerEmail, String street, String city,
                                 List<OrderRequestDto.ItemDto> items) {
        if (isBlank(customerName)) {
            throw new RuntimeException("El nombre del cliente es obligatorio");
        }
        if (isBlank(customerEmail) || !customerEmail.contains("@")) {
            throw new RuntimeException("El email del cliente es invalido");
        }
        if (isBlank(street) || isBlank(city)) {
            throw new RuntimeException("La direccion de entrega es obligatoria");
        }
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("El pedido no tiene items");
        }
    }

    private void applyPrice(Order order, PriceBreakdown price) {
        order.setSubtotal(price.subtotal());
        order.setDiscount(price.discount());
        order.setTax(price.tax());
        order.setShippingCost(price.shippingCost());
        order.setTotal(price.total());
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + orderId));
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
