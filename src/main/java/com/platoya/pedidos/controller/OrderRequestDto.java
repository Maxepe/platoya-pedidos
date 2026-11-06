package com.platoya.pedidos.controller;

import com.platoya.pedidos.model.CustomerInfo;
import com.platoya.pedidos.model.DeliveryAddress;
import com.platoya.pedidos.service.OrderLineRequest;
import com.platoya.pedidos.service.OrderRequest;

import java.util.List;

/** Cuerpo JSON de POST /api/orders. Se mantiene plano para no romper el contrato de la API. */
public class OrderRequestDto {

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerType;
    private String street;
    private String city;
    private String zipCode;
    private List<ItemDto> items;

    public static class ItemDto {
        private Long productId;
        private int quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public OrderRequest toOrderRequest() {
        CustomerInfo customer = new CustomerInfo(customerName, customerEmail, customerPhone, customerType);
        DeliveryAddress deliveryAddress = new DeliveryAddress(street, city, zipCode);
        return new OrderRequest(customer, deliveryAddress, toLines());
    }

    private List<OrderLineRequest> toLines() {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(item -> item == null ? null : new OrderLineRequest(item.getProductId(), item.getQuantity()))
                .toList();
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public List<ItemDto> getItems() { return items; }
    public void setItems(List<ItemDto> items) { this.items = items; }
}
