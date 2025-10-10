package com.matsuzaka.foodtiger.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "菜單項目 ID 不能為空")
    private Long menuItemId;

    @Min(value = 1, message = "數量必須至少為 1")
    private Integer quantity;
}
