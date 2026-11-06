package com.platoya.pedidos.pricing;

import org.springframework.stereotype.Component;

@Component
public class RegularPricingPolicy implements CustomerPricingPolicy {

    @Override
    public String customerType() {
        return "REGULAR";
    }

    @Override
    public double discountFor(double subtotal) {
        return 0;
    }
}
