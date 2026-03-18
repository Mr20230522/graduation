package com.ruoyi.booking.service.impl;

import com.ruoyi.booking.domain.BookingOrder;
import com.ruoyi.booking.domain.BookingPaymentRecord;
import com.ruoyi.booking.domain.CarBooking;
import com.ruoyi.booking.domain.dto.CreateOrderDTO;
import com.ruoyi.booking.domain.dto.PayDTO;
import com.ruoyi.booking.mapper.BookingOrderMapper;
import com.ruoyi.booking.mapper.BookingPaymentRecordMapper;
import com.ruoyi.booking.mapper.BookingMapper;
import com.ruoyi.booking.service.IBookingOrderService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BookingOrderServiceImpl implements IBookingOrderService {

    @Autowired
    private BookingOrderMapper orderMapper;

    @Autowired
    private BookingPaymentRecordMapper paymentRecordMapper;

    @Autowired
    private BookingMapper bookingMapper;

    // 套餐价格映射
    private static final Map<Integer, BigDecimal> COMBO_PRICE = new HashMap<>();
    static {
        COMBO_PRICE.put(30, new BigDecimal(30));   // 标准洗车
        COMBO_PRICE.put(60, new BigDecimal(80));   // 精洗+打蜡
        COMBO_PRICE.put(90, new BigDecimal(120));  // 全套护理
    }

    @Override
    @Transactional
    public Map<String, Object> createOrder(CreateOrderDTO dto) {
        Map<String, Object> result = new HashMap<>();

        // 生成订单号
        String orderNo = generateOrderNo();

        // 计算金额
        BigDecimal amount = COMBO_PRICE.get(dto.getComboMinutes());
        if (amount == null) {
            amount = new BigDecimal(30);
        }

        // 设置过期时间（15分钟）
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 15);
        Date expireTime = cal.getTime();

        // 创建订单
        BookingOrder order = new BookingOrder();
        order.setOrderNo(orderNo);
        order.setUserId(SecurityUtils.getUserId());
        order.setDeptId(dto.getDeptId());
        order.setAmount(amount);
        order.setSubject("洗车预约 - " + dto.getCarNumber());
        order.setBody(String.format("预约时间：%s %s，车位：%s",
                new SimpleDateFormat("yyyy-MM-dd").format(dto.getWorkDate()),
                dto.getSlot(),
                dto.getSpaceNo()));
        order.setStatus(0);  // 待支付
        order.setExpireTime(expireTime);

        orderMapper.insertBookingOrder(order);

        result.put("orderNo", orderNo);
        result.put("amount", amount);
        result.put("expireTime", expireTime);
        return result;
    }

    @Override
    public Map<String, Object> pay(PayDTO dto) {
        Map<String, Object> result = new HashMap<>();

        // 获取订单
        BookingOrder order = orderMapper.selectBookingOrderByNo(dto.getOrderNo());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态错误");
        }

        // 检查是否过期
        if (order.getExpireTime().before(new Date())) {
            orderMapper.updateStatusByOrderNo(dto.getOrderNo(), 3, null); // 3-已取消
            throw new RuntimeException("订单已过期");
        }

        // TODO: 调用第三方支付接口
        // 这里模拟支付成功
        if ("alipay".equals(dto.getPayMethod()) || "wechat".equals(dto.getPayMethod())) {
            // 模拟返回二维码
            result.put("qrCode", "https://example.com/qrcode/" + dto.getOrderNo());
        } else {
            // 模拟返回跳转链接
            result.put("payUrl", "https://pay.example.com/" + dto.getOrderNo());
        }

        // 更新支付方式
        order.setPayMethod(dto.getPayMethod());
        order.setStatus(1); // 支付中
        orderMapper.updateBookingOrder(order);

        return result;
    }

    @Override
    @Transactional
    public String handlePayCallback(String paymentMethod, String callbackData) {
        // TODO: 解析回调数据，验证签名
        // 这里简化处理，假设支付成功

        // 从回调数据中获取订单号
        String orderNo = extractOrderNoFromCallback(callbackData);
        String transactionId = extractTransactionIdFromCallback(callbackData);

        // 更新订单状态
        orderMapper.updateStatusByOrderNo(orderNo, 2, new Date()); // 2-已支付

        // 记录支付记录
        BookingPaymentRecord record = new BookingPaymentRecord();
        record.setOrderNo(orderNo);
        record.setTransactionId(transactionId);
        record.setPayMethod(paymentMethod);
        record.setAmount(getAmountFromOrder(orderNo));
        record.setStatus(1); // 成功
        record.setCallbackData(callbackData);
        record.setPayTime(new Date());
        paymentRecordMapper.insertPaymentRecord(record);

        return "success";
    }

    @Override
    public Map<String, Object> getPayStatus(String orderNo) {
        Map<String, Object> result = new HashMap<>();
        BookingOrder order = orderMapper.selectBookingOrderByNo(orderNo);

        if (order == null) {
            result.put("status", -1);
            result.put("msg", "订单不存在");
            return result;
        }

        result.put("status", order.getStatus());
        result.put("payTime", order.getPayTime());
        result.put("amount", order.getAmount());

        // 如果已支付，查询是否已创建预约
        if (order.getStatus() == 2 && order.getBookingNo() != null) {
            result.put("bookingNo", order.getBookingNo());
        }

        return result;
    }

    @Override
    public boolean cancelOrder(String orderNo) {
        return orderMapper.updateStatusByOrderNo(orderNo, 3, null) > 0;
    }

    @Override
    @Transactional
    public void processExpiredOrders() {
        // 查询过期订单（待支付且超过过期时间）
        BookingOrder query = new BookingOrder();
        query.setStatus(0);
        List<BookingOrder> orders = orderMapper.selectBookingOrderList(query);

        Date now = new Date();
        for (BookingOrder order : orders) {
            if (order.getExpireTime().before(now)) {
                orderMapper.updateStatusByOrderNo(order.getOrderNo(), 3, null);
            }
        }
    }

    // ==================== 私有方法 ====================

    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "ORD" + sdf.format(new Date()) + String.format("%04d", new Random().nextInt(10000));
    }

    private String extractOrderNoFromCallback(String callbackData) {
        // TODO: 实际解析逻辑
        return "";
    }

    private String extractTransactionIdFromCallback(String callbackData) {
        // TODO: 实际解析逻辑
        return "";
    }

    private BigDecimal getAmountFromOrder(String orderNo) {
        BookingOrder order = orderMapper.selectBookingOrderByNo(orderNo);
        return order != null ? order.getAmount() : BigDecimal.ZERO;
    }
}