package com.matsuzaka.foodtiger.service.impl;

import com.matsuzaka.foodtiger.dao.entity.*;
import com.matsuzaka.foodtiger.dao.repository.*;
import com.matsuzaka.foodtiger.dto.DeliveryAssignmentRequest;
import com.matsuzaka.foodtiger.dto.OrderItemRequest;
import com.matsuzaka.foodtiger.dto.OrderRequest;
import com.matsuzaka.foodtiger.dto.OrderStatusUpdateRequest;
import com.matsuzaka.foodtiger.exception.InvalidOperationException;
import com.matsuzaka.foodtiger.exception.InvalidOrderStatusTransitionException;
import com.matsuzaka.foodtiger.exception.MenuItemUnavailableException;
import com.matsuzaka.foodtiger.exception.ResourceNotFoundException;
import com.matsuzaka.foodtiger.exception.UnauthorizedException;
import com.matsuzaka.foodtiger.service.OrderrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderrServiceImpl implements OrderrService {

    private static final Logger logger = LoggerFactory.getLogger(OrderrServiceImpl.class);

    @Autowired
    private OrderrRepository orderrRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private MenuItemRepository menuItemRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public List<Orderr> findAllOrderrs() {
        logger.info("正在查詢所有訂單");
        return orderrRepository.findAll();
    }

    @Override
    public Optional<Orderr> findOrderrById(Long id) {
        logger.info("正在查詢 ID 為 {} 的訂單", id);
        return orderrRepository.findById(id);
    }

    @Override
    public Orderr saveOrderr(Orderr orderr) {
        logger.info("正在保存訂單 ID: {}", orderr.getId());
        return orderrRepository.save(orderr);
    }

    @Override
    public void deleteOrderr(Long id) {
        logger.warn("正在刪除 ID 為 {} 的訂單", id);
        orderrRepository.deleteById(id);
    }

    @Override
    public List<Orderr> findOrderrsByUserId(Long userId) {
        logger.info("正在查詢用戶 ID 為 {} 的訂單", userId);
        return orderrRepository.findByUserId(userId);
    }

    @Override
    public List<Orderr> findOrderrsByRestaurantId(Long restaurantId) {
        logger.info("正在查詢餐廳 ID 為 {} 的訂單", restaurantId);
        return orderrRepository.findByRestaurantId(restaurantId);
    }

    @Override
    public List<Orderr> findOrderrsByDeliveryPersonId(Long deliveryPersonId) {
        logger.info("正在查詢外送員 ID 為 {} 的訂單", deliveryPersonId);
        return orderrRepository.findByDeliveryPersonId(deliveryPersonId);
    }

    @Override
    public List<Orderr> findOrderrsByStatus(OrderStatus status) {
        logger.info("正在查詢狀態為 {} 的訂單", status);
        return orderrRepository.findByStatus(status);
    }

    /**
     * 創建新訂單。
     * 驗證用戶、餐廳、地址和菜單項目的存在和可用性。
     * 計算訂單總金額並設置初始狀態。
     *
     * @param orderRequest 包含訂單詳細資訊的 DTO
     * @return 創建成功的訂單實體
     * @throws ResourceNotFoundException 如果用戶、餐廳、地址或菜單項目不存在
     * @throws MenuItemUnavailableException 如果菜單項目不可用
     */
    @Override
    @Transactional // 確保訂單創建和訂單項目保存的原子性
    public Orderr createOrder(OrderRequest orderRequest) throws ResourceNotFoundException, MenuItemUnavailableException {
        logger.info("收到創建新訂單的請求，用戶 ID: {}, 餐廳 ID: {}", orderRequest.getUserId(), orderRequest.getRestaurantId());

        // 1. 驗證用戶
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> {
                    logger.warn("訂單創建失敗：用戶 ID {} 未找到", orderRequest.getUserId());
                    return new ResourceNotFoundException("用戶 ID " + orderRequest.getUserId() + " 未找到");
                });

        // 2. 驗證餐廳
        Restaurant restaurant = restaurantRepository.findById(orderRequest.getRestaurantId())
                .orElseThrow(() -> {
                    logger.warn("訂單創建失敗：餐廳 ID {} 未找到", orderRequest.getRestaurantId());
                    return new ResourceNotFoundException("餐廳 ID " + orderRequest.getRestaurantId() + " 未找到");
                });

        // 3. 驗證送貨地址
        Address deliveryAddress = addressRepository.findById(orderRequest.getDeliveryAddressId())
                .orElseThrow(() -> {
                    logger.warn("訂單創建失敗：送貨地址 ID {} 未找到", orderRequest.getDeliveryAddressId());
                    return new ResourceNotFoundException("送貨地址 ID " + orderRequest.getDeliveryAddressId() + " 未找到");
                });

        // 創建訂單實體
        Orderr newOrderr = new Orderr();
        newOrderr.setUser(user);
        newOrderr.setRestaurant(restaurant);
        newOrderr.setDeliveryAddress(deliveryAddress);
        newOrderr.setStatus(OrderStatus.處理中); // 初始狀態為處理中
        newOrderr.setDeliveryFee(0); // 假設初始運費為 0，後續可能根據邏輯計算

        int totalAmount = 0;
        // 初始化訂單項目列表
        newOrderr.setOrderItems(new java.util.ArrayList<>());

        // 處理訂單項目
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> {
                        logger.warn("訂單創建失敗：菜單項目 ID {} 未找到", itemRequest.getMenuItemId());
                        return new ResourceNotFoundException("菜單項目 ID " + itemRequest.getMenuItemId() + " 未找到");
                    });

            // 檢查菜單項目是否屬於該餐廳
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                logger.warn("訂單創建失敗：菜單項目 ID {} 不屬於餐廳 ID {}", itemRequest.getMenuItemId(), restaurant.getId());
                throw new MenuItemUnavailableException("菜單項目 ID " + itemRequest.getMenuItemId() + " 不屬於餐廳 ID " + restaurant.getId());
            }

            // 檢查菜單項目是否可用
            if (!menuItem.getAvailable()) {
                logger.warn("訂單創建失敗：菜單項目 '{}' (ID: {}) 不可用", menuItem.getTitle(), menuItem.getId());
                throw new MenuItemUnavailableException("菜單項目 '" + menuItem.getTitle() + "' 不可用");
            }

            // 創建訂單項目實體
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderr(newOrderr); // 設置關聯的訂單
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtOrderr(menuItem.getPrice()); // 記錄下單時的價格

            totalAmount += orderItem.getQuantity() * orderItem.getPriceAtOrderr();
            newOrderr.getOrderItems().add(orderItem); // 將訂單項目添加到訂單中
        }

        newOrderr.setTotalAmount(totalAmount);
        Orderr savedOrderr = orderrRepository.save(newOrderr); // 保存訂單和所有關聯的訂單項目

        logger.info("新訂單 ID {} 創建成功，總金額為 {}", savedOrderr.getId(), savedOrderr.getTotalAmount());
        return savedOrderr;
    }

    /**
     * 更新訂單狀態。
     * 僅限餐廳擁有者可以更新其餐廳的訂單狀態。
     * 驗證訂單是否存在，以及狀態轉換是否有效。
     *
     * @param orderId 訂單 ID
     * @param request 包含新狀態的 DTO
     * @param restaurantOwnerId 執行操作的餐廳擁有者 ID
     * @return 更新後的訂單實體
     * @throws ResourceNotFoundException 如果訂單不存在
     * @throws InvalidOrderStatusTransitionException 如果狀態轉換無效
     * @throws UnauthorizedException 如果操作者不是該訂單所屬餐廳的擁有者
     */
    @Override
    @Transactional
    public Orderr updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, Long restaurantOwnerId)
            throws ResourceNotFoundException, InvalidOrderStatusTransitionException, UnauthorizedException {
        logger.info("嘗試更新訂單 ID {} 的狀態為 {}，操作者為餐廳擁有者 ID {}", orderId, request.getNewStatus(), restaurantOwnerId);

        Orderr orderr = orderrRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("更新訂單狀態失敗：訂單 ID {} 未找到", orderId);
                    return new ResourceNotFoundException("訂單 ID " + orderId + " 未找到");
                });

        // 驗證操作者是否為該訂單所屬餐廳的擁有者
        if (!orderr.getRestaurant().getOwner().getId().equals(restaurantOwnerId)) {
            logger.warn("更新訂單狀態失敗：餐廳擁有者 ID {} 無權修改訂單 ID {}，該訂單屬於餐廳擁有者 ID {}",
                    restaurantOwnerId, orderId, orderr.getRestaurant().getOwner().getId());
            throw new UnauthorizedException("您無權修改此訂單的狀態");
        }

        OrderStatus currentStatus = orderr.getStatus();
        OrderStatus newStatus = request.getNewStatus();

        // 驗證狀態轉換邏輯
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            logger.warn("更新訂單狀態失敗：訂單 ID {} 從 {} 到 {} 的狀態轉換無效", orderId, currentStatus, newStatus);
            throw new InvalidOrderStatusTransitionException("無效的訂單狀態轉換：從 " + currentStatus + " 到 " + newStatus);
        }

        orderr.setStatus(newStatus);
        Orderr updatedOrderr = orderrRepository.save(orderr);
        logger.info("訂單 ID {} 狀態成功更新為 {}", orderId, newStatus);
        return updatedOrderr;
    }

    /**
     * 指派外送員給訂單。
     * 僅限訂單狀態為「準備中」時可指派。
     *
     * @param orderId 訂單 ID
     * @param request 包含外送員 ID 的 DTO
     * @return 更新後的訂單實體
     * @throws ResourceNotFoundException 如果訂單或外送員不存在
     * @throws InvalidOperationException 如果訂單狀態不允許指派外送員，或訂單已被指派
     */
    @Override
    @Transactional
    public Orderr assignDeliveryPerson(Long orderId, DeliveryAssignmentRequest request)
            throws ResourceNotFoundException, InvalidOperationException {
        logger.info("嘗試指派外送員 ID {} 給訂單 ID {}", request.getDeliveryPersonId(), orderId);

        Orderr orderr = orderrRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("指派外送員失敗：訂單 ID {} 未找到", orderId);
                    return new ResourceNotFoundException("訂單 ID " + orderId + " 未找到");
                });

        User deliveryPerson = userRepository.findById(request.getDeliveryPersonId())
                .orElseThrow(() -> {
                    logger.warn("指派外送員失敗：外送員 ID {} 未找到", request.getDeliveryPersonId());
                    return new ResourceNotFoundException("外送員 ID " + request.getDeliveryPersonId() + " 未找到");
                });

        // 驗證外送員角色
        if (deliveryPerson.getRole() != Role.DELIVER) {
            logger.warn("指派外送員失敗：用戶 ID {} 不是外送員角色", request.getDeliveryPersonId());
            throw new InvalidOperationException("用戶 ID " + request.getDeliveryPersonId() + " 不是外送員角色");
        }

        // 檢查訂單狀態是否允許指派外送員 (例如，必須是「準備中」)
        if (orderr.getStatus() != OrderStatus.準備中) {
            logger.warn("指派外送員失敗：訂單 ID {} 狀態為 {}，不允許指派外送員", orderr.getId(), orderr.getStatus());
            throw new InvalidOperationException("訂單狀態為 " + orderr.getStatus() + "，不允許指派外送員");
        }

        // 檢查訂單是否已被指派外送員
        if (orderr.getDeliveryPerson() != null) {
            logger.warn("指派外送員失敗：訂單 ID {} 已被指派給外送員 ID {}", orderr.getId(), orderr.getDeliveryPerson().getId());
            throw new InvalidOperationException("訂單 ID " + orderr.getId() + " 已被指派給外送員");
        }

        orderr.setDeliveryPerson(deliveryPerson);
        Orderr updatedOrderr = orderrRepository.save(orderr);
        logger.info("訂單 ID {} 成功指派給外送員 ID {}", orderId, deliveryPerson.getId());
        return updatedOrderr;
    }

    /**
     * 外送員接受訂單。
     * 僅限訂單狀態為「準備中」且已指派給該外送員時可接受。
     * 接受後，訂單狀態變為「運送中」。
     *
     * @param orderId 訂單 ID
     * @param deliveryPersonId 接受訂單的外送員 ID
     * @return 更新後的訂單實體
     * @throws ResourceNotFoundException 如果訂單不存在
     * @throws UnauthorizedException 如果外送員無權接受此訂單
     * @throws InvalidOperationException 如果訂單狀態不允許接受
     */
    @Override
    @Transactional
    public Orderr acceptDelivery(Long orderId, Long deliveryPersonId)
            throws ResourceNotFoundException, UnauthorizedException, InvalidOperationException {
        logger.info("外送員 ID {} 嘗試接受訂單 ID {}", deliveryPersonId, orderId);

        Orderr orderr = orderrRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("接受訂單失敗：訂單 ID {} 未找到", orderId);
                    return new ResourceNotFoundException("訂單 ID " + orderId + " 未找到");
                });

        // 驗證訂單是否已指派給該外送員
        if (orderr.getDeliveryPerson() == null || !orderr.getDeliveryPerson().getId().equals(deliveryPersonId)) {
            logger.warn("接受訂單失敗：外送員 ID {} 無權接受訂單 ID {}，該訂單未指派給此外送員或未指派", deliveryPersonId, orderId);
            throw new UnauthorizedException("您無權接受此訂單");
        }

        // 驗證訂單狀態是否允許接受 (例如，必須是「準備中」)
        if (orderr.getStatus() != OrderStatus.準備中) {
            logger.warn("接受訂單失敗：訂單 ID {} 狀態為 {}，不允許接受", orderr.getId(), orderr.getStatus());
            throw new InvalidOperationException("訂單狀態為 " + orderr.getStatus() + "，不允許接受");
        }

        orderr.setStatus(OrderStatus.運送中);
        Orderr updatedOrderr = orderrRepository.save(orderr);
        logger.info("外送員 ID {} 成功接受訂單 ID {}，狀態變為 {}", deliveryPersonId, orderId, OrderStatus.運送中);
        return updatedOrderr;
    }

    /**
     * 外送員拒絕訂單。
     * 僅限訂單狀態為「準備中」且已指派給該外送員時可拒絕。
     * 拒絕後，訂單狀態變回「準備中」，並清除外送員指派。
     *
     * @param orderId 訂單 ID
     * @param deliveryPersonId 拒絕訂單的外送員 ID
     * @return 更新後的訂單實體
     * @throws ResourceNotFoundException 如果訂單不存在
     * @throws UnauthorizedException 如果外送員無權拒絕此訂單
     * @throws InvalidOperationException 如果訂單狀態不允許拒絕
     */
    @Override
    @Transactional
    public Orderr rejectDelivery(Long orderId, Long deliveryPersonId)
            throws ResourceNotFoundException, UnauthorizedException, InvalidOperationException {
        logger.warn("外送員 ID {} 嘗試拒絕訂單 ID {}", deliveryPersonId, orderId); // WARN 級別日誌，因為拒絕是負面操作

        Orderr orderr = orderrRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("拒絕訂單失敗：訂單 ID {} 未找到", orderId);
                    return new ResourceNotFoundException("訂單 ID " + orderId + " 未找到");
                });

        // 驗證訂單是否已指派給該外送員
        if (orderr.getDeliveryPerson() == null || !orderr.getDeliveryPerson().getId().equals(deliveryPersonId)) {
            logger.warn("拒絕訂單失敗：外送員 ID {} 無權拒絕訂單 ID {}，該訂單未指派給此外送員或未指派", deliveryPersonId, orderId);
            throw new UnauthorizedException("您無權拒絕此訂單");
        }

        // 驗證訂單狀態是否允許拒絕 (例如，必須是「準備中」)
        if (orderr.getStatus() != OrderStatus.準備中) {
            logger.warn("拒絕訂單失敗：訂單 ID {} 狀態為 {}，不允許拒絕", orderr.getId(), orderr.getStatus());
            throw new InvalidOperationException("訂單狀態為 " + orderr.getStatus() + "，不允許拒絕");
        }

        orderr.setDeliveryPerson(null); // 清除外送員指派
        orderr.setStatus(OrderStatus.準備中); // 狀態變回準備中，等待重新指派
        Orderr updatedOrderr = orderrRepository.save(orderr);
        logger.info("外送員 ID {} 成功拒絕訂單 ID {}，訂單狀態變回 {}", deliveryPersonId, orderId, OrderStatus.準備中);
        return updatedOrderr;
    }

    /**
     * 檢查訂單狀態轉換是否有效。
     * 這是業務規則的一部分。
     *
     * @param currentStatus 當前狀態
     * @param newStatus 新狀態
     * @return 如果轉換有效則為 true，否則為 false
     */
    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case 處理中:
                return newStatus == OrderStatus.準備中 || newStatus == OrderStatus.取消;
            case 準備中:
                return newStatus == OrderStatus.運送中 || newStatus == OrderStatus.取消;
            case 運送中:
                return newStatus == OrderStatus.完成 || newStatus == OrderStatus.取消;
            case 完成:
            case 取消:
                return false; // 完成或取消的訂單不能再更改狀態
            default:
                return false;
        }
    }
}