package com.ruoyi.booking.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.ruoyi.booking.domain.BookingOrder;
import com.ruoyi.booking.domain.CarBooking;
import com.ruoyi.booking.domain.dto.CreateOrderDTO;
import com.ruoyi.booking.domain.dto.PayDTO;
import com.ruoyi.booking.mapper.BookingMapper;
import com.ruoyi.booking.mapper.BookingOrderMapper;
import com.ruoyi.booking.service.IBookingOrderService;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private BookingMapper bookingMapper;

    @Autowired
    private ISysDeptService deptService;

    @Value("${ruoyi.profile}")
    private String profile;

    // 套餐价格映射
    private static final Map<Integer, BigDecimal> COMBO_PRICE = new HashMap<>();
    static {
        COMBO_PRICE.put(30, new BigDecimal(30));
        COMBO_PRICE.put(60, new BigDecimal(80));
        COMBO_PRICE.put(90, new BigDecimal(120));
    }

    @Override
    @Transactional
    public Map<String, Object> createOrder(CreateOrderDTO dto) {
        Map<String, Object> result = new HashMap<>();

        String orderNo = generateOrderNo();
        BigDecimal amount = COMBO_PRICE.get(dto.getComboMinutes());
        if (amount == null) {
            amount = new BigDecimal(30);
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 15);
        Date expireTime = cal.getTime();

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
        order.setStatus(0);
        order.setExpireTime(expireTime);
        order.setCarNumber(dto.getCarNumber());
        order.setCarModel(dto.getCarModel());
        order.setCarColor(dto.getCarColor());
        order.setWorkDate(dto.getWorkDate());
        order.setSlot(dto.getSlot());
        order.setSpaceNo(dto.getSpaceNo());
        order.setComboMinutes(dto.getComboMinutes());

        orderMapper.insertBookingOrder(order);

        result.put("orderNo", orderNo);
        result.put("amount", amount);
        result.put("expireTime", expireTime);
        return result;
    }

    @Override
    public Map<String, Object> pay(PayDTO dto) {
        Map<String, Object> result = new HashMap<>();

        BookingOrder order = orderMapper.selectBookingOrderByNo(dto.getOrderNo());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态错误");
        }

        if (order.getExpireTime().before(new Date())) {
            orderMapper.updateStatusByOrderNo(dto.getOrderNo(), 3, null);
            throw new RuntimeException("订单已过期");
        }

        SysDept dept = deptService.selectDeptById(order.getDeptId());

        System.out.println("========== 支付宝配置调试 ==========");
        System.out.println("门店ID: " + dept.getDeptId());
        System.out.println("门店名称: " + dept.getDeptName());
        System.out.println("alipayAppId: " + dept.getAlipayAppId());
        System.out.println("alipayGateway: " + dept.getAlipayGateway());
        System.out.println("===================================");

        if (dept == null || dept.getAlipayAppId() == null) {
            throw new RuntimeException("门店未配置支付宝支付");
        }

        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    dept.getAlipayGateway(),
                    dept.getAlipayAppId(),
                    dept.getAlipayPrivateKey(),
                    "json",
                    "UTF-8",
                    dept.getAlipayPublicKey(),
                    "RSA2"
            );

            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl("http://localhost:8080/booking/order/callback/alipay");
            // 尝试使用局域网 IP
            request.setReturnUrl("http://10.252.50.143:1024/");

            request.setBizContent("{" +
                    "\"out_trade_no\":\"" + order.getOrderNo() + "\"," +
                    "\"total_amount\":\"" + order.getAmount() + "\"," +
                    "\"subject\":\"" + order.getSubject() + "\"," +
                    "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"" +
                    "}");

            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

            if (response.isSuccess()) {
                result.put("payForm", response.getBody());
                result.put("payUrl", response.getBody());
            } else {
                throw new RuntimeException("支付宝支付失败：" + response.getMsg());
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new RuntimeException("支付宝支付异常：" + e.getMessage());
        }

        order.setStatus(1);
        order.setPayMethod(dto.getPayMethod());
        orderMapper.updateBookingOrder(order);

        return result;
    }

    @Override
    @Transactional
    public String handlePayCallback(String paymentMethod, String callbackData) {
        Map<String, String> params = parseCallbackData(callbackData);
        String orderNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");

        System.out.println("========== 支付宝回调 ==========");
        System.out.println("订单号: " + orderNo);
        System.out.println("支付状态: " + tradeStatus);
        System.out.println("=================================");

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 更新订单状态
            orderMapper.updateStatusByOrderNo(orderNo, 2, new Date());

            // 获取订单信息
            BookingOrder order = orderMapper.selectBookingOrderByNo(orderNo);

            if (order == null) {
                System.out.println("订单不存在: " + orderNo);
                return "success";
            }

            // 创建预约记录（如果还没有创建）
            if (order.getBookingNo() == null) {
                try {
                    CarBooking booking = new CarBooking();
                    booking.setUserId(order.getUserId());
                    booking.setDeptId(order.getDeptId());
                    booking.setSpaceNo(order.getSpaceNo());
                    booking.setWorkDate(order.getWorkDate());

                    Date startTime = parseStartTime(order.getWorkDate(), order.getSlot());
                    booking.setStartTime(startTime);
                    booking.setEndTime(calculateEndTime(startTime, order.getComboMinutes()));

                    booking.setCarNumber(order.getCarNumber());
                    booking.setCarModel(order.getCarModel());
                    booking.setCarColor(order.getCarColor());
                    booking.setComboMinutes(order.getComboMinutes());
                    booking.setComboName(getComboName(order.getComboMinutes()));
                    booking.setStatus(0);

                    booking.setBookingNo(generateBookingNo());
                    booking.setCode(generateCode());
                    booking.setCreateBy(String.valueOf(order.getUserId()));

                    bookingMapper.insertBooking(booking);

                    order.setBookingId(booking.getId());
                    order.setBookingNo(booking.getBookingNo());
                    orderMapper.updateBookingOrder(order);

                    System.out.println("✅ 预约创建成功！预约单号: " + booking.getBookingNo());

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("❌ 创建预约失败: " + e.getMessage());
                }
            } else {
                System.out.println("预约已存在，跳过创建: " + order.getBookingNo());
            }
        }

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

        // 如果订单状态是发起支付(1)，尝试查询支付宝状态
        if (order.getStatus() == 1) {
            try {
                SysDept dept = deptService.selectDeptById(order.getDeptId());
                if (dept != null && dept.getAlipayAppId() != null) {
                    AlipayClient alipayClient = new DefaultAlipayClient(
                            dept.getAlipayGateway(),
                            dept.getAlipayAppId(),
                            dept.getAlipayPrivateKey(),
                            "json",
                            "UTF-8",
                            dept.getAlipayPublicKey(),
                            "RSA2"
                    );

                    AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                    request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");

                    AlipayTradeQueryResponse response = alipayClient.execute(request);

                    if (response.isSuccess() && "TRADE_SUCCESS".equals(response.getTradeStatus())) {
                        // 支付成功，更新订单状态
                        orderMapper.updateStatusByOrderNo(orderNo, 2, new Date());
                        order.setStatus(2);
                        order.setPayTime(new Date());
                        
                        // 创建预约记录（如果还没有创建）
                        createBookingFromOrder(order);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        result.put("status", order.getStatus());
        result.put("payTime", order.getPayTime());
        result.put("amount", order.getAmount());

        return result;
    }
    
    /**
     * 根据订单创建预约记录
     */
    private void createBookingFromOrder(BookingOrder order) {
        // 如果已经有预约号，跳过
        if (order.getBookingNo() != null) {
            System.out.println("预约已存在，跳过创建: " + order.getBookingNo());
            return;
        }
        
        try {
            CarBooking booking = new CarBooking();
            booking.setUserId(order.getUserId());
            booking.setDeptId(order.getDeptId());
            booking.setSpaceNo(order.getSpaceNo());
            booking.setWorkDate(order.getWorkDate());

            Date startTime = parseStartTime(order.getWorkDate(), order.getSlot());
            booking.setStartTime(startTime);
            booking.setEndTime(calculateEndTime(startTime, order.getComboMinutes()));

            booking.setCarNumber(order.getCarNumber());
            booking.setCarModel(order.getCarModel());
            booking.setCarColor(order.getCarColor());
            booking.setComboMinutes(order.getComboMinutes());
            booking.setComboName(getComboName(order.getComboMinutes()));
            booking.setStatus(0);

            booking.setBookingNo(generateBookingNo());
            booking.setCode(generateCode());
            booking.setCreateBy(String.valueOf(order.getUserId()));

            bookingMapper.insertBooking(booking);

            order.setBookingId(booking.getId());
            order.setBookingNo(booking.getBookingNo());
            orderMapper.updateBookingOrder(order);

            System.out.println("✅ [getPayStatus] 预约创建成功！预约单号: " + booking.getBookingNo());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ [getPayStatus] 创建预约失败: " + e.getMessage());
        }
    }

    @Override
    public boolean cancelOrder(String orderNo) {
        return orderMapper.updateStatusByOrderNo(orderNo, 3, null) > 0;
    }

    @Override
    public void processExpiredOrders() {
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

    private String generateBookingNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "BK" + sdf.format(new Date()) + String.format("%04d", new Random().nextInt(10000));
    }

    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private Date parseStartTime(Date workDate, String slot) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(workDate);
            return sdf.parse(dateStr + " " + slot + ":00");
        } catch (Exception e) {
            e.printStackTrace();
            return new Date();
        }
    }

    private Date calculateEndTime(Date startTime, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }

    private String getComboName(Integer minutes) {
        if (minutes == null) return "标准洗车";
        if (minutes == 30) return "标准洗车";
        if (minutes == 60) return "精洗+打蜡";
        if (minutes == 90) return "全套护理";
        return "洗车服务";
    }

    private Map<String, String> parseCallbackData(String callbackData) {
        Map<String, String> result = new HashMap<>();
        try {
            // 尝试解析 key=value&key=value 格式
            if (callbackData.contains("=") && callbackData.contains("&")) {
                String[] pairs = callbackData.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length >= 2) {
                        result.put(kv[0], kv[1]);
                    }
                }
            }
            // 如果上面解析不到，尝试解析 JSON 格式
            if (result.isEmpty() && callbackData.startsWith("{")) {
                com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSON.parseObject(callbackData);
                for (String key : json.keySet()) {
                    result.put(key, json.getString(key));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}