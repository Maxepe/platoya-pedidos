package com.platoya.pedidos.repository;

import com.platoya.pedidos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
