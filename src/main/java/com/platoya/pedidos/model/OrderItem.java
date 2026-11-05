package com.platoya.pedidos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private String productName;
    private double unitPrice;
    private int quantity;

    protected OrderItem() {
    }

    public static OrderItem of(Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.productId = product.getId();
        item.productName = product.getName();
        item.unitPrice = product.getPrice();
        item.quantity = quantity;
        return item;
    }

    public double lineTotal() {
        return unitPrice * quantity;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
}
