package com.matsuzaka.foodtiger.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "用戶 ID 不能為空")
    private Long userId;

    @NotNull(message = "餐廳 ID 不能為空")
    private Long restaurantId;

    @NotNull(message = "送貨地址 ID 不能為空")
    private Long deliveryAddressId;

    @Valid
    @Size(min = 1, message = "訂單中必須至少包含一個菜單項目")
    private List<OrderItemRequest> items;
}
