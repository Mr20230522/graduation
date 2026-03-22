package com.ruoyi.booking.controller;



import com.ruoyi.booking.domain.DeptCarSpace;
import com.ruoyi.booking.domain.DeptSchedule;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.booking.domain.BossRevenueVO;
import com.ruoyi.booking.domain.BossStatisticsVO;
import com.ruoyi.booking.service.IBossService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Boss管理Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/boss")
public class BossController extends BaseController
{
    @Autowired
    private IBossService bossService;

    @Autowired
    private ISysDeptService sysDeptService;

    /**
     * 获取当前老板的门店信息
     */
    @GetMapping("/deptInfo")
    public AjaxResult getDeptInfo()
    {
        Long deptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        SysDept dept = sysDeptService.selectDeptById(deptId);
        return success(dept);
    }

    /**
     * 获取统计数据（总营收、总订单数、总评价数、平均评分）
     */
    @GetMapping("/statistics")
    public AjaxResult getStatistics()
    {
        Long deptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        BossStatisticsVO statistics = bossService.getStatistics(deptId);
        return success(statistics);
    }

    /**
     * 获取营收趋势（近7天/30天）
     */
    @GetMapping("/revenueTrend")
    public AjaxResult getRevenueTrend(@RequestParam Integer days)
    {
        Long deptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        List<BossRevenueVO> revenueList = bossService.getRevenueTrend(deptId, days);
        return success(revenueList);
    }

    /**
     * 获取评价列表
     */
    @GetMapping("/reviewList")
    public TableDataInfo getReviewList(@RequestParam Map<String, Object> params)
    {
        Long deptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        startPage();
        List<Map<String, Object>> list = bossService.getReviewList(deptId, params);
        return getDataTable(list);
    }

    /**
     * 回复评价
     */
    @Log(title = "评价回复", businessType = BusinessType.UPDATE)
    @PostMapping("/replyReview")
    public AjaxResult replyReview(@RequestBody Map<String, Object> params)
    {
        Long reviewId = Long.valueOf(params.get("reviewId").toString());
        String replyContent = params.get("replyContent").toString();
        Long userId = SecurityUtils.getUserId();
        return toAjax(bossService.replyReview(reviewId, replyContent, userId));
    }

    /**
     * 更新门店信息
     */
    @Log(title = "门店信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updateDept")
    public AjaxResult updateDept(@RequestBody SysDept dept)
    {
        Long deptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        dept.setDeptId(deptId);
        return toAjax(sysDeptService.updateDept(dept));
    }

    /**
     * 获取门店排班信息
     */
    @GetMapping("/schedule")
    public AjaxResult getSchedule()
    {
        Long deptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        List<DeptSchedule> scheduleList = bossService.getSchedule(deptId);
        return success(scheduleList);
    }

    /**
     * 更新门店排班
     */
    @Log(title = "门店排班", businessType = BusinessType.UPDATE)
    @PostMapping("/updateSchedule")
    public AjaxResult updateSchedule(@RequestBody List<Map<String, Object>> scheduleList)
    {
        Long deptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        return toAjax(bossService.updateSchedule(deptId, scheduleList));
    }

    /**
     * 获取车位列表
     */
    @GetMapping("/carSpaces")
    public AjaxResult getCarSpaces()
    {
        Long deptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        List<DeptCarSpace> carSpaces = bossService.getCarSpaces(deptId);
        return success(carSpaces);
    }

    /**
     * 更新车位状态
     */
    @Log(title = "车位管理", businessType = BusinessType.UPDATE)
    @PutMapping("/updateCarSpace")
    public AjaxResult updateCarSpace(@RequestBody Map<String, Object> params)
    {
        Long deptId = SecurityUtils.getLoginUser().getUser().getDeptId();
        String spaceNo = params.get("spaceNo").toString();
        Integer status = Integer.parseInt(params.get("status").toString());
        return toAjax(bossService.updateCarSpace(deptId, spaceNo, status));
    }
}