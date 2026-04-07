-- ============================================
-- Booking System Database Fix Script
-- Created: 2026-04-02
-- Description: 修复数据库表结构，添加缺失字段和表
-- ============================================

-- ============================================
-- 1. 修改 sys_dept 表，添加缺失的字段
-- ============================================
ALTER TABLE sys_dept 
ADD COLUMN IF NOT EXISTS car_space_count INT DEFAULT 4 COMMENT '车位数量' AFTER license,
ADD COLUMN IF NOT EXISTS address VARCHAR(200) DEFAULT '' COMMENT '门店详细地址' AFTER car_space_count,
ADD COLUMN IF NOT EXISTS longitude DOUBLE COMMENT '经度' AFTER address,
ADD COLUMN IF NOT EXISTS latitude DOUBLE COMMENT '纬度' AFTER longitude,
ADD COLUMN IF NOT EXISTS license VARCHAR(50) DEFAULT '' COMMENT '营业执照编号' AFTER latitude,
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500) DEFAULT '' COMMENT '门店图片地址' AFTER alipay_gateway,
ADD COLUMN IF NOT EXISTS image_type CHAR(1) DEFAULT '0' COMMENT '图片类型（0系统默认 1自定义）' AFTER image_url;

-- ============================================
-- 2. 创建门店车位表
-- ============================================
DROP TABLE IF EXISTS dept_car_space;
CREATE TABLE dept_car_space (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    dept_id BIGINT(20) NOT NULL COMMENT '门店ID',
    space_no VARCHAR(20) NOT NULL COMMENT '车位编号',
    space_name VARCHAR(50) DEFAULT NULL COMMENT '车位名称',
    status TINYINT(1) DEFAULT 0 COMMENT '状态 0启用 1禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_space (dept_id, space_no),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店车位表';

-- ============================================
-- 3. 创建门店排班表
-- ============================================
DROP TABLE IF EXISTS dept_schedule;
CREATE TABLE dept_schedule (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    dept_id BIGINT(20) NOT NULL COMMENT '门店ID',
    work_date DATE NOT NULL COMMENT '工作日期',
    open_time VARCHAR(10) DEFAULT '08:00:00' COMMENT '营业开始时间',
    close_time VARCHAR(10) DEFAULT '20:00:00' COMMENT '营业结束时间',
    slot_minutes INT DEFAULT 30 COMMENT '时段分钟数',
    car_spaces INT DEFAULT 4 COMMENT '车位数量',
    status TINYINT(1) DEFAULT 0 COMMENT '状态 0正常 1休息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_date (dept_id, work_date),
    KEY idx_dept_id (dept_id),
    KEY idx_work_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店排班表';

-- ============================================
-- 4. 创建预约记录表
-- ============================================
DROP TABLE IF EXISTS car_booking;
CREATE TABLE car_booking (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    booking_no VARCHAR(50) NOT NULL COMMENT '预约单号',
    dept_id BIGINT(20) NOT NULL COMMENT '门店ID',
    space_no VARCHAR(20) DEFAULT NULL COMMENT '车位编号',
    work_date DATE DEFAULT NULL COMMENT '预约日期',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    car_number VARCHAR(20) DEFAULT NULL COMMENT '车牌号',
    car_model VARCHAR(50) DEFAULT NULL COMMENT '车型',
    car_color VARCHAR(20) DEFAULT NULL COMMENT '车颜色',
    combo_name VARCHAR(50) DEFAULT NULL COMMENT '套餐名称',
    combo_minutes INT DEFAULT NULL COMMENT '套餐分钟数',
    code VARCHAR(10) DEFAULT NULL COMMENT '核销码',
    status TINYINT(1) DEFAULT 0 COMMENT '状态 0待服务 1已核销 2服务中 3已取消',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_booking_no (booking_no),
    UNIQUE KEY uk_code (code),
    KEY idx_user_id (user_id),
    KEY idx_dept_id (dept_id),
    KEY idx_work_date (work_date),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约记录表';

-- ============================================
-- 5. 创建订单表
-- ============================================
DROP TABLE IF EXISTS payment_order;
CREATE TABLE payment_order (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    booking_id BIGINT(20) DEFAULT NULL COMMENT '预约ID',
    booking_no VARCHAR(50) DEFAULT NULL COMMENT '预约单号',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    dept_id BIGINT(20) NOT NULL COMMENT '门店ID',
    amount DECIMAL(10,2) DEFAULT NULL COMMENT '订单金额',
    paid_amount DECIMAL(10,2) DEFAULT NULL COMMENT '实付金额',
    subject VARCHAR(200) DEFAULT NULL COMMENT '订单标题',
    body TEXT COMMENT '订单描述',
    status TINYINT(1) DEFAULT 0 COMMENT '状态 0待支付 1支付中 2已支付 3已取消 4已退款',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式',
    transaction_id VARCHAR(100) DEFAULT NULL COMMENT '第三方流水号',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    callback_data TEXT COMMENT '回调数据',
    car_number VARCHAR(20) DEFAULT NULL COMMENT '车牌号',
    car_model VARCHAR(50) DEFAULT NULL COMMENT '车型',
    car_color VARCHAR(20) DEFAULT NULL COMMENT '车颜色',
    work_date DATE DEFAULT NULL COMMENT '预约日期',
    slot VARCHAR(10) DEFAULT NULL COMMENT '预约时段',
    space_no VARCHAR(20) DEFAULT NULL COMMENT '车位编号',
    combo_minutes INT DEFAULT NULL COMMENT '套餐分钟数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_dept_id (dept_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================
-- 6. 创建支付记录表
-- ============================================
DROP TABLE IF EXISTS booking_payment_record;
CREATE TABLE booking_payment_record (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    transaction_id VARCHAR(100) DEFAULT NULL COMMENT '第三方流水号',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式',
    amount DECIMAL(10,2) DEFAULT NULL COMMENT '支付金额',
    status TINYINT(1) DEFAULT 0 COMMENT '状态 0处理中 1成功 2失败',
    request_data TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    callback_data TEXT COMMENT '回调数据',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_no (order_no),
    KEY idx_transaction_id (transaction_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- ============================================
-- 7. 创建预约历史表
-- ============================================
DROP TABLE IF EXISTS booking_history;
CREATE TABLE booking_history (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    booking_no VARCHAR(50) DEFAULT NULL COMMENT '预约单号',
    dept_id BIGINT(20) DEFAULT NULL COMMENT '门店ID',
    space_no VARCHAR(20) DEFAULT NULL COMMENT '车位编号',
    work_date DATE DEFAULT NULL COMMENT '预约日期',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    car_number VARCHAR(20) DEFAULT NULL COMMENT '车牌号',
    car_model VARCHAR(50) DEFAULT NULL COMMENT '车型',
    car_color VARCHAR(20) DEFAULT NULL COMMENT '车颜色',
    combo_name VARCHAR(50) DEFAULT NULL COMMENT '套餐名称',
    combo_minutes INT DEFAULT NULL COMMENT '套餐分钟数',
    code VARCHAR(10) DEFAULT NULL COMMENT '核销码',
    status TINYINT(1) DEFAULT NULL COMMENT '状态',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_booking_no (booking_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约历史表';

-- ============================================
-- 8. 为现有门店初始化车位数据（示例：为 dept_id=101, 102 初始化4个车位）
-- ============================================
-- 为深圳总公司初始化车位
INSERT IGNORE INTO dept_car_space (dept_id, space_no, space_name, status) VALUES
(101, 'A1', '1号洗车位', 0),
(101, 'A2', '2号洗车位', 0),
(101, 'A3', '3号洗车位', 0),
(101, 'A4', '4号洗车位', 0);

-- 为长沙分公司初始化车位
INSERT IGNORE INTO dept_car_space (dept_id, space_no, space_name, status) VALUES
(102, 'B1', '1号洗车位', 0),
(102, 'B2', '2号洗车位', 0),
(102, 'B3', '3号洗车位', 0),
(102, 'B4', '4号洗车位', 0);

-- ============================================
-- 9. 为现有门店初始化排班数据（未来7天）
-- ============================================
DELIMITER //

DROP PROCEDURE IF EXISTS init_schedule_for_dept//

CREATE PROCEDURE init_schedule_for_dept(
    IN p_dept_id BIGINT,
    IN p_days INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE work_date_val DATE;
    
    WHILE i < p_days DO
        SET work_date_val = DATE_ADD(CURDATE(), INTERVAL i DAY);
        
        INSERT IGNORE INTO dept_schedule (dept_id, work_date, open_time, close_time, slot_minutes, car_spaces, status)
        VALUES (p_dept_id, work_date_val, '08:00:00', '20:00:00', 30, 4, 0);
        
        SET i = i + 1;
    END WHILE;
END//

DELIMITER ;

-- 调用存储过程为门店初始化排班
CALL init_schedule_for_dept(101, 30);
CALL init_schedule_for_dept(102, 30);

-- ============================================
-- 10. 更新 sys_dept 的 car_space_count 字段
-- ============================================
UPDATE sys_dept SET car_space_count = 4 WHERE dept_id IN (101, 102);

-- ============================================
-- 11. 添加测试门店数据（可选）
-- ============================================
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, car_space_count, address, longitude, latitude, license)
VALUES 
(200, 0, '0', '旗舰店', 0, '店长', '13800138000', 'store@test.com', '0', '0', 'admin', NOW(), 6, '北京市朝阳区建国路88号', 116.478179, 39.928896, '91110000MA0001XX01'),
(201, 200, '0,200', '分店1', 1, '店长', '13800138001', 'store1@test.com', '0', '0', 'admin', NOW(), 4, '北京市海淀区中关村大街1号', 116.317717, 39.983424, '91110000MA0001XX02'),
(202, 200, '0,200', '分店2', 2, '店长', '13800138002', 'store2@test.com', '0', '0', 'admin', NOW(), 5, '北京市海淀区知春路28号', 116.327217, 39.983424, '91110000MA0001XX03')
ON DUPLICATE KEY UPDATE 
    car_space_count = VALUES(car_space_count),
    address = VALUES(address),
    longitude = VALUES(longitude),
    latitude = VALUES(latitude),
    license = VALUES(license);

-- 为新添加的门店初始化车位
INSERT IGNORE INTO dept_car_space (dept_id, space_no, space_name, status) VALUES
(200, 'A1', '1号洗车位', 0),
(200, 'A2', '2号洗车位', 0),
(200, 'A3', '3号洗车位', 0),
(200, 'A4', '4号洗车位', 0),
(200, 'A5', '5号洗车位', 0),
(200, 'A6', '6号洗车位', 0),
(201, 'B1', '1号洗车位', 0),
(201, 'B2', '2号洗车位', 0),
(201, 'B3', '3号洗车位', 0),
(201, 'B4', '4号洗车位', 0),
(202, 'C1', '1号洗车位', 0),
(202, 'C2', '2号洗车位', 0),
(202, 'C3', '3号洗车位', 0),
(202, 'C4', '4号洗车位', 0),
(202, 'C5', '5号洗车位', 0);

-- 为新门店初始化排班
CALL init_schedule_for_dept(200, 30);
CALL init_schedule_for_dept(201, 30);
CALL init_schedule_for_dept(202, 30);

-- ============================================
-- 完成
-- ============================================
SELECT 'Database fix completed successfully!' AS message;
