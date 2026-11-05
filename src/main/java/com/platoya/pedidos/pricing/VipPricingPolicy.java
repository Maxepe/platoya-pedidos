package com.platoya.pedidos.pricing;

import org.springframework.stereotype.Component;

@Component
public class VipPricingPolicy implements CustomerPricingPolicy {

    static final double VIP_DISCOUNT_RATE = 0.10;

    @Override
    public String customerType() {
        return "VIP";
    }

    @Override
    public double discountFor(double subtotal) {
        return subtotal * VIP_DISCOUNT_RATE;
    }

    @Override
    public boolean hasFreeShipping() {
        return true;
    }
}
