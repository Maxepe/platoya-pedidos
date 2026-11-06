package com.platoya.pedidos.notification;

import org.springframework.stereotype.Component;

@Component
public class WhatsAppService implements WhatsAppGateway {

    // Simula el envio a traves de la API de WhatsApp Business
    @Override
    public void sendMessage(String phone, String text) {
        System.out.println("[WHATSAPP] Para: " + phone + " | " + text);
    }
}
