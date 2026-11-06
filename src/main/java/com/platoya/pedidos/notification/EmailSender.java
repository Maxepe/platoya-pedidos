package com.platoya.pedidos.notification;

import org.springframework.stereotype.Component;

@Component
public class EmailSender implements EmailGateway {

    // Simula el envio a traves del proveedor SMTP
    @Override
    public void send(String to, String subject, String body) {
        System.out.println("[EMAIL] Para: " + to + " | Asunto: " + subject + " | " + body);
    }
}
