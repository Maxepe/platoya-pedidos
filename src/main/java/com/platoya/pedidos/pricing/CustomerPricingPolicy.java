package com.platoya.pedidos.pricing;

/**
 * Beneficios comerciales de un tipo de cliente.
 * Contrato: discountFor(subtotal) devuelve un valor entre 0 y subtotal.
 */
public interface CustomerPricingPolicy {

    String customerType();

    double discountFor(double subtotal);

    default boolean hasFreeShipping() {
        return false;
    }
}
