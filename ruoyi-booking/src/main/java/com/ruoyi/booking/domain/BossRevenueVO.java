package com.ruoyi.booking.domain;

import java.math.BigDecimal;

public class BossRevenueVO
{
    private String date;        // 日期
    private BigDecimal revenue; // 营收金额
    private Integer orderCount; // 订单数

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }
}