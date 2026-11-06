package com.platoya.pedidos.notification;

import com.platoya.pedidos.model.Order;

/** Avisos al cliente ante cada cambio de estado del pedido. */
public interface OrderNotifier {

    void orderConfirmed(Order order);

    void orderCancelled(Order order);

    void orderDelivered(Order order);
}
