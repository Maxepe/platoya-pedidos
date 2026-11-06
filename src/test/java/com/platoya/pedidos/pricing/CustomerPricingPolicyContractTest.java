package com.platoya.pedidos.pricing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica el contrato comun de CustomerPricingPolicy (LSP): cualquier implementacion
 * puede reemplazar a otra sin que OrderPricingService produzca montos invalidos.
 */
class CustomerPricingPolicyContractTest {

    static Stream<CustomerPricingPolicy> politicas() {
        return Stream.of(new RegularPricingPolicy(), new VipPricingPolicy(), new EmployeePricingPolicy());
    }

    @ParameterizedTest
    @MethodSource("politicas")
    void elDescuentoNuncaEsNegativoNiSuperaElSubtotal(CustomerPricingPolicy policy) {
        for (double subtotal : new double[] {0, 1, 3500, 20000, 31500, 1_000_000}) {
            assertThat(policy.discountFor(subtotal)).isBetween(0.0, subtotal);
        }
    }

    @ParameterizedTest
    @MethodSource("politicas")
    void cadaPoliticaDeclaraUnTipoDeCliente(CustomerPricingPolicy policy) {
        assertThat(policy.customerType()).isNotBlank();
    }
}
