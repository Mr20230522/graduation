package com.ruoyi.booking.controller;

import com.ruoyi.booking.mapper.BookingDeptMapper;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店选择相关接口（无需权限控制）
 * 用于前端门店选择页面
 */
@Anonymous
@RestController
@RequestMapping("/booking/dept")
public class BookingDeptController extends BaseController {

    @Autowired
    private BookingDeptMapper bookingDeptMapper;

    /**
     * 获取营业中的门店列表（用于前端门店选择）
     * 无需权限，所有登录用户都可访问（绕过数据权限过滤）
     */
    @GetMapping("/list")
    public AjaxResult list(SysDept dept) {
        List<SysDept> depts = bookingDeptMapper.selectAllActiveDepts();
        return success(depts);
    }

    /**
     * 获取门店详情
     */
    @GetMapping("/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId) {
        return success(bookingDeptMapper.selectDeptById(deptId));
    }
}
