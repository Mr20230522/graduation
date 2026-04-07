-- ============================================
-- 权限配置脚本
-- 为 common 角色添加"车辆预约"菜单
-- 添加 boss 角色并配置菜单
-- ============================================

-- 1. 添加 boss 角色（如果不存在）
INSERT IGNORE INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
VALUES (3, '老板', 'boss', 3, 2, 1, 1, '0', '0', 'admin', NOW(), '', NULL, '门店老板角色');

-- 2. 添加"车辆预约"菜单（如果不存在）
-- 一级菜单：车辆预约
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3000, '车辆预约', 0, 5, 'booking', NULL, '', '', 1, 0, 'M', '0', '0', '', 'car', 'admin', NOW(), '', NULL, '车辆预约目录');

-- 二级菜单：门店选择
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3001, '门店选择', 3000, 1, 'deptSelect', 'booking/deptSelect/index', '', 'DeptSelect', 1, 0, 'C', '0', '0', 'booking:dept:list', 'shop', 'admin', NOW(), '', NULL, '门店选择菜单');

-- 二级菜单：预约日历
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3002, '预约日历', 3000, 2, 'calendar', 'booking/bookingCalendar', '', 'BookingCalendar', 1, 0, 'C', '0', '0', 'booking:calendar:list', 'calendar', 'admin', NOW(), '', NULL, '预约日历菜单');

-- 二级菜单：我的预约
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3003, '我的预约', 3000, 3, 'myBooking', 'booking/myBookingList', '', 'MyBooking', 1, 0, 'C', '0', '0', 'booking:my:list', 'document', 'admin', NOW(), '', NULL, '我的预约菜单');

-- 二级菜单：历史记录
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3004, '历史记录', 3000, 4, 'history', 'booking/bookingHistory', '', 'BookingHistory', 1, 0, 'C', '0', '0', 'booking:history:list', 'clock', 'admin', NOW(), '', NULL, '历史记录菜单');

-- 二级菜单：消费一览
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3005, '消费一览', 3000, 5, 'myConsumption', 'booking/myConsumption', '', 'myConsumption', 1, 0, 'C', '0', '0', 'booking:consumption:list', 'shopping-cart', 'admin', NOW(), '', NULL, '消费一览菜单');

-- 3. 添加"老板管理"菜单（如果不存在）
-- 一级菜单：老板管理
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (4000, '老板管理', 0, 6, 'boss', NULL, '', '', 1, 0, 'M', '0', '0', '', 'user-solid', 'admin', NOW(), '', NULL, '老板管理目录');

-- 二级菜单：营收看板
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (4001, '营收看板', 4000, 1, 'revenue', 'boss/revenue', '', 'BossRevenue', 1, 0, 'C', '0', '0', 'boss:revenue:list', 'data-line', 'admin', NOW(), '', NULL, '营收看板菜单');

-- 二级菜单：评价管理
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (4002, '评价管理', 4000, 2, 'review', 'boss/review', '', 'BossReview', 1, 0, 'C', '0', '0', 'boss:review:list', 'star-on', 'admin', NOW(), '', NULL, '评价管理菜单');

-- 二级菜单：门店设置
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (4003, '门店设置', 4000, 3, 'setting', 'boss/setting', '', 'BossSetting', 1, 0, 'C', '0', '0', 'boss:setting:edit', 'setting', 'admin', NOW(), '', NULL, '门店设置菜单');

-- 4. 为 common 角色分配"车辆预约"菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, 3000),
(2, 3001),
(2, 3002),
(2, 3003),
(2, 3004),
(2, 3005);

-- 5. 为 boss 角色分配"老板管理"菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(3, 4000),
(3, 4001),
(3, 4002),
(3, 4003);

-- 6. 为 boss 角色分配"车辆预约"菜单权限（老板也可以使用预约功能）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(3, 3000),
(3, 3001),
(3, 3002),
(3, 3003),
(3, 3004),
(3, 3005);

-- 7. 为 common 角色分配门店（可选，让普通用户也能看到门店）
INSERT IGNORE INTO sys_role_dept (role_id, dept_id) VALUES
(2, 100),
(2, 101),
(2, 102),
(2, 200),
(2, 201),
(2, 202);

-- 8. 为 boss 角色分配门店（老板的门店）
INSERT IGNORE INTO sys_role_dept (role_id, dept_id) VALUES
(3, 200);

-- ============================================
-- 注意事项：
-- 1. 如果菜单ID已存在，请手动调整SQL中的ID
-- 2. 执行前请备份数据库
-- 3. 执行后需要重新登录才能看到新的菜单
-- ============================================
