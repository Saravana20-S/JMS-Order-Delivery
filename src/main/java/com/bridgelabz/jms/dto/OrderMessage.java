package com.bridgelabz.jms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMessage {

    private Long orderId;

    private Long customerId;

    private Long productId;

    private Integer quantity;

    private String correlationId;
}