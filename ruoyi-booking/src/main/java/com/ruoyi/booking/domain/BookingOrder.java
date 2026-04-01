package com.ruoyi.booking.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

public class BookingOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;           // 订单号
    private Long bookingId;            // 预约ID
    private String bookingNo;          // 预约单号
    private Long userId;               // 用户ID
    private Long deptId;               // 门店ID
    private BigDecimal amount;          // 订单金额
    private BigDecimal paidAmount;      // 实付金额
    private String subject;             // 订单标题
    private String body;                // 订单描述
    private Integer status;             // 0待支付 1支付中 2已支付 3已取消 4已退款
    private String payMethod;           // 支付方式：alipay/wechat/wallet
    private String transactionId;       // 第三方支付流水号
    private Date expireTime;            // 过期时间
    private Date payTime;               // 支付时间
    private Date cancelTime;            // 取消时间
    private String callbackData;        // 回调数据
    // ==================== 新增字段（用于支付成功后创建预约） ====================

    /**
     * 车牌号码
     */
    private String carNumber;

    /**
     * 车辆型号
     */
    private String carModel;

    /**
     * 车辆颜色
     */
    private String carColor;

    /**
     * 预约日期
     */
    private Date workDate;

    /**
     * 预约时段（如：14:30）
     */
    private String slot;

    /**
     * 车位编号
     */
    private String spaceNo;

    /**
     * 套餐分钟数（30/60/90）
     */
    private Integer comboMinutes;

// ==================== 新增字段的 getter/setter ====================

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getSpaceNo() {
        return spaceNo;
    }

    public void setSpaceNo(String spaceNo) {
        this.spaceNo = spaceNo;
    }

    public Integer getComboMinutes() {
        return comboMinutes;
    }

    public void setComboMinutes(Integer comboMinutes) {
        this.comboMinutes = comboMinutes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingNo() { return bookingNo; }
    public void setBookingNo(String bookingNo) { this.bookingNo = bookingNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public Date getPayTime() { return payTime; }
    public void setPayTime(Date payTime) { this.payTime = payTime; }
    public Date getCancelTime() { return cancelTime; }
    public void setCancelTime(Date cancelTime) { this.cancelTime = cancelTime; }
    public String getCallbackData() { return callbackData; }
    public void setCallbackData(String callbackData) { this.callbackData = callbackData; }
}