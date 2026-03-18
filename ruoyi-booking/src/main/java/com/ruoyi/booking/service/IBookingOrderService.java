package com.ruoyi.booking.service;

import com.ruoyi.booking.domain.BookingOrder;
import com.ruoyi.booking.domain.dto.CreateOrderDTO;
import com.ruoyi.booking.domain.dto.PayDTO;
import java.util.Map;

public interface IBookingOrderService {

    /**
     * 创建订单（预约前）
     */
    Map<String, Object> createOrder(CreateOrderDTO dto);

    /**
     * 发起支付
     */
    Map<String, Object> pay(PayDTO dto);

    /**
     * 支付回调处理
     */
    String handlePayCallback(String paymentMethod, String callbackData);

    /**
     * 查询支付状态
     */
    Map<String, Object> getPayStatus(String orderNo);

    /**
     * 取消订单
     */
    boolean cancelOrder(String orderNo);

    /**
     * 订单过期处理
     */
    void processExpiredOrders();
}