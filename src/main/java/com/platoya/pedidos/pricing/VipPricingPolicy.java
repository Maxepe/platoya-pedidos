package com.platoya.pedidos.pricing;

import org.springframework.stereotype.Component;

@Component
public class VipPricingPolicy implements CustomerPricingPolicy {

    @Override
    public String customerType() {
        return "VIP";
    }

    @Override
    public double discountFor(double subtotal) {
        return subtotal * 0.10;
    }

    @Override
    public boolean hasFreeShipping() {
        return true;
    }
}
