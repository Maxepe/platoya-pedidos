package com.platoya.pedidos.model;

import com.platoya.pedidos.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    static Order pedidoConfirmado(String telefono) {
        return Order.confirm(
                new CustomerInfo("Ana Prueba", "ana@example.com", telefono, "REGULAR"),
                new DeliveryAddress("Calle Falsa 123", "San Nicolas", "2900"),
                List.of(OrderItem.of(new Product(1L, "Hamburguesa completa", "COCINA", 14000, 20), 1)),
                new PriceBreakdown(14000, 0, 2940, 1500, 18440));
    }

    @Test
    void unPedidoNuevoQuedaConfirmadoYFacturable() {
        Order order = pedidoConfirmado(null);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMADO);
        assertThat(order.isBilled()).isTrue();
    }

    @Test
    void cancelarUnConfirmadoLoDejaFueraDeLaFacturacion() {
        Order order = pedidoConfirmado(null);

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELADO);
        assertThat(order.isBilled()).isFalse();
    }

    @Test
    void noSePuedeCancelarUnPedidoEntregado() {
        Order order = pedidoConfirmado(null);
        order.deliver();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Solo se pueden cancelar pedidos confirmados");
    }

    @Test
    void noSePuedeEntregarUnPedidoCancelado() {
        Order order = pedidoConfirmado(null);
        order.cancel();

        assertThatThrownBy(order::deliver)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Solo se pueden entregar pedidos confirmados");
    }

    @Test
    void unTelefonoEnBlancoCuentaComoAusente() {
        assertThat(pedidoConfirmado("   ").hasCustomerPhone()).isFalse();
        assertThat(pedidoConfirmado("+5490000000001").hasCustomerPhone()).isTrue();
    }
}
