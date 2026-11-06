package com.platoya.pedidos.service;

import com.platoya.pedidos.exception.BusinessRuleException;
import com.platoya.pedidos.exception.InsufficientStockException;
import com.platoya.pedidos.exception.OrderNotFoundException;
import com.platoya.pedidos.model.CustomerInfo;
import com.platoya.pedidos.model.DeliveryAddress;
import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.OrderStatus;
import com.platoya.pedidos.model.PriceBreakdown;
import com.platoya.pedidos.model.Product;
import com.platoya.pedidos.notification.OrderNotifier;
import com.platoya.pedidos.pricing.OrderPricingService;
import com.platoya.pedidos.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderProcessorTest {

    private static final Product HAMBURGUESA = new Product(1L, "Hamburguesa completa", "COCINA", 14000, 20);
    private static final PriceBreakdown PRECIO = new PriceBreakdown(14000, 0, 2940, 1500, 18440);

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private OrderPricingService pricingService;
    @Mock
    private OrderNotifier notifier;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private final OrderRequest solicitud = new OrderRequest(
            new CustomerInfo("Ana Prueba", "ana@example.com", null, "REGULAR"),
            new DeliveryAddress("Calle Falsa 123", "San Nicolas", "2900"),
            List.of(new OrderLineRequest(1L, 1)));

    @Test
    void confirmarUnPedidoLoGuardaYNotificaUnaVez() {
        List<OrderItem> items = List.of(OrderItem.of(HAMBURGUESA, 1));
        when(inventoryService.reserve(solicitud.lines())).thenReturn(items);
        when(pricingService.price(items, "REGULAR")).thenReturn(PRECIO);

        Order order = orderProcessor.createOrder(solicitud);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMADO);
        assertThat(order.getTotal()).isEqualTo(18440.0);
        verify(orderRepository).save(order);
        verify(notifier).orderConfirmed(order);
    }

    @Test
    void sinStockNoSeGuardaNiSeNotifica() {
        when(inventoryService.reserve(solicitud.lines()))
                .thenThrow(new InsufficientStockException("Hamburguesa completa"));

        assertThatThrownBy(() -> orderProcessor.createOrder(solicitud))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Stock insuficiente para Hamburguesa completa");
        verify(orderRepository, never()).save(any());
        verify(notifier, never()).orderConfirmed(any());
    }

    @Test
    void cancelarReponeStockYNotifica() {
        Order order = pedidoConfirmado();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderProcessor.cancelOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELADO);
        verify(inventoryService).release(order);
        verify(notifier).orderCancelled(order);
    }

    @Test
    void cancelarUnEntregadoNoReponeStockNiNotifica() {
        Order order = pedidoConfirmado();
        order.deliver();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderProcessor.cancelOrder(1L)).isInstanceOf(BusinessRuleException.class);
        verify(inventoryService, never()).release(any());
        verify(notifier, never()).orderCancelled(any());
    }

    @Test
    void consultarUnPedidoInexistenteLanzaOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderProcessor.getOrder(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Pedido no encontrado: 99");
    }

    private static Order pedidoConfirmado() {
        return Order.confirm(
                new CustomerInfo("Ana Prueba", "ana@example.com", null, "REGULAR"),
                new DeliveryAddress("Calle Falsa 123", "San Nicolas", "2900"),
                List.of(OrderItem.of(HAMBURGUESA, 1)), PRECIO);
    }
}
