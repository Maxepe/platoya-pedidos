package com.platoya.pedidos.repository;

import com.platoya.pedidos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
