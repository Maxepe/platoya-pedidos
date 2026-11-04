package com.platoya.pedidos.service;

import com.platoya.pedidos.controller.OrderRequestDto;
import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.model.OrderItem;
import com.platoya.pedidos.model.Product;
import com.platoya.pedidos.notification.EmailSender;
import com.platoya.pedidos.notification.WhatsAppService;
import com.platoya.pedidos.repository.OrderRepository;
import com.platoya.pedidos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderProcessor {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ProductRepository prodRepo;

    public Order processOrder(String customerName, String customerEmail, String customerPhone,
                              String customerType, String street, String city, String zipCode,
                              List<OrderRequestDto.ItemDto> items) {
        // validaciones
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new RuntimeException("El nombre del cliente es obligatorio");
        }
        if (customerEmail == null || customerEmail.trim().isEmpty() || !customerEmail.contains("@")) {
            throw new RuntimeException("El email del cliente es invalido");
        }
        if (street == null || street.trim().isEmpty() || city == null || city.trim().isEmpty()) {
            throw new RuntimeException("La direccion de entrega es obligatoria");
        }
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("El pedido no tiene items");
        }

        Order o = new Order();
        o.setCustomerName(customerName);
        o.setCustomerEmail(customerEmail);
        o.setCustomerPhone(customerPhone);
        o.setCustomerType(customerType);
        o.setStreet(street);
        o.setCity(city);
        o.setZipCode(zipCode);

        // recorrer items, validar stock y calcular
        double subtotal = 0;
        List<OrderItem> lista = new ArrayList<>();
        for (OrderRequestDto.ItemDto it : items) {
            if (it == null || it.getProductId() == null) {
                throw new RuntimeException("Item invalido");
            }
            if (it.getQuantity() <= 0) {
                throw new RuntimeException("La cantidad debe ser mayor a cero");
            }
            Optional<Product> op = prodRepo.findById(it.getProductId());
            if (!op.isPresent()) {
                throw new RuntimeException("Producto inexistente: " + it.getProductId());
            }
            Product p = op.get();
            if (p.getStock() < it.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para " + p.getName());
            }
            p.setStock(p.getStock() - it.getQuantity());
            prodRepo.save(p);

            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setUnitPrice(p.getPrice());
            oi.setQuantity(it.getQuantity());
            lista.add(oi);
            subtotal = subtotal + p.getPrice() * it.getQuantity();
        }
        o.setItems(lista);

        // descuentos
        double d = 0;
        if (customerType != null && customerType.equals("VIP")) {
            d = subtotal * 0.10;
        } else if (customerType != null && customerType.equals("EMPLEADO")) {
            d = subtotal * 0.20;
        }

        // impuestos y envio
        double tmp = subtotal - d;
        double iva = tmp * 0.21;
        double envio = 0;
        if (customerType != null && customerType.equals("VIP")) {
            envio = 0;
        } else if (tmp < 20000) {
            envio = 1500;
        }
        double total = tmp + iva + envio;
        total = Math.round(total * 100.0) / 100.0;

        o.setSubtotal(subtotal);
        o.setDiscount(d);
        o.setTax(iva);
        o.setShippingCost(envio);
        o.setTotal(total);
        o.setStatus("CONFIRMADO");
        o.setCreatedAt(LocalDateTime.now());
        orderRepo.save(o);

        // notificaciones
        EmailSender email = new EmailSender();
        email.send(customerEmail, "Pedido confirmado",
                "Hola " + customerName + ", tu pedido #" + o.getId() + " fue confirmado. Total: $" + total);
        if (customerPhone != null && !customerPhone.trim().isEmpty()) {
            WhatsAppService wa = new WhatsAppService();
            wa.sendMessage(customerPhone, "PlatoYa: tu pedido #" + o.getId() + " fue confirmado. Total: $" + total);
        }
        return o;
    }

    public Order getOrder(Long id) {
        return orderRepo.findById(id).orElse(null);
    }

    public List<Order> listOrders(String status) {
        List<Order> all = orderRepo.findAll();
        if (status == null || status.trim().isEmpty()) {
            return all;
        }
        List<Order> res = new ArrayList<>();
        for (Order x : all) {
            if (x.getStatus().equals(status)) {
                res.add(x);
            }
        }
        return res;
    }

    public Order cancelOrder(Long id) {
        if (id == null) {
            throw new RuntimeException("Id invalido");
        }
        Optional<Order> oo = orderRepo.findById(id);
        if (!oo.isPresent()) {
            throw new RuntimeException("Pedido no encontrado: " + id);
        }
        Order o = oo.get();
        if (!o.getStatus().equals("CONFIRMADO")) {
            throw new RuntimeException("Solo se pueden cancelar pedidos confirmados");
        }
        for (OrderItem oi : o.getItems()) {
            Product p = prodRepo.findById(oi.getProductId()).get();
            p.setStock(p.getStock() + oi.getQuantity());
            prodRepo.save(p);
        }
        o.setStatus("CANCELADO");
        orderRepo.save(o);

        EmailSender email = new EmailSender();
        email.send(o.getCustomerEmail(), "Pedido cancelado",
                "Hola " + o.getCustomerName() + ", tu pedido #" + o.getId() + " fue cancelado.");
        return o;
    }

    public Order deliverOrder(Long id) {
        if (id == null) {
            throw new RuntimeException("Id invalido");
        }
        Optional<Order> oo = orderRepo.findById(id);
        if (!oo.isPresent()) {
            throw new RuntimeException("Pedido no encontrado: " + id);
        }
        Order o = oo.get();
        if (!o.getStatus().equals("CONFIRMADO")) {
            throw new RuntimeException("Solo se pueden entregar pedidos confirmados");
        }
        o.setStatus("ENTREGADO");
        orderRepo.save(o);

        if (o.getCustomerPhone() != null && !o.getCustomerPhone().trim().isEmpty()) {
            WhatsAppService wa = new WhatsAppService();
            wa.sendMessage(o.getCustomerPhone(), "PlatoYa: tu pedido #" + o.getId() + " fue entregado. Gracias!");
        }
        return o;
    }

    public Map<String, Object> report() {
        List<Order> all = orderRepo.findAll();
        int c1 = 0;
        int c2 = 0;
        int c3 = 0;
        double fact = 0;
        for (Order x : all) {
            if (x.getStatus().equals("CONFIRMADO")) {
                c1++;
                fact = fact + x.getTotal();
            } else if (x.getStatus().equals("ENTREGADO")) {
                c2++;
                fact = fact + x.getTotal();
            } else if (x.getStatus().equals("CANCELADO")) {
                c3++;
            }
        }
        Map<String, Object> r = new HashMap<>();
        r.put("cantidadPedidos", all.size());
        r.put("confirmados", c1);
        r.put("entregados", c2);
        r.put("cancelados", c3);
        r.put("totalFacturado", Math.round(fact * 100.0) / 100.0);
        if (c1 + c2 > 0) {
            r.put("ticketPromedio", Math.round(fact / (c1 + c2) * 100.0) / 100.0);
        } else {
            r.put("ticketPromedio", 0.0);
        }
        return r;
    }
}
