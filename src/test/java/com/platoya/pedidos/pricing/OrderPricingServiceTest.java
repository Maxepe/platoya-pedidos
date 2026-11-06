package com.platoya.pedidos.pricing;

import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.PriceBreakdown;
import com.platoya.pedidos.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPricingServiceTest {

    private static final Product HAMBURGUESA = new Product(1L, "Hamburguesa completa", "COCINA", 14000, 20);
    private static final Product CERVEZA = new Product(2L, "Cerveza pinta", "BARRA", 6500, 50);
    private static final Product GASEOSA = new Product(3L, "Gaseosa", "BARRA", 3500, 30);

    private final OrderPricingService pricingService = new OrderPricingService(
            new CustomerPricingPolicies(
                    List.of(new RegularPricingPolicy(), new VipPricingPolicy(), new EmployeePricingPolicy()),
                    new RegularPricingPolicy()));

    @Test
    @DisplayName("El subtotal suma precio por cantidad de cada linea")
    void subtotalSumaLineas() {
        List<OrderItem> items = List.of(OrderItem.of(HAMBURGUESA, 2), OrderItem.of(GASEOSA, 1));

        assertThat(pricingService.calculateSubtotal(items)).isEqualTo(31500.0);
    }

    @Test
    @DisplayName("REGULAR bajo el umbral: IVA 21% y envio de $1500")
    void regularBajoUmbral() {
        PriceBreakdown price = pricingService.price(List.of(OrderItem.of(HAMBURGUESA, 1)), "REGULAR");

        assertThat(price).isEqualTo(new PriceBreakdown(14000, 0, 2940, 1500, 18440));
    }

    @Test
    @DisplayName("Una base imponible de exactamente $20000 ya no paga envio")
    void limiteDelEnvioGratis() {
        PriceBreakdown price = pricingService.price(
                List.of(OrderItem.of(CERVEZA, 2), OrderItem.of(GASEOSA, 2)), "REGULAR");

        assertThat(price.shippingCost()).isZero();
        assertThat(price.total()).isEqualTo(24200.0);
    }

    @Test
    @DisplayName("VIP: 10% de descuento y envio sin cargo aunque este bajo el umbral")
    void vipConEnvioGratis() {
        PriceBreakdown price = pricingService.price(List.of(OrderItem.of(GASEOSA, 1)), "VIP");

        assertThat(price.discount()).isEqualTo(350.0);
        assertThat(price.shippingCost()).isZero();
        assertThat(price.total()).isEqualTo(3811.5);
    }

    @Test
    @DisplayName("Un tipo de cliente desconocido o nulo se cotiza como REGULAR")
    void tipoDesconocidoComoRegular() {
        List<OrderItem> items = List.of(OrderItem.of(GASEOSA, 1));

        assertThat(pricingService.price(items, "vip")).isEqualTo(pricingService.price(items, "REGULAR"));
        assertThat(pricingService.price(items, null)).isEqualTo(pricingService.price(items, "REGULAR"));
    }
}
