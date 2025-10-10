package com.matsuzaka.foodtiger.dto;

import com.matsuzaka.foodtiger.dao.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "訂單 ID 不能為空")
    private Long orderrId;

    @Min(value = 1, message = "支付金額必須大於 0")
    private Integer amount;

    @NotNull(message = "支付方式不能為空")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "交易 ID 不能為空")
    private String transactionId;
}
