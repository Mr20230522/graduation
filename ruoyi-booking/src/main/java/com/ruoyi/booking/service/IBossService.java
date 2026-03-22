package com.ruoyi.booking.service;

import com.ruoyi.booking.domain.BossRevenueVO;
import com.ruoyi.booking.domain.BossStatisticsVO;
import com.ruoyi.booking.domain.DeptCarSpace;
import com.ruoyi.booking.domain.DeptSchedule;

import java.util.List;
import java.util.Map;

public interface IBossService
{
    /**
     * 获取门店统计数据
     */
    BossStatisticsVO getStatistics(Long deptId);

    /**
     * 获取营收趋势
     */
    List<BossRevenueVO> getRevenueTrend(Long deptId, Integer days);

    /**
     * 获取评价列表
     */
    List<Map<String, Object>> getReviewList(Long deptId, Map<String, Object> params);

    /**
     * 回复评价
     */
    int replyReview(Long reviewId, String replyContent, Long userId);

    /**
     * 获取门店排班
     */
    List<DeptSchedule> getSchedule(Long deptId);

    /**
     * 更新门店排班
     */
    int updateSchedule(Long deptId, List<Map<String, Object>> scheduleList);

    /**
     * 获取车位列表
     */
    List<DeptCarSpace> getCarSpaces(Long deptId);

    /**
     * 更新车位状态
     */
    int updateCarSpace(Long deptId, String spaceNo, Integer status);
}