# 权限配置修复说明

## 问题描述
车辆预约和老板管理菜单无法动态显示在侧边栏

## 问题原因
1. **数据库缺少角色菜单配置** - `common` 角色没有被分配"车辆预约"菜单，`boss` 角色不存在
2. **前端组件路径错误** - 已修复

## 修复步骤

### 步骤 1：执行 SQL 脚本（按顺序执行）

1. **add_role_menu.sql** - 添加角色和菜单配置
```bash
mysql -u your_username -p your_database < sql/add_role_menu.sql
```

2. **test_users.sql** - 创建测试用户（可选）
```bash
mysql -u your_username -p your_database < sql/test_users.sql
```

### 步骤 2：验证配置

执行以下 SQL 检查配置是否成功：

```sql
-- 检查角色是否创建成功
SELECT * FROM sys_role WHERE role_key IN ('common', 'boss');

-- 检查菜单是否创建成功
SELECT * FROM sys_menu WHERE menu_id IN (3000, 3001, 3002, 3003, 3004, 3005, 4000, 4001, 4002, 4003);

-- 检查角色菜单关联
SELECT r.role_name, m.menu_name 
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.role_id
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_key IN ('common', 'boss');

-- 检查用户角色关联
SELECT u.user_name, r.role_name 
FROM sys_user_role ur
JOIN sys_user u ON ur.user_id = u.user_id
JOIN sys_role r ON ur.role_id = r.role_id
WHERE u.user_name IN ('testcommon', 'testboss');
```

### 步骤 3：测试登录

使用以下测试账号登录：

| 账号 | 密码 | 角色 | 可见菜单 |
|------|------|------|----------|
| testcommon | 123456 | common | 车辆预约 |
| testboss | 123456 | boss | 老板管理 |
| ry | 123456 | common | 车辆预约（需要先执行 add_role_menu.sql） |

### 步骤 4：清除缓存

登录后如果还是看不到菜单，请：
1. 清除浏览器缓存
2. 强制刷新页面（Ctrl+Shift+R 或 Cmd+Shift+R）
3. 完全退出浏览器重新打开

## 文件说明

- `sql/add_role_menu.sql` - 核心配置：添加 boss 角色和菜单权限
- `sql/test_users.sql` - 测试数据：创建测试用户

## 注意事项

1. 如果菜单ID已存在，请手动调整 SQL 中的ID
2. 执行前请备份数据库
3. 执行后需要**重新登录**才能看到新的菜单
4. 如果前端组件加载失败，请检查 `router/index.js` 中的组件路径是否正确
