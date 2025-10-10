-- 插入用戶
INSERT INTO user (username, password, email, phone, role) VALUES
('alice', 'hashed_pw1', 'alice@example.com', '0912345678', 'CUSTOMER'),
('bob', 'hashed_pw2', 'bob@example.com', '0922333444', 'DELIVER'),
('carol', 'hashed_pw3', 'carol@example.com', '0933221100', 'RESTAURANT_OWNER'),
('david', 'hashed_pw5', 'david@example.com', '0944556677', 'RESTAURANT_OWNER'),
('eva', 'hashed_pw6', 'eva@example.com', '0955667788', 'RESTAURANT_OWNER'),
('frank', 'hashed_pw7', 'frank@example.com', '0966778899', 'RESTAURANT_OWNER'),
('admin', 'hashed_pw4', 'admin@example.com', '0999888777', 'ADMIN');

-- 插入地址（用戶地址和餐廳地址）
INSERT INTO address (user_id, zip_code, city, district, street, extra_details) VALUES
(1, '100', '台北市', '中正區', '忠孝東路一段1號', '10樓'),
(2, '104', '台北市', '大同區', '承德路三段99號', NULL),
(3, '105', '台北市', '信義區', '松高路88號', 'B1'),
(4, '110', '台北市', '南港區', '南港路一段50號', NULL),
(5, '111', '台北市', '士林區', '士林路200號', NULL),
(6, '112', '台北市', '北投區', '北投路300號', NULL),
(7, '113', '台北市', '文山區', '文山路400號', NULL),
(NULL, '106', '台北市', '大安區', '復興南路二段200號', '餐廳地址1'),
(NULL, '107', '台北市', '松山區', '南京東路五段10號', '餐廳地址2'),
(NULL, '108', '台北市', '中山區', '中山北路二段20號', '餐廳地址3'),
(NULL, '109', '台北市', '萬華區', '西園路一段30號', '餐廳地址4');

-- 插入餐廳（owner_id=3,4,5,6，address_id=8,9,10,11）
INSERT INTO restaurant (name, owner_id, address_id, description, rating) VALUES
('美味餐廳', 3, 8, '主打台灣家常菜', 4.5),
('川味小館', 4, 9, '四川麻辣料理', 4.2),
('日式食堂', 5, 10, '精緻日式料理', 4.7),
('義式餐坊', 6, 11, '道地義大利麵', 4.3);

-- 插入菜單項目（每家餐廳 3 個）
INSERT INTO menu_item (restaurant_id, title, description, price, image_url) VALUES
(1, '滷肉飯', '經典台灣滷肉飯', 80, 'https://cdn.example.com/luroufan.jpg'),
(1, '雞排', '酥脆多汁雞排', 100, 'https://cdn.example.com/chicken.jpg'),
(1, '青菜蛋花湯', '清爽蛋花湯', 60, 'https://cdn.example.com/soup.jpg'),
(2, '麻辣火鍋', '四川麻辣火鍋', 350, 'https://cdn.example.com/hotpot.jpg'),
(2, '口水雞', '麻辣口水雞', 180, 'https://cdn.example.com/chicken2.jpg'),
(2, '酸辣粉', '重口味酸辣粉', 120, 'https://cdn.example.com/noodle.jpg'),
(3, '壽司拼盤', '新鮮壽司', 300, 'https://cdn.example.com/sushi.jpg'),
(3, '豬排丼', '日式豬排丼飯', 220, 'https://cdn.example.com/don.jpg'),
(3, '味噌湯', '經典味噌湯', 50, 'https://cdn.example.com/miso.jpg'),
(4, '海鮮義大利麵', '新鮮海鮮搭配義大利麵', 280, 'https://cdn.example.com/pasta.jpg'),
(4, '瑪格麗特披薩', '經典瑪格麗特披薩', 250, 'https://cdn.example.com/pizza.jpg'),
(4, '凱薩沙拉', '清爽凱薩沙拉', 120, 'https://cdn.example.com/salad.jpg');

-- 插入訂單（6 筆，分配不同顧客、餐廳、送餐員、地址）
INSERT INTO orderr (user_id, restaurant_id, delivery_person_id, delivery_address_id, total_amount, delivery_fee, status, estimated_delivery_time, rating) VALUES
(1, 1, 2, 1, 240, 30, '完成', CURRENT_TIMESTAMP, 5),
(1, 2, 2, 1, 470, 40, '運送中', CURRENT_TIMESTAMP, NULL),
(1, 3, 2, 1, 570, 35, '完成', CURRENT_TIMESTAMP, 4),
(1, 4, 2, 1, 400, 30, '完成', CURRENT_TIMESTAMP, 5),
(3, 1, 2, 3, 180, 30, '完成', CURRENT_TIMESTAMP, 5),
(5, 2, 2, 5, 350, 40, '運送中', CURRENT_TIMESTAMP, NULL);

-- 插入訂單項目（每筆訂單 2~3 個）
INSERT INTO orderr_item (orderr_id, menu_item_id, quantity, price_at_orderr) VALUES
(1, 1, 1, 80), (1, 2, 1, 100), (1, 3, 1, 60),
(2, 4, 1, 350), (2, 5, 1, 180), (2, 6, 1, 120),
(3, 7, 2, 300), (3, 8, 1, 220), (3, 9, 1, 50),
(4, 10, 1, 280), (4, 11, 1, 250), (4, 12, 1, 120),
(5, 1, 1, 80), (5, 3, 1, 60),
(6, 4, 1, 350);

-- 插入支付（每筆訂單一筆）
INSERT INTO payment (orderr_id, amount, payment_method, transaction_id, status) VALUES
(1, 270, '刷卡', 'TXN100001', '付款成功'),
(2, 630, '現金', 'TXN100002', '付款成功'),
(3, 870, '刷卡', 'TXN100003', '付款成功'),
(4, 430, '刷卡', 'TXN100004', '付款成功'),
(5, 170, '現金', 'TXN100005', '付款成功'),
(6, 390, '刷卡', 'TXN100006', '付款成功');
