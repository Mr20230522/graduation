package com.ruoyi.booking.service.impl;


import com.ruoyi.booking.domain.BossRevenueVO;
import com.ruoyi.booking.domain.BossStatisticsVO;
import com.ruoyi.booking.domain.DeptCarSpace;
import com.ruoyi.booking.domain.DeptSchedule;
import com.ruoyi.booking.service.IBossService;
import com.ruoyi.booking.mapper.BossMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BossServiceImpl implements IBossService
{
    @Autowired
    private BossMapper bossMapper;

    @Override
    public BossStatisticsVO getStatistics(Long deptId)
    {
        return bossMapper.getStatistics(deptId);
    }

    @Override
    public List<BossRevenueVO> getRevenueTrend(Long deptId, Integer days)
    {
        return bossMapper.getRevenueTrend(deptId, days);
    }

    @Override
    public List<Map<String, Object>> getReviewList(Long deptId, Map<String, Object> params)
    {
        params.put("deptId", deptId);
        return bossMapper.getReviewList(params);
    }

    @Override
    @Transactional
    public int replyReview(Long reviewId, String replyContent, Long userId)
    {
        return bossMapper.replyReview(reviewId, replyContent, userId);
    }

    @Override
    public List<DeptSchedule> getSchedule(Long deptId)
    {
        return bossMapper.getSchedule(deptId);
    }


    @Override
    @Transactional
    public int updateSchedule(Long deptId, List<Map<String, Object>> scheduleList)
    {
        // 先删除旧的排班
        bossMapper.deleteSchedule(deptId);
        // 批量插入新排班
        for (Map<String, Object> schedule : scheduleList)
        {
            schedule.put("deptId", deptId);
            bossMapper.insertSchedule(schedule);
        }
        return 1;
    }

    @Override
    public List<DeptCarSpace> getCarSpaces(Long deptId)
    {
        return bossMapper.getCarSpaces(deptId);
    }

    @Override
    public int updateCarSpace(Long deptId, String spaceNo, Integer status)
    {
        return bossMapper.updateCarSpace(deptId, spaceNo, status);
    }
}