package com.platoya.pedidos.notification;

import com.platoya.pedidos.model.Order;
import org.springframework.stereotype.Component;

/** Decide que canal usar en cada evento y arma el texto de cada aviso. */
@Component
public class NotificationDispatcher implements OrderNotifier {

    private final EmailGateway email;
    private final WhatsAppGateway whatsApp;

    public NotificationDispatcher(EmailGateway email, WhatsAppGateway whatsApp) {
        this.email = email;
        this.whatsApp = whatsApp;
    }

    @Override
    public void orderConfirmed(Order order) {
        email.send(order.getCustomerEmail(), "Pedido confirmado",
                "Hola " + order.getCustomerName() + ", tu pedido #" + order.getId()
                        + " fue confirmado. Total: $" + order.getTotal());
        sendWhatsAppIfPossible(order,
                "PlatoYa: tu pedido #" + order.getId() + " fue confirmado. Total: $" + order.getTotal());
    }

    @Override
    public void orderCancelled(Order order) {
        email.send(order.getCustomerEmail(), "Pedido cancelado",
                "Hola " + order.getCustomerName() + ", tu pedido #" + order.getId() + " fue cancelado.");
    }

    @Override
    public void orderDelivered(Order order) {
        sendWhatsAppIfPossible(order, "PlatoYa: tu pedido #" + order.getId() + " fue entregado. Gracias!");
    }

    private void sendWhatsAppIfPossible(Order order, String text) {
        if (order.hasCustomerPhone()) {
            whatsApp.sendMessage(order.getCustomerPhone(), text);
        }
    }
}
