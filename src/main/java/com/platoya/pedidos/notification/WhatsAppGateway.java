package com.platoya.pedidos.notification;

public interface WhatsAppGateway {

    void sendMessage(String phone, String text);
}
