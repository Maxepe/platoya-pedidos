package com.platoya.pedidos.pricing;

import org.springframework.stereotype.Component;

@Component
public class EmployeePricingPolicy implements CustomerPricingPolicy {

    @Override
    public String customerType() {
        return "EMPLEADO";
    }

    @Override
    public double discountFor(double subtotal) {
        return subtotal * 0.20;
    }
}
