package com.matsuzaka.foodtiger.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryAssignmentRequest {
    @NotNull(message = "外送員 ID 不能為空")
    private Long deliveryPersonId;
}
