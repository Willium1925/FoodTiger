package com.matsuzaka.foodtiger.dto;

import com.matsuzaka.foodtiger.dao.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    @NotNull(message = "新的訂單狀態不能為空")
    private OrderStatus newStatus;
}
