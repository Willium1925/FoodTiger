package com.matsuzaka.foodtiger.controller;

import com.matsuzaka.foodtiger.config.security.CustomUserDetails;
import com.matsuzaka.foodtiger.dao.entity.Orderr;
import com.matsuzaka.foodtiger.dao.entity.Payment;
import com.matsuzaka.foodtiger.dao.entity.PaymentStatus;
import com.matsuzaka.foodtiger.dto.PaymentRequest;
import com.matsuzaka.foodtiger.exception.InvalidOperationException;
import com.matsuzaka.foodtiger.exception.PaymentFailedException;
import com.matsuzaka.foodtiger.exception.ResourceNotFoundException;
import com.matsuzaka.foodtiger.exception.UnauthorizedException;
import com.matsuzaka.foodtiger.service.OrderrService; // Import OrderrService
import com.matsuzaka.foodtiger.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderrService orderrService; // Autowire OrderrService

    // 允許ADMIN 獲取所有支付記錄
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getAllPayments() {
        logger.info("收到獲取所有支付記錄的請求 (由 ADMIN 執行)");
        List<Payment> payments = paymentService.findAllPayments();
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    // 允許 CUSTOMER 獲取自己的支付記錄，ADMIN 獲取任何支付記錄
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('CUSTOMER') and @paymentService.findPaymentById(#id).orElse(null)?.orderr?.user?.id == authentication.principal.id)")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        logger.info("收到獲取 ID 為 {} 支付記錄的請求", id);
        return paymentService.findPaymentById(id)
                .map(payment -> {
                    logger.info("成功獲取 ID 為 {} 的支付記錄", id);
                    return new ResponseEntity<>(payment, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("未找到 ID 為 {} 的支付記錄", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // 移除原有的 createPayment 方法，因為業務邏輯創建已由 /api/payments/process 處理
    // @PostMapping
    // public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
    //     logger.info("收到直接創建支付實體的請求 (不推薦用於業務邏輯)");
    //     Payment savedPayment = paymentService.savePayment(payment);
    //     logger.info("支付記錄 ID {} 創建成功", savedPayment.getId());
    //     return new ResponseEntity<>(savedPayment, HttpStatus.CREATED);
    // }

    // 允許 ADMIN 更新任何支付記錄
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
        logger.info("收到更新 ID 為 {} 支付記錄的請求 (由 ADMIN 執行)", id);
        return paymentService.findPaymentById(id)
                .map(existingPayment -> {
                    existingPayment.setOrderr(payment.getOrderr());
                    existingPayment.setAmount(payment.getAmount());
                    existingPayment.setPaymentMethod(payment.getPaymentMethod());
                    existingPayment.setTransactionId(payment.getTransactionId());
                    existingPayment.setStatus(payment.getStatus());
                    Payment updatedPayment = paymentService.savePayment(existingPayment);
                    logger.info("支付記錄 ID {} 更新成功", id);
                    return new ResponseEntity<>(updatedPayment, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("嘗試更新不存在的支付記錄 ID {}", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // 允許 ADMIN 刪除任何支付記錄
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        logger.warn("收到刪除 ID 為 {} 支付記錄的請求 (由 ADMIN 執行)", id);
        if (paymentService.findPaymentById(id).isPresent()) {
            paymentService.deletePayment(id);
            logger.info("支付記錄 ID {} 刪除成功", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        logger.warn("嘗試刪除不存在的支付記錄 ID {}", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // 允許 CUSTOMER 獲取自己訂單的支付記錄，ADMIN 獲取任何訂單的支付記錄
    @GetMapping("/orderr/{orderrId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('CUSTOMER') and @orderrService.findOrderrById(#orderrId).orElse(null)?.user?.id == authentication.principal.id)")
    public ResponseEntity<Payment> getPaymentByOrderrId(@PathVariable Long orderrId) {
        logger.info("收到獲取訂單 ID 為 {} 支付記錄的請求", orderrId);
        return paymentService.findPaymentByOrderrId(orderrId)
                .map(payment -> {
                    logger.info("成功獲取訂單 ID 為 {} 的支付記錄", orderrId);
                    return new ResponseEntity<>(payment, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("未找到訂單 ID 為 {} 的支付記錄", orderrId);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // 允許 ADMIN 獲取任何狀態的支付記錄
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        logger.info("收到獲取狀態為 {} 支付記錄的請求 (由 ADMIN 執行)", status);
        List<Payment> payments = paymentService.findPaymentsByStatus(status);
        if (payments.isEmpty()) {
            logger.warn("未找到狀態為 {} 的支付記錄", status);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.info("成功獲取狀態為 {} 的 {} 筆支付記錄", status, payments.size());
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    // 允許 ADMIN 獲取任何交易 ID 的支付記錄
    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        logger.info("收到獲取交易 ID 為 {} 支付記錄的請求 (由 ADMIN 執行)", transactionId);
        return paymentService.findPaymentByTransactionId(transactionId)
                .map(payment -> {
                    logger.info("成功獲取交易 ID 為 {} 的支付記錄", transactionId);
                    return new ResponseEntity<>(payment, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("未找到交易 ID 為 {} 的支付記錄", transactionId);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    /**
     * 處理支付的 API 端點。
     * 僅限 CUSTOMER 角色。
     *
     * @param request 包含支付詳細資訊的 DTO
     * @return 處理後的支付實體和 CREATED 狀態
     * @throws ResourceNotFoundException 如果訂單不存在
     * @throws InvalidOperationException 如果訂單已支付
     * @throws PaymentFailedException 如果支付處理失敗
     */
    @PostMapping("/process")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Payment> processPayment(@Valid @RequestBody PaymentRequest request)
            throws ResourceNotFoundException, InvalidOperationException, PaymentFailedException, UnauthorizedException {
        logger.info("收到處理支付請求，訂單 ID: {}, 金額: {}", request.getOrderrId(), request.getAmount());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        // 確保支付是為當前認證用戶的訂單處理的
        Orderr orderr = orderrService.findOrderrById(request.getOrderrId())
                .orElseThrow(() -> new ResourceNotFoundException("訂單 ID " + request.getOrderrId() + " 未找到"));
        if (!orderr.getUser().getId().equals(currentUser.getId())) {
            logger.warn("用戶 ID {} 嘗試為非自己的訂單 ID {} 處理支付", currentUser.getId(), request.getOrderrId());
            throw new UnauthorizedException("您無權為此訂單處理支付");
        }

        Payment processedPayment = paymentService.processPayment(request);
        logger.info("訂單 ID {} 的支付處理完成，狀態為 {}", request.getOrderrId(), processedPayment.getStatus());
        return new ResponseEntity<>(processedPayment, HttpStatus.CREATED);
    }
}
