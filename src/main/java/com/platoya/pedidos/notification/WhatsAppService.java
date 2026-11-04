package com.platoya.pedidos.notification;

public class WhatsAppService {

    // Simula el envio a traves de la API de WhatsApp Business
    public void sendMessage(String phone, String text) {
        System.out.println("[WHATSAPP] Para: " + phone + " | " + text);
    }
}
