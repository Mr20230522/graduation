package com.ruoyi.framework.web.service;

import com.ruoyi.system.mapper.SysDeptMapper;
import nl.basjes.parse.useragent.utils.springframework.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.utils.*;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysUserService;

import java.util.List;

/**
 * 三身份统一注册（JDK8 + 无 MyBatis-Plus）
 */
@Component
public class SysRegisterService {

    @Autowired private ISysUserService userService;
    @Autowired private ISysDeptService deptService;
    @Autowired private SysUserRoleMapper userRoleMapper;
    @Autowired private ISysConfigService configService;
    @Autowired private RedisCache redisCache;
    @Autowired private SysDeptMapper deptMapper;

    /* ========= 统一入口 ========= */
    @Transactional(rollbackFor = Exception.class)
    public String register(RegisterBody body){
        /* 1. 通用校验 */
        String msg = commonCheck(body);
        if (StringUtils.isNotEmpty(msg)) return msg;

        /* 2. 构造用户对象（复用 SysUser 原有字段）*/
        SysUser user = new SysUser();
        user.setUserName(body.getUsername());
        user.setNickName(body.getUsername());
        user.setPassword(SecurityUtils.encryptPassword(body.getPassword()));
        user.setStatus("0");
        user.setDelFlag("0");
        user.setCreateTime(DateUtils.getNowDate());

        /* 3. 按身份分支处理 */
        if ("1".equals(body.getUserType())) {
            /* 老板：创建公司 */
            SysDept dept = createDept(body, user.getUserName());
            System.err.println("已经开始得到部门：" + dept);
            user.setDeptId(dept.getDeptId());
            userService.insertUser(user);
        } else if ("2".equals(body.getUserType())) {
            /* 员工：用公司名换 deptId 并校验 */
            Long deptId = findDeptIdByName(body.getCompanyName());
            if (deptId == null) {
                return "该公司不存在或尚未通过审核，无法注册员工账号";
            }
            user.setDeptId(deptId);
            userService.insertUser(user);
        } else {
            /* 普通用户 */
            userService.insertUser(user);
        }

        /* 4. 统一绑定角色（用户已入库，userId 已生成）*/
        bindRole(user.getUserId(), body.getUserType());

        /* 5. 日志 & 成功返回 */
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(
                user.getUserName(), Constants.REGISTER, "注册成功"));
        return "";
    }

    /* -------------------- 私有工具 -------------------- */
    private String commonCheck(RegisterBody body) {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            validateCaptcha(body.getUsername(), body.getCode(), body.getUuid());
        }
        if (StringUtils.isEmpty(body.getUsername())) {
            return "用户名不能为空";
        }
        if (StringUtils.isEmpty(body.getPassword())) {
            return "用户密码不能为空";
        }
        if (body.getUsername().length() < UserConstants.USERNAME_MIN_LENGTH
                || body.getUsername().length() > UserConstants.USERNAME_MAX_LENGTH) {
            return "账户长度必须在2到20个字符之间";
        }
        if (body.getPassword().length() < UserConstants.PASSWORD_MIN_LENGTH
                || body.getPassword().length() > UserConstants.PASSWORD_MAX_LENGTH) {
            return "密码长度必须在5到20个字符之间";
        }
        SysUser check = new SysUser();
        check.setUserName(body.getUsername());
        if (!userService.checkUserNameUnique(check)) {
            return "保存用户'" + body.getUsername() + "'失败，注册账号已存在";
        }
        return "";
    }

    private SysDept createDept(RegisterBody body, String createBy) {
        System.err.println("【注册】开始创建部门，公司名=" + body.getCompanyName());
        System.err.println("【注册】开始创建部门，公司地址=" + body.getAddress());
        SysDept dept = new SysDept();
        dept.setDeptName(body.getCompanyName());
        dept.setAddress(body.getAddress());
        dept.setLongitude(body.getLongitude());
        dept.setLatitude(body.getLatitude());
        dept.setLeader(body.getLegalPerson());
        dept.setEmail(body.getEmail());
        dept.setParentId(0L);           // ✅ 顶级门店
        dept.setAncestors("0");         // ✅ 祖级路径
        dept.setOrderNum(0);            // ✅ 排序
        dept.setStatus("0");            // ✅ 已审核
        dept.setDelFlag("0");           // ✅ 未删除
        dept.setCreateBy(createBy);
        dept.setCreateTime(DateUtils.getNowDate());
        dept.setLicense(body.getLicense());
        deptService.insertDept(dept);
        return dept;
    }

    private Long findDeptIdByName(String companyName) {
        // 1. 精确匹配，不走模糊
        List<SysDept> list = deptMapper.selectByNameExact(companyName);
        if (CollectionUtils.isEmpty(list)) return null;
        return list.get(0).getDeptId();
    }

    private void bindRole(Long userId, String userType) {
        Long roleId = "1".equals(userType) ? 100L :
                "2".equals(userType) ? 101L : 2L;
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        userRoleMapper.batchUserRole(java.util.Arrays.asList(ur));
    }

    private void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException();
        }
    }
}