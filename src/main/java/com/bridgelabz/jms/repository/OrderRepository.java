package com.bridgelabz.jms.repository;

import com.bridgelabz.jms.entity.Order;
import com.bridgelabz.jms.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);
}