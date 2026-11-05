package com.platoya.pedidos.controller;

import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.service.OrderProcessor;
import com.platoya.pedidos.service.OrderReportService;
import com.platoya.pedidos.service.SalesReport;
import org.springframework.http.ResponseEntity;
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

    private final OrderProcessor processor;
    private final OrderReportService reportService;

    public OrderController(OrderProcessor processor, OrderReportService reportService) {
        this.processor = processor;
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody OrderRequestDto r) {
        try {
            return ResponseEntity.ok(processor.createOrder(r.toOrderRequest()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        return ResponseEntity.ok(processor.getOrder(id));
    }

    @GetMapping
    public List<Order> list(@RequestParam(required = false) String status) {
        return processor.listOrders(status);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(processor.cancelOrder(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<?> deliver(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(processor.deliverOrder(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/report")
    public SalesReport report() {
        return reportService.buildSalesReport();
    }
}
