package com.platoya.pedidos.pricing;

import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.PriceBreakdown;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderPricingService {

    private final CustomerPricingPolicies pricingPolicies;

    public OrderPricingService(CustomerPricingPolicies pricingPolicies) {
        this.pricingPolicies = pricingPolicies;
    }

    public PriceBreakdown price(List<OrderItem> items, String customerType) {
        CustomerPricingPolicy policy = pricingPolicies.forType(customerType);
        double subtotal = calculateSubtotal(items);
        double discount = policy.discountFor(subtotal);
        double taxableAmount = subtotal - discount;
        double tax = taxableAmount * 0.21;
        double shippingCost = calculateShipping(taxableAmount, policy);
        double total = Math.round((taxableAmount + tax + shippingCost) * 100.0) / 100.0;
        return new PriceBreakdown(subtotal, discount, tax, shippingCost, total);
    }

    double calculateSubtotal(List<OrderItem> items) {
        return items.stream()
                .mapToDouble(OrderItem::lineTotal)
                .reduce(0, Double::sum);
    }

    private double calculateShipping(double taxableAmount, CustomerPricingPolicy policy) {
        if (policy.hasFreeShipping() || taxableAmount >= 20000) {
            return 0;
        }
        return 1500;
    }
}
