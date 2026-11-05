package com.platoya.pedidos.pricing;

import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.PriceBreakdown;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderPricingService {

    public PriceBreakdown price(List<OrderItem> items, String customerType) {
        double subtotal = calculateSubtotal(items);
        double discount = calculateDiscount(subtotal, customerType);
        double taxableAmount = subtotal - discount;
        double tax = taxableAmount * 0.21;
        double shippingCost = calculateShipping(taxableAmount, customerType);
        double total = Math.round((taxableAmount + tax + shippingCost) * 100.0) / 100.0;
        return new PriceBreakdown(subtotal, discount, tax, shippingCost, total);
    }

    double calculateSubtotal(List<OrderItem> items) {
        return items.stream()
                .mapToDouble(OrderItem::lineTotal)
                .reduce(0, Double::sum);
    }

    private double calculateDiscount(double subtotal, String customerType) {
        if ("VIP".equals(customerType)) {
            return subtotal * 0.10;
        } else if ("EMPLEADO".equals(customerType)) {
            return subtotal * 0.20;
        }
        return 0;
    }

    private double calculateShipping(double taxableAmount, String customerType) {
        if ("VIP".equals(customerType) || taxableAmount >= 20000) {
            return 0;
        }
        return 1500;
    }
}
