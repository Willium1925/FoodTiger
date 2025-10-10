package com.matsuzaka.foodtiger.controller;

import com.matsuzaka.foodtiger.config.security.CustomUserDetails;
import com.matsuzaka.foodtiger.dao.entity.Orderr;
import com.matsuzaka.foodtiger.dao.entity.OrderStatus;
import com.matsuzaka.foodtiger.dao.entity.Restaurant;
import com.matsuzaka.foodtiger.dto.DeliveryAssignmentRequest;
import com.matsuzaka.foodtiger.dto.OrderRequest;
import com.matsuzaka.foodtiger.dto.OrderStatusUpdateRequest;
import com.matsuzaka.foodtiger.exception.InvalidOperationException;
import com.matsuzaka.foodtiger.exception.InvalidOrderStatusTransitionException;
import com.matsuzaka.foodtiger.exception.MenuItemUnavailableException;
import com.matsuzaka.foodtiger.exception.ResourceNotFoundException;
import com.matsuzaka.foodtiger.exception.UnauthorizedException;
import com.matsuzaka.foodtiger.service.OrderrService;
import com.matsuzaka.foodtiger.service.RestaurantService; // Import RestaurantService
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
@RequestMapping("/api/orderrs")
public class OrderrController {

    private static final Logger logger = LoggerFactory.getLogger(OrderrController.class);

    @Autowired
    private OrderrService orderrService;

    @Autowired
    private RestaurantService restaurantService; // Autowire RestaurantService

    // 允許 ADMIN 獲取所有訂單
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Orderr>> getAllOrderrs() {
        logger.info("收到獲取所有訂單的請求 (由 ADMIN 執行)");
        List<Orderr> orderrs = orderrService.findAllOrderrs();
        return new ResponseEntity<>(orderrs, HttpStatus.OK);
    }

