package com.platoya.pedidos.pricing;

public final class Amounts {

    private static final double CENTS_PER_UNIT = 100.0;

    private Amounts() {
    }

    public static double roundToCents(double amount) {
        return Math.round(amount * CENTS_PER_UNIT) / CENTS_PER_UNIT;
    }
}
