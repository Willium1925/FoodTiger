CREATE DATABASE IF NOT EXISTS FoodTigerDB;

DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS orderr_item;
DROP TABLE IF EXISTS orderr;
DROP TABLE IF EXISTS menu_item;
DROP TABLE IF EXISTS restaurant;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS user;



CREATE TABLE user(
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,  -- 用戶名，唯一
                       password VARCHAR(255) NOT NULL,        --  hashed 密碼
                       email VARCHAR(100) UNIQUE,             -- 電子郵件，可 NULL 但唯一
                       phone VARCHAR(20) UNIQUE,              -- 手機號碼，可 NULL 但唯一
                       role ENUM('CUSTOMER', 'DELIVER', 'RESTAURANT_OWNER', 'ADMIN') NOT NULL,  -- 角色
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 創建時間，自動記錄
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  -- 更新時間，追蹤變更
);
-- 索引：加速登入查詢
CREATE INDEX idx_users_email ON user(email);
CREATE INDEX idx_users_phone ON user(phone);


CREATE TABLE address (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT,                        -- 連結用戶 (可 NULL，若為餐廳地址)
                           zip_code VARCHAR(20),                  -- 郵遞區號
                           city VARCHAR(50) NOT NULL,             -- 市／縣
                           district VARCHAR(50),                  -- 區
                           street VARCHAR(255) NOT NULL,          -- 街道
                           extra_details VARCHAR(50),             -- 額外資訊，如門牌細節
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE  -- 刪除用戶時連動刪除地址，維持一致性
);
-- 索引：加速地理查詢
CREATE INDEX idx_address_city ON address(city, district, street);


CREATE TABLE restaurant (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(100) NOT NULL,
                             owner_id BIGINT NOT NULL,              -- 擁有者，連結 user
                             address_id BIGINT NOT NULL,            -- 地址，連結 address
                             description TEXT,
                             rating DECIMAL(3,1) DEFAULT 0,      -- 評分，精確到小數1位
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (owner_id) REFERENCES user(id) ON DELETE RESTRICT,  -- 限制刪除擁有者，避免餐廳孤立
                             FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE CASCADE  -- 刪除地址時連動刪除餐廳，維持一致性
);
-- 索引：加速搜尋
CREATE INDEX idx_restaurant_name ON restaurant(name);
CREATE INDEX idx_restaurant_rating ON restaurant(rating);
CREATE INDEX idx_restaurant_owner_id ON restaurant(owner_id); -- 加速查詢某擁有者的餐廳，方便餐廳管理員


CREATE TABLE menu_item (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            restaurant_id BIGINT NOT NULL,         -- 所屬餐廳
                            title VARCHAR(100) NOT NULL,
                            description TEXT,
                            price INT NOT NULL,
                            image_url VARCHAR(255),                -- 圖片連結，易整合 CDN
                            available BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE     -- 刪除餐廳時連動刪除菜單項目，維持一致性
);


CREATE TABLE orderr (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,               -- 顧客
                        restaurant_id BIGINT NOT NULL,
                        delivery_person_id BIGINT,             -- 送餐員，可 NULL 直到分配
                        delivery_address_id BIGINT NOT NULL,
                        -- payment_id BIGINT,                     -- 支付
                        total_amount INT NOT NULL,
                        delivery_fee INT DEFAULT 0,
                        status ENUM('處理中', '準備中', '運送中', '完成', '取消') DEFAULT '處理中',
                        orderr_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        estimated_delivery_time TIMESTAMP,
                        completed_time TIMESTAMP,
                        rating INT CHECK (rating BETWEEN 1 AND 5), -- 顧客評分，限制只能 1-5 星
                        FOREIGN KEY (user_id) REFERENCES user(id),
                        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
                        FOREIGN KEY (delivery_person_id) REFERENCES user(id),
                        FOREIGN KEY (delivery_address_id) REFERENCES address(id)
                        -- FOREIGN KEY (payment_id) REFERENCES payment(id)  -- 見下
);
-- 索引：加速狀態查詢
CREATE INDEX idx_orderr_status ON orderr(status);
CREATE INDEX idx_orderr_user_id ON orderr(user_id);


CREATE TABLE orderr_item (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             orderr_id BIGINT NOT NULL,
                             menu_item_id BIGINT NOT NULL,
                             quantity INT NOT NULL DEFAULT 1,
                             price_at_orderr INT NOT NULL,  -- 訂單時價格，凍結值
                             FOREIGN KEY (orderr_id) REFERENCES orderr(id) ON DELETE CASCADE,        -- 刪除訂單時連動刪除訂單項目
                             FOREIGN KEY (menu_item_id) REFERENCES menu_item(id) ON DELETE RESTRICT  -- 限制刪除菜單項目，當有訂單項目引用某個菜單項目時，該菜單項目不能被刪除，避免訂單孤立
);


CREATE TABLE payment (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         orderr_id BIGINT NOT NULL UNIQUE,       -- 一訂單一支付
                         amount INT NOT NULL,
                         payment_method ENUM('刷卡', '現金', 'App') NOT NULL,
                         transaction_id VARCHAR(100),           -- 第三方 ID
                         status ENUM('處理中', '付款成功', '付款失敗') DEFAULT '處理中',
                         payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (orderr_id) REFERENCES orderr(id) ON DELETE CASCADE
);





