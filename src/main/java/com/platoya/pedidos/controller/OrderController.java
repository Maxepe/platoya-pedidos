package com.platoya.pedidos.controller;

import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.service.OrderProcessor;
import com.platoya.pedidos.service.OrderReportService;
import com.platoya.pedidos.service.SalesReport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderProcessor orderProcessor;
    private final OrderReportService reportService;

    public OrderController(OrderProcessor orderProcessor, OrderReportService reportService) {
        this.orderProcessor = orderProcessor;
        this.reportService = reportService;
    }

    @PostMapping
    public Order create(@RequestBody OrderRequestDto body) {
        return orderProcessor.createOrder(body.toOrderRequest());
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return orderProcessor.getOrder(id);
    }

    @GetMapping
    public List<Order> list(@RequestParam(required = false) String status) {
        return orderProcessor.listOrders(status);
    }

    @PostMapping("/{id}/cancel")
    public Order cancel(@PathVariable Long id) {
        return orderProcessor.cancelOrder(id);
    }

    @PostMapping("/{id}/deliver")
    public Order deliver(@PathVariable Long id) {
        return orderProcessor.deliverOrder(id);
    }

    @GetMapping("/report")
    public SalesReport report() {
        return reportService.buildSalesReport();
    }
}
