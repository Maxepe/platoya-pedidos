package com.platoya.pedidos.service;

/** Los nombres de los componentes son el contrato JSON del endpoint /api/orders/report. */
public record SalesReport(int cantidadPedidos, long confirmados, long entregados, long cancelados,
                          double totalFacturado, double ticketPromedio) {
}