    // 允許 CUSTOMER 獲取自己的訂單，DELIVER 獲取自己負責的訂單，RESTAURANT_OWNER 獲取自己餐廳的訂單，ADMIN 獲取任何訂單
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('CUSTOMER') and @orderrService.findOrderrById(#id).orElse(null)?.user?.id == authentication.principal.id) or " +
            "(hasRole('DELIVER') and @orderrService.findOrderrById(#id).orElse(null)?.deliveryPerson?.id == authentication.principal.id) or " +
            "(hasRole('RESTAURANT_OWNER') and @orderrService.findOrderrById(#id).orElse(null)?.restaurant?.owner?.id == authentication.principal.id)")
    public ResponseEntity<Orderr> getOrderrById(@PathVariable Long id) {
        logger.info("收到獲取 ID 為 {} 訂單的請求", id);
        return orderrService.findOrderrById(id)
                .map(orderr -> {
                    logger.info("成功獲取 ID 為 {} 的訂單", id);
                    return new ResponseEntity<>(orderr, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("未找到 ID 為 {} 的訂單", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // 移除原有的 createOrderr 方法，因為業務邏輯創建已由 /api/orderrs/create 處理
    // @PostMapping
    // public ResponseEntity<Orderr> createOrderr(@RequestBody Orderr orderr) {
    //     logger.info("收到直接創建訂單實體的請求 (不推薦用於業務邏輯)");
    //     Orderr savedOrderr = orderrService.saveOrderr(orderr);
    //     logger.info("訂單 ID {} 創建成功", savedOrderr.getId());
    //     return new ResponseEntity<>(savedOrderr, HttpStatus.CREATED);
    // }

    // 允許 ADMIN 更新任何訂單，或 RESTAURANT_OWNER 更新自己餐廳的訂單
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('RESTAURANT_OWNER') and @orderrService.findOrderrById(#id).orElse(null)?.restaurant?.owner?.id == authentication.principal.id)")
    public ResponseEntity<Orderr> updateOrderr(@PathVariable Long id, @RequestBody Orderr orderr) throws ResourceNotFoundException, UnauthorizedException {
        logger.info("收到更新 ID 為 {} 訂單的請求", id);

        // 獲取當前認證用戶的 ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUser.getId();

        // 驗證權限 (如果不是 ADMIN 且不是餐廳擁有者)
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            Orderr existingOrderr = orderrService.findOrderrById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("訂單 ID " + id + " 未找到"));
            if (!existingOrderr.getRestaurant().getOwner().getId().equals(currentUserId)) {
                logger.warn("用戶 ID {} 嘗試更新非自己餐廳的訂單 ID {}", currentUserId, id);
                throw new UnauthorizedException("您無權更新此訂單");
            }
        }

        return orderrService.findOrderrById(id)
                .map(existingOrderr -> {
                    existingOrderr.setUser(orderr.getUser()); // 這些字段的更新可能需要更細緻的權限控制
                    existingOrderr.setRestaurant(orderr.getRestaurant());
                    existingOrderr.setDeliveryPerson(orderr.getDeliveryPerson());
                    existingOrderr.setDeliveryAddress(orderr.getDeliveryAddress());
                    existingOrderr.setTotalAmount(orderr.getTotalAmount());
                    existingOrderr.setDeliveryFee(orderr.getDeliveryFee());
                    existingOrderr.setStatus(orderr.getStatus());
                    existingOrderr.setEstimatedDeliveryTime(orderr.getEstimatedDeliveryTime());
                    existingOrderr.setCompletedTime(orderr.getCompletedTime());
                    existingOrderr.setRating(orderr.getRating());
                    Orderr updatedOrderr = orderrService.saveOrderr(existingOrderr);
                    logger.info("訂單 ID {} 更新成功", id);
                    return new ResponseEntity<>(updatedOrderr, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("嘗試更新不存在的訂單 ID {}", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // 允許 ADMIN 刪除任何訂單
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrderr(@PathVariable Long id) {
        logger.warn("收到刪除 ID 為 {} 訂單的請求 (由 ADMIN 執行)", id);
        if (orderrService.findOrderrById(id).isPresent()) {
            orderrService.deleteOrderr(id);
            logger.info("訂單 ID {} 刪除成功", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        logger.warn("嘗試刪除不存在的訂單 ID {}", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // 允許 CUSTOMER 獲取自己的訂單列表
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #userId == authentication.principal.id)")
    public ResponseEntity<List<Orderr>> getOrderrsByUserId(@PathVariable Long userId) throws UnauthorizedException {
        logger.info("收到獲取用戶 ID 為 {} 的訂單請求", userId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) && !userId.equals(currentUser.getId())) {
            logger.warn("用戶 ID {} 嘗試獲取非自己的訂單列表 (用戶 ID {})", currentUser.getId(), userId);
            throw new UnauthorizedException("您無權查看此用戶的訂單列表");
        }

        List<Orderr> orderrs = orderrService.findOrderrsByUserId(userId);
        if (orderrs.isEmpty()) {
            logger.warn("未找到用戶 ID {} 的訂單", userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.info("成功獲取用戶 ID {} 的 {} 筆訂單", userId, orderrs.size());
        return new ResponseEntity<>(orderrs, HttpStatus.OK);
    }

    // 允許 RESTAURANT_OWNER 獲取自己餐廳的訂單列表，ADMIN 獲取任何餐廳的訂單列表
    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('RESTAURANT_OWNER') and @restaurantService.findRestaurantById(#restaurantId).orElse(null)?.owner?.id == authentication.principal.id)")
    public ResponseEntity<List<Orderr>> getOrderrsByRestaurantId(@PathVariable Long restaurantId) throws UnauthorizedException, ResourceNotFoundException {
        logger.info("收到獲取餐廳 ID 為 {} 的訂單請求", restaurantId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // 驗證餐廳是否存在
            Restaurant restaurant = restaurantService.findRestaurantById(restaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("餐廳 ID " + restaurantId + " 未找到"));
            if (!restaurant.getOwner().getId().equals(currentUser.getId())) {
                logger.warn("用戶 ID {} 嘗試獲取非自己餐廳的訂單列表 (餐廳 ID {})", currentUser.getId(), restaurantId);
                throw new UnauthorizedException("您無權查看此餐廳的訂單列表");
            }
        }

        List<Orderr> orderrs = orderrService.findOrderrsByRestaurantId(restaurantId);
        if (orderrs.isEmpty()) {
            logger.warn("未找到餐廳 ID {} 的訂單", restaurantId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.info("成功獲取餐廳 ID {} 的 {} 筆訂單", restaurantId, orderrs.size());
        return new ResponseEntity<>(orderrs, HttpStatus.OK);
    }

    // 允許 DELIVER 獲取自己負責的訂單列表，ADMIN 獲取任何外送員的訂單列表
    @GetMapping("/delivery-person/{deliveryPersonId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DELIVER') and #deliveryPersonId == authentication.principal.id)")
    public ResponseEntity<List<Orderr>> getOrderrsByDeliveryPersonId(@PathVariable Long deliveryPersonId) throws UnauthorizedException {
        logger.info("收到獲取外送員 ID 為 {} 的訂單請求", deliveryPersonId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) && !deliveryPersonId.equals(currentUser.getId())) {
            logger.warn("用戶 ID {} 嘗試獲取非自己的外送訂單列表 (外送員 ID {})", currentUser.getId(), deliveryPersonId);
            throw new UnauthorizedException("您無權查看此外送員的訂單列表");
        }

        List<Orderr> orderrs = orderrService.findOrderrsByDeliveryPersonId(deliveryPersonId);
        if (orderrs.isEmpty()) {
            logger.warn("未找到外送員 ID {} 的訂單", deliveryPersonId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.info("成功獲取外送員 ID {} 的 {} 筆訂單", deliveryPersonId, orderrs.size());
        return new ResponseEntity<>(orderrs, HttpStatus.OK);
    }

    // 允許 ADMIN 獲取任何狀態的訂單，或根據角色限制
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasAnyRole('CUSTOMER', 'DELIVER', 'RESTAURANT_OWNER')") // 這裡可以根據具體業務邏輯細化
    public ResponseEntity<List<Orderr>> getOrderrsByStatus(@PathVariable OrderStatus status) {
        logger.info("收到獲取狀態為 {} 訂單的請求", status);
        // 這裡需要更細緻的權限控制，例如 CUSTOMER 只能看自己的訂單，RESTAURANT_OWNER 只能看自己餐廳的訂單
        // 為了簡化，目前只允許所有已認證用戶訪問，但實際查詢結果會根據用戶角色在 Service 層進行過濾
        List<Orderr> orderrs = orderrService.findOrderrsByStatus(status);
        if (orderrs.isEmpty()) {
            logger.warn("未找到狀態為 {} 的訂單", status);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.info("成功獲取狀態為 {} 的 {} 筆訂單", status, orderrs.size());
        return new ResponseEntity<>(orderrs, HttpStatus.OK);
    }

    /**
     * 創建新訂單的 API 端點。
     * 僅限 CUSTOMER 角色。
     *
     * @param orderRequest 包含訂單詳細資訊的 DTO
     * @return 創建成功的訂單實體和 CREATED 狀態
     * @throws ResourceNotFoundException 如果用戶、餐廳、地址或菜單項目不存在
     * @throws MenuItemUnavailableException 如果菜單項目不可用
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Orderr> placeOrder(@Valid @RequestBody OrderRequest orderRequest) throws ResourceNotFoundException, MenuItemUnavailableException, UnauthorizedException {
        logger.info("收到下訂單請求，用戶 ID: {}, 餐廳 ID: {}", orderRequest.getUserId(), orderRequest.getRestaurantId());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        // 確保訂單是為當前認證用戶創建的
        if (!orderRequest.getUserId().equals(currentUser.getId())) {
            logger.warn("用戶 ID {} 嘗試為非自己的用戶 ID {} 下訂單", currentUser.getId(), orderRequest.getUserId());
            throw new UnauthorizedException("您無權為其他用戶下訂單");
        }

        Orderr newOrder = orderrService.createOrder(orderRequest);
        logger.info("訂單 ID {} 下單成功", newOrder.getId());
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    /**
     * 更新訂單狀態的 API 端點。
     * 僅限 RESTAURANT_OWNER 角色。
     *
     * @param orderId 訂單 ID
     * @param request 包含新狀態的 DTO
     * @return 更新後的訂單實體和 OK 狀態
     * @throws ResourceNotFoundException 如果訂單不存在
     * @throws InvalidOrderStatusTransitionException 如果狀態轉換無效
     * @throws UnauthorizedException 如果操作者不是該訂單所屬餐廳的擁有者
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Orderr> updateOrderStatus(@PathVariable Long orderId,
                                                    @Valid @RequestBody OrderStatusUpdateRequest request) // Removed @RequestParam Long restaurantOwnerId
            throws ResourceNotFoundException, InvalidOrderStatusTransitionException, UnauthorizedException {
        logger.info("收到更新訂單 ID {} 狀態的請求，新狀態為 {}", orderId, request.getNewStatus());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long restaurantOwnerId = currentUser.getId(); // 從認證資訊中獲取餐廳擁有者 ID

        Orderr updatedOrder = orderrService.updateOrderStatus(orderId, request, restaurantOwnerId);
        logger.info("訂單 ID {} 狀態更新成功為 {}", orderId, updatedOrder.getStatus());
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }

    /**
     * 指派外送員給訂單的 API 端點。
     * 僅限 ADMIN 或 RESTAURANT_OWNER 角色。
     *
     * @param orderId 訂單 ID
     * @param request 包含外送員 ID 的 DTO
     * @return 更新後的訂單實體和 OK 狀態
     * @throws ResourceNotFoundException 如果訂單或外送員不存在
     * @throws InvalidOperationException 如果訂單狀態不允許指派外送員，或訂單已被指派
     */
    @PutMapping("/{orderId}/assign-delivery")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public ResponseEntity<Orderr> assignDeliveryPerson(@PathVariable Long orderId,
                                                       @Valid @RequestBody DeliveryAssignmentRequest request) // Removed @RequestParam Long restaurantOwnerId
            throws ResourceNotFoundException, InvalidOperationException, UnauthorizedException {
        logger.info("收到指派外送員 ID {} 給訂單 ID {} 的請求", request.getDeliveryPersonId(), orderId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        // 如果是 RESTAURANT_OWNER，需要驗證是否是自己餐廳的訂單
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RESTAURANT_OWNER"))) {
            Orderr orderr = orderrService.findOrderrById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("訂單 ID " + orderId + " 未找到"));
            if (!orderr.getRestaurant().getOwner().getId().equals(currentUser.getId())) {
                logger.warn("餐廳擁有者 ID {} 嘗試指派非自己餐廳的訂單 ID {}", currentUser.getId(), orderId);
                throw new UnauthorizedException("您無權指派此訂單的外送員");
            }
        }

        Orderr updatedOrder = orderrService.assignDeliveryPerson(orderId, request);
        logger.info("訂單 ID {} 成功指派給外送員 ID {}", orderId, updatedOrder.getDeliveryPerson().getId());
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }

    /**
     * 外送員接受訂單的 API 端點。
     * 僅限 DELIVER 角色。
     *
     * @param orderId 訂單 ID
     * @return 更新後的訂單實體和 OK 狀態
     * @throws ResourceNotFoundException 如果訂單不存在
     * @throws UnauthorizedException 如果外送員無權接受此訂單
     * @throws InvalidOperationException 如果訂單狀態不允許接受
     */
    @PutMapping("/{orderId}/accept-delivery")
    @PreAuthorize("hasRole('DELIVER')")
    public ResponseEntity<Orderr> acceptDelivery(@PathVariable Long orderId) // Removed @RequestParam Long deliveryPersonId
            throws ResourceNotFoundException, UnauthorizedException, InvalidOperationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long deliveryPersonId = currentUser.getId(); // 從認證資訊中獲取外送員 ID

        logger.info("收到外送員 ID {} 接受訂單 ID {} 的請求", deliveryPersonId, orderId);
        Orderr updatedOrder = orderrService.acceptDelivery(orderId, deliveryPersonId);
        logger.info("外送員 ID {} 成功接受訂單 ID {}，狀態變為 {}", deliveryPersonId, orderId, updatedOrder.getStatus());
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }

    /**
     * 外送員拒絕訂單的 API 端點。
     * 僅限 DELIVER 角色。
     *
     * @param orderId 訂單 ID
     * @return 更新後的訂單實體和 OK 狀態
     * @throws ResourceNotFoundException 如果訂單不存在
     * @throws UnauthorizedException 如果外送員無權拒絕此訂單
     * @throws InvalidOperationException 如果訂單狀態不允許拒絕
     */
    @PutMapping("/{orderId}/reject-delivery")
    @PreAuthorize("hasRole('DELIVER')")
    public ResponseEntity<Orderr> rejectDelivery(@PathVariable Long orderId) // Removed @RequestParam Long deliveryPersonId
            throws ResourceNotFoundException, UnauthorizedException, InvalidOperationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        Long deliveryPersonId = currentUser.getId(); // 從認證資訊中獲取外送員 ID

        logger.warn("收到外送員 ID {} 拒絕訂單 ID {} 的請求", deliveryPersonId, orderId);
        Orderr updatedOrder = orderrService.rejectDelivery(orderId, deliveryPersonId);
        logger.info("外送員 ID {} 成功拒絕訂單 ID {}，訂單狀態變回 {}", deliveryPersonId, orderId, updatedOrder.getStatus());
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }
}
