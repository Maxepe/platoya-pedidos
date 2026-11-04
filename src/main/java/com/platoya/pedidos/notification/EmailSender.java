package com.platoya.pedidos.notification;

public class EmailSender {

    // Simula el envio a traves del proveedor SMTP
    public void send(String to, String subject, String body) {
        System.out.println("[EMAIL] Para: " + to + " | Asunto: " + subject + " | " + body);
    }
}
