package com.platoya.pedidos.pricing;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Resuelve la politica de precios segun el tipo de cliente. Un tipo desconocido se trata como REGULAR. */
@Component
public class CustomerPricingPolicies {

    private final Map<String, CustomerPricingPolicy> policiesByType;
    private final CustomerPricingPolicy defaultPolicy;

    public CustomerPricingPolicies(List<CustomerPricingPolicy> policies, RegularPricingPolicy defaultPolicy) {
        this.policiesByType = policies.stream()
                .collect(Collectors.toMap(CustomerPricingPolicy::customerType, Function.identity()));
        this.defaultPolicy = defaultPolicy;
    }

    public CustomerPricingPolicy forType(String customerType) {
        return policiesByType.getOrDefault(customerType, defaultPolicy);
    }
}
