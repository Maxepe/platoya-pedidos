package com.platoya.pedidos.notification;

public interface EmailGateway {

    void send(String to, String subject, String body);
}
