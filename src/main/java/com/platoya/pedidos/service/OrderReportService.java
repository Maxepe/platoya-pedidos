package com.platoya.pedidos.service;

import com.platoya.pedidos.model.Order;
import com.platoya.pedidos.model.OrderStatus;
import com.platoya.pedidos.pricing.Amounts;
import com.platoya.pedidos.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderReportService {

    private final OrderRepository orderRepository;

    public OrderReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public SalesReport buildSalesReport() {
        List<Order> orders = orderRepository.findAll();
        long confirmed = countByStatus(orders, OrderStatus.CONFIRMADO);
        long delivered = countByStatus(orders, OrderStatus.ENTREGADO);
        long cancelled = countByStatus(orders, OrderStatus.CANCELADO);
        double billedAmount = sumBilledTotals(orders);
        return new SalesReport(orders.size(), confirmed, delivered, cancelled,
                Amounts.roundToCents(billedAmount), averageTicket(billedAmount, confirmed + delivered));
    }

    private long countByStatus(List<Order> orders, OrderStatus status) {
        return orders.stream().filter(order -> order.getStatus() == status).count();
    }

    private double sumBilledTotals(List<Order> orders) {
        return orders.stream()
                .filter(Order::isBilled)
                .mapToDouble(Order::getTotal)
                .reduce(0, Double::sum);
    }

    private double averageTicket(double billedAmount, long billedOrders) {
        if (billedOrders == 0) {
            return 0.0;
        }
        return Amounts.roundToCents(billedAmount / billedOrders);
    }
}
