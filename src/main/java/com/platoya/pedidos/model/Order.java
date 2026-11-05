package com.platoya.pedidos.model;

import com.platoya.pedidos.support.Texts;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerType;
    private String street;
    private String city;
    private String zipCode;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    private double subtotal;
    private double discount;
    private double tax;
    private double shippingCost;
    private double total;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private LocalDateTime createdAt;

    protected Order() {
    }

    public static Order confirm(CustomerInfo customer, DeliveryAddress address,
                                List<OrderItem> items, PriceBreakdown price) {
        Order order = new Order();
        order.customerName = customer.name();
        order.customerEmail = customer.email();
        order.customerPhone = customer.phone();
        order.customerType = customer.type();
        order.street = address.street();
        order.city = address.city();
        order.zipCode = address.zipCode();
        order.items = new ArrayList<>(items);
        order.subtotal = price.subtotal();
        order.discount = price.discount();
        order.tax = price.tax();
        order.shippingCost = price.shippingCost();
        order.total = price.total();
        order.status = OrderStatus.CONFIRMADO;
        order.createdAt = LocalDateTime.now();
        return order;
    }

    public void cancel() {
        requireConfirmed("Solo se pueden cancelar pedidos confirmados");
        status = OrderStatus.CANCELADO;
    }

    public void deliver() {
        requireConfirmed("Solo se pueden entregar pedidos confirmados");
        status = OrderStatus.ENTREGADO;
    }

    public boolean isBilled() {
        return status == OrderStatus.CONFIRMADO || status == OrderStatus.ENTREGADO;
    }

    public boolean hasCustomerPhone() {
        return !Texts.isBlank(customerPhone);
    }

    private void requireConfirmed(String message) {
        if (status != OrderStatus.CONFIRMADO) {
            throw new RuntimeException(message);
        }
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerType() { return customerType; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getZipCode() { return zipCode; }
    public List<OrderItem> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
    public double getDiscount() { return discount; }
    public double getTax() { return tax; }
    public double getShippingCost() { return shippingCost; }
    public double getTotal() { return total; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
