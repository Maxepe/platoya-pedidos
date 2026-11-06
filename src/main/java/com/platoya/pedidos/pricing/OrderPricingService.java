package com.platoya.pedidos.pricing;

import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.PriceBreakdown;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderPricingService {

    static final double TAX_RATE = 0.21;
    static final double FREE_SHIPPING_THRESHOLD = 20000;
    static final double SHIPPING_COST = 1500;

    private final CustomerPricingPolicies pricingPolicies;

    public OrderPricingService(CustomerPricingPolicies pricingPolicies) {
        this.pricingPolicies = pricingPolicies;
    }

    public PriceBreakdown price(List<OrderItem> items, String customerType) {
        CustomerPricingPolicy policy = pricingPolicies.forType(customerType);
        double subtotal = calculateSubtotal(items);
        double discount = policy.discountFor(subtotal);
        double taxableAmount = subtotal - discount;
        double tax = taxableAmount * TAX_RATE;
        double shippingCost = calculateShipping(taxableAmount, policy);
        double total = Amounts.roundToCents(taxableAmount + tax + shippingCost);
        return new PriceBreakdown(subtotal, discount, tax, shippingCost, total);
    }

    double calculateSubtotal(List<OrderItem> items) {
        return items.stream()
                .mapToDouble(OrderItem::lineTotal)
                .reduce(0, Double::sum);
    }

    private double calculateShipping(double taxableAmount, CustomerPricingPolicy policy) {
        if (policy.hasFreeShipping() || taxableAmount >= FREE_SHIPPING_THRESHOLD) {
            return 0;
        }
        return SHIPPING_COST;
    }
}
