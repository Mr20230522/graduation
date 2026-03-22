package com.ruoyi.booking.mapper;

import com.ruoyi.booking.domain.BossRevenueVO;
import com.ruoyi.booking.domain.BossStatisticsVO;
import com.ruoyi.booking.domain.DeptCarSpace;
import com.ruoyi.booking.domain.DeptSchedule;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BossMapper
{
    /**
     * 获取门店统计数据
     */
    BossStatisticsVO getStatistics(@Param("deptId") Long deptId);

    /**
     * 获取营收趋势
     */
    List<BossRevenueVO> getRevenueTrend(@Param("deptId") Long deptId, @Param("days") Integer days);

    /**
     * 获取评价列表
     */
    List<Map<String, Object>> getReviewList(Map<String, Object> params);

    /**
     * 回复评价
     */
    int replyReview(@Param("reviewId") Long reviewId,
                    @Param("replyContent") String replyContent,
                    @Param("replyUserId") Long userId);

    /**
     * 获取门店排班 - 使用实体类
     */
    List<DeptSchedule> getSchedule(@Param("deptId") Long deptId);

    /**
     * 删除排班
     */
    int deleteSchedule(@Param("deptId") Long deptId);

    /**
     * 插入排班
     */
    int insertSchedule(Map<String, Object> schedule);

    /**
     * 获取车位列表 - 使用 DeptCarSpace 实体类
     */
    List<DeptCarSpace> getCarSpaces(@Param("deptId") Long deptId);

    /**
     * 更新车位状态
     */
    int updateCarSpace(@Param("deptId") Long deptId,
                       @Param("spaceNo") String spaceNo,
                       @Param("status") Integer status);
}