package com.bridgelabz.jms.entity;

import com.bridgelabz.jms.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private String message;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void beforeInsert() {
        createdAt = LocalDateTime.now();
    }
}