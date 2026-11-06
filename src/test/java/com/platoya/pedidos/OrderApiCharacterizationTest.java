package com.platoya.pedidos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de caracterizacion: fijan el comportamiento externo observable de la API
 * (estado HTTP, cuerpo JSON, stock y notificaciones) tal como esta en la version base.
 * Se ejecutan sin cambios antes y despues del refactoring.
 *
 * Productos precargados (data.sql): 1 Hamburguesa $14000 (stock 20), 2 Cerveza $6500 (50),
 * 3 Gaseosa $3500 (30), 4 Papas $7000 (10), 5 Pizza $12000 (3).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OrderApiCharacterizationTest {

    private static final double CENTAVO = 0.001;

    @Autowired
    private MockMvc mvc;

    // ---------------------------------------------------------------- calculo de precios

    @Test
    @DisplayName("C01 - Cliente REGULAR por debajo de $20000 paga IVA y envio")
    void regularBajoUmbralPagaEnvio() throws Exception {
        crearPedido("REGULAR", "\"+5490000000002\"", item(1, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal", closeTo(14000.0, CENTAVO)))
                .andExpect(jsonPath("$.discount", closeTo(0.0, CENTAVO)))
                .andExpect(jsonPath("$.tax", closeTo(2940.0, CENTAVO)))
                .andExpect(jsonPath("$.shippingCost", closeTo(1500.0, CENTAVO)))
                .andExpect(jsonPath("$.total", closeTo(18440.0, CENTAVO)))
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
    }

    @Test
    @DisplayName("C02 - Cliente REGULAR desde $20000 no paga envio")
    void regularSobreUmbralSinEnvio() throws Exception {
        crearPedido("REGULAR", "null", item(1, 1), item(2, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal", closeTo(27000.0, CENTAVO)))
                .andExpect(jsonPath("$.tax", closeTo(5670.0, CENTAVO)))
                .andExpect(jsonPath("$.shippingCost", closeTo(0.0, CENTAVO)))
                .andExpect(jsonPath("$.total", closeTo(32670.0, CENTAVO)));
    }

    @Test
    @DisplayName("C03 - Limite exacto: base imponible de $20000 no paga envio")
    void limiteExactoDelEnvio() throws Exception {
        crearPedido("REGULAR", "null", item(2, 2), item(3, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal", closeTo(20000.0, CENTAVO)))
                .andExpect(jsonPath("$.shippingCost", closeTo(0.0, CENTAVO)))
                .andExpect(jsonPath("$.total", closeTo(24200.0, CENTAVO)));
    }

    @Test
    @DisplayName("C04 - Cliente VIP: 10% de descuento y envio sin cargo")
    void vipConDescuentoYSinEnvio() throws Exception {
        crearPedido("VIP", "\"+5490000000001\"", item(1, 2), item(3, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal", closeTo(31500.0, CENTAVO)))
                .andExpect(jsonPath("$.discount", closeTo(3150.0, CENTAVO)))
                .andExpect(jsonPath("$.tax", closeTo(5953.5, CENTAVO)))
                .andExpect(jsonPath("$.shippingCost", closeTo(0.0, CENTAVO)))
                .andExpect(jsonPath("$.total", closeTo(34303.5, CENTAVO)));
    }

    @Test
    @DisplayName("C05 - Cliente VIP por debajo del umbral tampoco paga envio")
    void vipBajoUmbralSinEnvio() throws Exception {
        crearPedido("VIP", "null", item(3, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discount", closeTo(350.0, CENTAVO)))
                .andExpect(jsonPath("$.shippingCost", closeTo(0.0, CENTAVO)))
                .andExpect(jsonPath("$.total", closeTo(3811.5, CENTAVO)));
    }

    @Test
    @DisplayName("C06 - Cliente EMPLEADO: 20% de descuento y paga envio")
    void empleadoConDescuento() throws Exception {
        crearPedido("EMPLEADO", "null", item(4, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discount", closeTo(1400.0, CENTAVO)))
                .andExpect(jsonPath("$.tax", closeTo(1176.0, CENTAVO)))
                .andExpect(jsonPath("$.shippingCost", closeTo(1500.0, CENTAVO)))
                .andExpect(jsonPath("$.total", closeTo(8276.0, CENTAVO)));
    }

    @Test
    @DisplayName("C07 - Un tipo de cliente desconocido (\"vip\" en minuscula) se trata como REGULAR")
    void tipoDesconocidoSeTrataComoRegular() throws Exception {
        crearPedido("vip", "null", item(3, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discount", closeTo(0.0, CENTAVO)))
                .andExpect(jsonPath("$.shippingCost", closeTo(1500.0, CENTAVO)))
                .andExpect(jsonPath("$.total", closeTo(5735.0, CENTAVO)));
    }

    // ---------------------------------------------------------------- validaciones

    @Test
    @DisplayName("C08 - Pedido sin items es rechazado")
    void pedidoSinItems() throws Exception {
        crearPedido("REGULAR", "null")
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El pedido no tiene items"));
    }

    @Test
    @DisplayName("C09 - Email sin @ es rechazado")
    void emailInvalido() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerName":"Ana Prueba","customerEmail":"ana.example.com","customerType":"REGULAR",
                         "street":"Calle Falsa 123","city":"San Nicolas","zipCode":"2900",
                         "items":[{"productId":1,"quantity":1}]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El email del cliente es invalido"));
    }

    @Test
    @DisplayName("C10 - Cantidad cero es rechazada")
    void cantidadCero() throws Exception {
        crearPedido("REGULAR", "null", item(1, 0))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La cantidad debe ser mayor a cero"));
    }

    @Test
    @DisplayName("C11 - Producto inexistente es rechazado")
    void productoInexistente() throws Exception {
        crearPedido("REGULAR", "null", item(99, 1))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Producto inexistente: 99"));
    }

    // ---------------------------------------------------------------- stock

    @Test
    @DisplayName("C12 - Confirmar un pedido descuenta el stock")
    void confirmarDescuentaStock() throws Exception {
        crearPedido("REGULAR", "null", item(1, 2)).andExpect(status().isOk());
        mvc.perform(get("/api/products"))
                .andExpect(jsonPath("$[0].stock").value(18));
    }

    @Test
    @DisplayName("C13 - Stock insuficiente es rechazado con mensaje")
    void stockInsuficiente() throws Exception {
        crearPedido("REGULAR", "null", item(5, 4))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Stock insuficiente para Pizza muzzarella"));
    }

    @Test
    @DisplayName("C14 - Pedido rechazado por stock no descuenta el stock de ningun item (cambio deliberado D1)")
    void stockDeItemsPreviosEnPedidoRechazado() throws Exception {
        crearPedido("REGULAR", "null", item(1, 1), item(5, 99))
                .andExpect(status().isBadRequest());
        // D1: la reserva es atomica. En la version base la hamburguesa quedaba descontada (20 -> 19).
        mvc.perform(get("/api/products"))
                .andExpect(jsonPath("$[0].stock").value(20));
    }

    // ---------------------------------------------------------------- notificaciones

    @Test
    @DisplayName("C15 - Confirmar notifica por email y por WhatsApp si hay telefono")
    void confirmarNotificaEmailYWhatsApp(CapturedOutput output) throws Exception {
        crearPedido("REGULAR", "\"+5490000000002\"", item(1, 1)).andExpect(status().isOk());
        assertThat(output.getOut())
                .contains("[EMAIL] Para: ana@example.com | Asunto: Pedido confirmado | Hola Ana Prueba, tu pedido #1 fue confirmado. Total: $18440.0")
                .contains("[WHATSAPP] Para: +5490000000002 | PlatoYa: tu pedido #1 fue confirmado. Total: $18440.0");
    }

    @Test
    @DisplayName("C16 - Sin telefono no se envia WhatsApp")
    void sinTelefonoNoHayWhatsApp(CapturedOutput output) throws Exception {
        crearPedido("REGULAR", "null", item(1, 1)).andExpect(status().isOk());
        assertThat(output.getOut()).contains("[EMAIL]").doesNotContain("[WHATSAPP]");
    }

    // ---------------------------------------------------------------- ciclo de vida

    @Test
    @DisplayName("C17 - Cancelar repone stock, cambia estado y notifica por email")
    void cancelarReponeStock(CapturedOutput output) throws Exception {
        crearPedido("REGULAR", "null", item(1, 2)).andExpect(status().isOk());
        mvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
        mvc.perform(get("/api/products")).andExpect(jsonPath("$[0].stock").value(20));
        assertThat(output.getOut())
                .contains("[EMAIL] Para: ana@example.com | Asunto: Pedido cancelado | Hola Ana Prueba, tu pedido #1 fue cancelado.");
    }

    @Test
    @DisplayName("C18 - No se puede cancelar dos veces")
    void cancelarDosVeces() throws Exception {
        crearPedido("REGULAR", "null", item(1, 1)).andExpect(status().isOk());
        mvc.perform(post("/api/orders/1/cancel")).andExpect(status().isOk());
        mvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Solo se pueden cancelar pedidos confirmados"));
    }

    @Test
    @DisplayName("C19 - Entregar cambia estado y avisa por WhatsApp; luego no se puede cancelar")
    void entregarYLuegoCancelar(CapturedOutput output) throws Exception {
        crearPedido("REGULAR", "\"+5490000000002\"", item(1, 1)).andExpect(status().isOk());
        mvc.perform(post("/api/orders/1/deliver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGADO"));
        assertThat(output.getOut())
                .contains("[WHATSAPP] Para: +5490000000002 | PlatoYa: tu pedido #1 fue entregado. Gracias!");
        mvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Solo se pueden cancelar pedidos confirmados"));
    }

    @Test
    @DisplayName("C20 - Cancelar un pedido inexistente responde 404 (cambio deliberado D2)")
    void cancelarInexistente() throws Exception {
        mvc.perform(post("/api/orders/99/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Pedido no encontrado: 99"));
    }

    // ---------------------------------------------------------------- consultas y reporte

    @Test
    @DisplayName("C21 - Consultar un pedido existente lo devuelve completo")
    void consultarExistente() throws Exception {
        crearPedido("VIP", "null", item(1, 2), item(3, 1)).andExpect(status().isOk());
        mvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].productName").value("Hamburguesa completa"))
                .andExpect(jsonPath("$.total", closeTo(34303.5, CENTAVO)));
    }

    @Test
    @DisplayName("C22 - Consultar un pedido inexistente responde 404 con mensaje (cambio deliberado D2)")
    void consultarInexistente() throws Exception {
        // D2: en la version base respondia 200 con cuerpo vacio.
        mvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Pedido no encontrado: 99"));
    }

    @Test
    @DisplayName("C23 - Listar filtra por estado")
    void listarPorEstado() throws Exception {
        crearPedido("REGULAR", "null", item(1, 1)).andExpect(status().isOk());
        crearPedido("REGULAR", "null", item(2, 1)).andExpect(status().isOk());
        mvc.perform(post("/api/orders/1/cancel")).andExpect(status().isOk());
        mvc.perform(get("/api/orders")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/orders").param("status", "CANCELADO"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("C24 - Reporte: cuenta por estado y factura solo confirmados y entregados")
    void reporte() throws Exception {
        crearPedido("REGULAR", "null", item(1, 1)).andExpect(status().isOk());      // 18440
        crearPedido("REGULAR", "null", item(1, 1), item(2, 2)).andExpect(status().isOk()); // 32670
        crearPedido("VIP", "null", item(3, 1)).andExpect(status().isOk());          // 3811.5
        mvc.perform(post("/api/orders/2/deliver")).andExpect(status().isOk());
        mvc.perform(post("/api/orders/3/cancel")).andExpect(status().isOk());
        mvc.perform(get("/api/orders/report"))
                .andExpect(jsonPath("$.cantidadPedidos").value(3))
                .andExpect(jsonPath("$.confirmados").value(1))
                .andExpect(jsonPath("$.entregados").value(1))
                .andExpect(jsonPath("$.cancelados").value(1))
                .andExpect(jsonPath("$.totalFacturado", closeTo(51110.0, CENTAVO)))
                .andExpect(jsonPath("$.ticketPromedio", closeTo(25555.0, CENTAVO)));
    }

    // ---------------------------------------------------------------- helpers

    private ResultActions crearPedido(String tipoCliente, String telefonoJson, String... items) throws Exception {
        String body = """
                {"customerName":"Ana Prueba","customerEmail":"ana@example.com","customerPhone":%s,
                 "customerType":"%s","street":"Calle Falsa 123","city":"San Nicolas","zipCode":"2900",
                 "items":[%s]}""".formatted(telefonoJson, tipoCliente, String.join(",", items));
        return mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static String item(long productId, int quantity) {
        return "{\"productId\":%d,\"quantity\":%d}".formatted(productId, quantity);
    }
}
