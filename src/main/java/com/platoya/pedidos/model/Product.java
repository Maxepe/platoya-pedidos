package com.platoya.pedidos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private Long id;
    private String name;
    private String category;
    private double price;
    private int stock;

    protected Product() {
    }

    public Product(Long id, String name, String category, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public boolean hasStock(int quantity) {
        return stock >= quantity;
    }

    public void decreaseStock(int quantity) {
        stock -= quantity;
    }

    public void increaseStock(int quantity) {
        stock += quantity;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
}
