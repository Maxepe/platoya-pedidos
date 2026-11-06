package com.platoya.pedidos.pricing;

import org.springframework.stereotype.Component;

@Component
public class EmployeePricingPolicy implements CustomerPricingPolicy {

    static final double EMPLOYEE_DISCOUNT_RATE = 0.20;

    @Override
    public String customerType() {
        return "EMPLEADO";
    }

    @Override
    public double discountFor(double subtotal) {
        return subtotal * EMPLOYEE_DISCOUNT_RATE;
    }
}
