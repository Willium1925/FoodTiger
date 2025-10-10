package com.matsuzaka.foodtiger.service.impl;

import com.matsuzaka.foodtiger.dao.entity.Orderr;
import com.matsuzaka.foodtiger.dao.entity.Payment;
import com.matsuzaka.foodtiger.dao.entity.PaymentStatus;
import com.matsuzaka.foodtiger.dao.repository.OrderrRepository;
import com.matsuzaka.foodtiger.dao.repository.PaymentRepository;
import com.matsuzaka.foodtiger.dto.PaymentRequest;
import com.matsuzaka.foodtiger.exception.InvalidOperationException;
import com.matsuzaka.foodtiger.exception.PaymentFailedException;
import com.matsuzaka.foodtiger.exception.ResourceNotFoundException;
import com.matsuzaka.foodtiger.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderrRepository orderrRepository; // Need OrderrRepository to check order status

    @Override
    public List<Payment> findAllPayments() {
        logger.info("正在查詢所有支付記錄");
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> findPaymentById(Long id) {
        logger.info("正在查詢 ID 為 {} 的支付記錄", id);
        return paymentRepository.findById(id);
    }

    @Override
    public Payment savePayment(Payment payment) {
        logger.info("正在保存支付記錄 ID: {}", payment.getId());
        return paymentRepository.save(payment);
    }

    @Override
    public void deletePayment(Long id) {
        logger.warn("正在刪除 ID 為 {} 的支付記錄", id);
        paymentRepository.deleteById(id);
    }

    @Override
    public Optional<Payment> findPaymentByOrderrId(Long orderrId) {
        logger.info("正在查詢訂單 ID 為 {} 的支付記錄", orderrId);
        return paymentRepository.findByOrderrId(orderrId);
    }

    @Override
    public List<Payment> findPaymentsByStatus(PaymentStatus status) {
        logger.info("正在查詢狀態為 {} 的支付記錄", status);
        return paymentRepository.findByStatus(status);
    }

    @Override
    public Optional<Payment> findPaymentByTransactionId(String transactionId) {
        logger.info("正在查詢交易 ID 為 {} 的支付記錄", transactionId);
        return paymentRepository.findByTransactionId(transactionId);
    }

    /**
     * 處理支付請求。
     * 驗證訂單是否存在且未支付。
     * 模擬支付處理，並根據結果更新支付狀態。
     *
     * @param request 包含支付詳細資訊的 DTO
     * @return 處理後的支付實體
     * @throws ResourceNotFoundException 如果訂單不存在
     * @throws InvalidOperationException 如果訂單已支付
     * @throws PaymentFailedException 如果支付處理失敗
     */
    @Override
    @Transactional
    public Payment processPayment(PaymentRequest request) throws ResourceNotFoundException, InvalidOperationException, PaymentFailedException {
        logger.info("收到處理訂單 ID {} 的支付請求，金額: {}", request.getOrderrId(), request.getAmount());

        // 1. 驗證訂單是否存在
        Orderr orderr = orderrRepository.findById(request.getOrderrId())
                .orElseThrow(() -> {
                    logger.warn("支付處理失敗：訂單 ID {} 未找到", request.getOrderrId());
                    return new ResourceNotFoundException("訂單 ID " + request.getOrderrId() + " 未找到");
                });

        // 2. 檢查訂單是否已支付
        if (paymentRepository.findByOrderrId(orderr.getId()).isPresent()) {
            logger.warn("支付處理失敗：訂單 ID {} 已存在支付記錄", orderr.getId());
            throw new InvalidOperationException("訂單 ID " + orderr.getId() + " 已存在支付記錄");
        }

        // 3. 創建支付實體
        Payment newPayment = new Payment();
        newPayment.setOrderr(orderr);
        newPayment.setAmount(request.getAmount());
        newPayment.setPaymentMethod(request.getPaymentMethod());
        newPayment.setTransactionId(request.getTransactionId());
        newPayment.setStatus(PaymentStatus.處理中); // 初始狀態

        // 模擬支付處理邏輯
        boolean paymentSuccessful = simulatePaymentGateway(request.getAmount(), request.getTransactionId());

        if (paymentSuccessful) {
            newPayment.setStatus(PaymentStatus.付款成功);
            logger.info("訂單 ID {} 的支付成功，交易 ID: {}", orderr.getId(), request.getTransactionId());
        } else {
            newPayment.setStatus(PaymentStatus.付款失敗);
            logger.error("訂單 ID {} 的支付失敗，交易 ID: {}", orderr.getId(), request.getTransactionId());
            throw new PaymentFailedException("支付處理失敗，交易 ID: " + request.getTransactionId());
        }

        return paymentRepository.save(newPayment);
    }

    /**
     * 模擬支付閘道處理。
     * 實際應用中會調用第三方支付服務。
     *
     * @param amount 支付金額
     * @param transactionId 交易 ID
     * @return 支付是否成功
     */
    private boolean simulatePaymentGateway(Integer amount, String transactionId) {
        logger.info("模擬支付閘道處理：金額 {}，交易 ID {}", amount, transactionId);
        // 簡單模擬：如果金額是偶數，則支付成功；否則失敗
        return amount % 2 == 0;
    }
}