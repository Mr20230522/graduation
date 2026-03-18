package com.ruoyi.booking.domain.dto;

public class PayDTO {
    private String orderNo;
    private String payMethod;  // alipay/wechat/wallet

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
}