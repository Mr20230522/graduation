package com.ruoyi.booking.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BookingDeptPaymentConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long deptId;                 // 门店ID
    private String companyName;           // 公司名称
    private String companyId;             // 营业执照号

    // 支付宝配置
    private String alipayAppId;            // 支付宝APPID
    private String alipayPublicKey;        // 支付宝公钥
    private String alipayPrivateKey;       // 商户私钥
    private String alipaySellerId;         // 支付宝卖家ID

    // 微信支付配置
    private String wechatAppId;            // 微信APPID
    private String wechatMchId;            // 微信商户号
    private String wechatApiKey;           // 微信API密钥
    private String wechatCertPath;         // 微信证书路径

    // 银行账户
    private String bankName;               // 开户行
    private String bankAccount;            // 银行账号
    private String bankHolder;             // 账户名称

    private Integer status;                // 0禁用 1启用
    private Integer isDefault;             // 是否默认

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getAlipayAppId() { return alipayAppId; }
    public void setAlipayAppId(String alipayAppId) { this.alipayAppId = alipayAppId; }
    public String getAlipayPublicKey() { return alipayPublicKey; }
    public void setAlipayPublicKey(String alipayPublicKey) { this.alipayPublicKey = alipayPublicKey; }
    public String getAlipayPrivateKey() { return alipayPrivateKey; }
    public void setAlipayPrivateKey(String alipayPrivateKey) { this.alipayPrivateKey = alipayPrivateKey; }
    public String getAlipaySellerId() { return alipaySellerId; }
    public void setAlipaySellerId(String alipaySellerId) { this.alipaySellerId = alipaySellerId; }
    public String getWechatAppId() { return wechatAppId; }
    public void setWechatAppId(String wechatAppId) { this.wechatAppId = wechatAppId; }
    public String getWechatMchId() { return wechatMchId; }
    public void setWechatMchId(String wechatMchId) { this.wechatMchId = wechatMchId; }
    public String getWechatApiKey() { return wechatApiKey; }
    public void setWechatApiKey(String wechatApiKey) { this.wechatApiKey = wechatApiKey; }
    public String getWechatCertPath() { return wechatCertPath; }
    public void setWechatCertPath(String wechatCertPath) { this.wechatCertPath = wechatCertPath; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getBankHolder() { return bankHolder; }
    public void setBankHolder(String bankHolder) { this.bankHolder = bankHolder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
}