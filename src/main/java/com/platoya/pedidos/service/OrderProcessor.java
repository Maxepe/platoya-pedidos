package com.platoya.pedidos.service;

import com.platoya.pedidos.controller.OrderRequestDto;
import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.PriceBreakdown;
import com.platoya.pedidos.model.Product;
import com.platoya.pedidos.notification.OrderNotifier;
import com.platoya.pedidos.pricing.OrderPricingService;
import com.platoya.pedidos.repository.OrderRepository;
import com.platoya.pedidos.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderProcessor {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderPricingService pricingService;
    private final OrderNotifier notifier;

    public OrderProcessor(OrderRepository orderRepository, ProductRepository productRepository,
                          OrderPricingService pricingService, OrderNotifier notifier) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.pricingService = pricingService;
        this.notifier = notifier;
    }

    public Order processOrder(String customerName, String customerEmail, String customerPhone,
                              String customerType, String street, String city, String zipCode,
                              List<OrderRequestDto.ItemDto> items) {
        validateRequest(customerName, customerEmail, street, city, items);
        List<OrderItem> orderItems = reserveItems(items);
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

    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

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

    public Order cancelOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        if (!order.getStatus().equals("CONFIRMADO")) {
            throw new RuntimeException("Solo se pueden cancelar pedidos confirmados");
        }
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId()).get();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        order.setStatus("CANCELADO");
        orderRepository.save(order);
        notifier.orderCancelled(order);
        return order;
    }

    public Order deliverOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        if (!order.getStatus().equals("CONFIRMADO")) {
            throw new RuntimeException("Solo se pueden entregar pedidos confirmados");
        }
        order.setStatus("ENTREGADO");
        orderRepository.save(order);
        notifier.orderDelivered(order);
        return order;
    }

    public Map<String, Object> report() {
        List<Order> all = orderRepository.findAll();
        int c1 = 0;
        int c2 = 0;
        int c3 = 0;
        double fact = 0;
        for (Order x : all) {
            if (x.getStatus().equals("CONFIRMADO")) {
                c1++;
                fact = fact + x.getTotal();
            } else if (x.getStatus().equals("ENTREGADO")) {
                c2++;
                fact = fact + x.getTotal();
            } else if (x.getStatus().equals("CANCELADO")) {
                c3++;
            }
        }
        Map<String, Object> r = new HashMap<>();
        r.put("cantidadPedidos", all.size());
        r.put("confirmados", c1);
        r.put("entregados", c2);
        r.put("cancelados", c3);
        r.put("totalFacturado", Math.round(fact * 100.0) / 100.0);
        if (c1 + c2 > 0) {
            r.put("ticketPromedio", Math.round(fact / (c1 + c2) * 100.0) / 100.0);
        } else {
            r.put("ticketPromedio", 0.0);
        }
        return r;
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

    private List<OrderItem> reserveItems(List<OrderRequestDto.ItemDto> items) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderRequestDto.ItemDto line : items) {
            Product product = findAvailableProduct(line);
            product.setStock(product.getStock() - line.getQuantity());
            productRepository.save(product);
            orderItems.add(toOrderItem(product, line.getQuantity()));
        }
        return orderItems;
    }

    private Product findAvailableProduct(OrderRequestDto.ItemDto line) {
        if (line == null || line.getProductId() == null) {
            throw new RuntimeException("Item invalido");
        }
        if (line.getQuantity() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }
        Product product = productRepository.findById(line.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto inexistente: " + line.getProductId()));
        if (product.getStock() < line.getQuantity()) {
            throw new RuntimeException("Stock insuficiente para " + product.getName());
        }
        return product;
    }

    private OrderItem toOrderItem(Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(quantity);
        return item;
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
