package com.platoya.pedidos.controller;

import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.service.OrderProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderProcessor processor;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody OrderRequestDto r) {
        try {
            Order o = processor.processOrder(r.getCustomerName(), r.getCustomerEmail(), r.getCustomerPhone(),
                    r.getCustomerType(), r.getStreet(), r.getCity(), r.getZipCode(), r.getItems());
            return ResponseEntity.ok(o);
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
    public Map<String, Object> report() {
        return processor.report();
    }
}
