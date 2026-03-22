package com.ruoyi.booking.domain;

import java.math.BigDecimal;

public class BossStatisticsVO
{
    private BigDecimal totalRevenue;  // 总营收
    private Integer totalOrders;       // 总订单数
    private Integer totalReviews;      // 总评价数
    private BigDecimal avgRating;      // 平均评分

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public BigDecimal getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(BigDecimal avgRating) {
        this.avgRating = avgRating;
    }
}