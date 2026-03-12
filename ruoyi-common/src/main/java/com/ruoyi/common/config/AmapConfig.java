package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 高德地图配置类
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "amap")
public class AmapConfig {

    /** 高德地图Key */
    private String key;

    /** 高德地图安全密钥 */
    private String securityKey;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    @Override
    public String toString() {
        return "AmapConfig{" +
                "key='" + key + '\'' +
                ", securityKey='" + securityKey + '\'' +
                '}';
    }
}