package com.medix.orders.repo;

import com.medix.orders.domain.Order;
import com.medix.orders.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerId(String customerId);
}
