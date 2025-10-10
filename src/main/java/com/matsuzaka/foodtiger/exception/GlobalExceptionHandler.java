package com.matsuzaka.foodtiger.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 處理所有 FoodTigerException 及其子類型的業務邏輯錯誤。
     * 這些是 Checked Exception，表示應用程式預期會發生的業務規則違規。
     * 記錄為 WARN 級別，因為它們是可預期的業務錯誤，而不是系統故障。
     *
     * @param ex 拋出的 FoodTigerException 實例
     * @param request 當前的 Web 請求
     * @return 包含錯誤詳情的 ResponseEntity
     */
    @ExceptionHandler(FoodTigerException.class)
    public ResponseEntity<Object> handleFoodTigerException(FoodTigerException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));

        logger.warn("業務邏輯錯誤 (Checked Exception): {}", ex.getMessage()); // 記錄為 WARN
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * 處理 ResourceNotFoundException，表示請求的資源不存在。
     * 記錄為 WARN 級別。
     *
     * @param ex 拋出的 ResourceNotFoundException 實例
     * @param request 當前的 Web 請求
     * @return 包含錯誤詳情的 ResponseEntity
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));

        logger.warn("資源未找到錯誤 (Checked Exception): {}", ex.getMessage()); // 記錄為 WARN
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * 處理所有未被特定處理器捕獲的 RuntimeException (Unchecked Exception)。
     * 這些通常表示程式碼邏輯錯誤、配置問題或環境故障。
     * 記錄為 ERROR 級別，因為它們是未預期的系統錯誤，需要開發人員關注。
     *
     * @param ex 拋出的 RuntimeException 實例
     * @param request 當前的 Web 請求
     * @return 包含錯誤詳情的 ResponseEntity
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred: " + ex.getMessage());
        body.put("path", request.getDescription(false));

        logger.error("未預期的運行時錯誤 (Unchecked Exception): {}", ex.getMessage(), ex); // 記錄為 ERROR，包含堆棧追蹤
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 處理所有其他未被捕獲的 Exception。
     * 這些是更廣泛的錯誤，可能包括系統級別的問題。
     * 記錄為 ERROR 級別。
     *
     * @param ex 拋出的 Exception 實例
     * @param request 當前的 Web 請求
     * @return 包含錯誤詳情的 ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unknown error occurred: " + ex.getMessage());
        body.put("path", request.getDescription(false));

        logger.error("未捕獲的例外 (Unchecked Exception): {}", ex.getMessage(), ex); // 記錄為 ERROR，包含堆棧追蹤
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
