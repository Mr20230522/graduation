-- ============================================
-- 测试数据脚本
-- 创建测试用户用于验证权限
-- ============================================

-- 1. 创建 common 角色的测试用户（用于测试"车辆预约"功能）
INSERT IGNORE INTO sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
VALUES (100, 101, 'testcommon', '测试用户', '00', 'test@example.com', '13800138001', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), NOW(), 'admin', NOW(), '', NULL, '测试用户-普通角色');

-- 分配 common 角色给测试用户
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (100, 2);

-- 2. 创建 boss 角色的测试用户（用于测试"老板管理"功能）
INSERT IGNORE INTO sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
VALUES (101, 200, 'testboss', '测试老板', '00', 'boss@example.com', '13800138002', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), NOW(), 'admin', NOW(), '', NULL, '测试用户-老板角色');

-- 分配 boss 角色给测试用户
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (101, 3);

-- ============================================
-- 测试账号说明（密码都是：123456）
-- 
-- testcommon - 普通用户，可以看到"车辆预约"菜单
-- testboss   - 老板，可以看到"老板管理"菜单
-- 
-- 如果执行后发现看不到菜单，请：
-- 1. 确认已执行 add_role_menu.sql
-- 2. 重新登录
-- 3. 清除浏览器缓存
-- ============================================
