package com.ruoyi.common.utils;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.config.AmapConfig;
import com.ruoyi.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;

/**
 * 高德地图工具类 - 简单版
 */
@Component
public class AmapUtils {

    private static final Logger log = LoggerFactory.getLogger(AmapUtils.class);

    private static final String GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo";
    private static final String IP_URL = "https://restapi.amap.com/v3/ip";

    @Autowired
    private AmapConfig amapConfig;

    /**
     * 地址转坐标（地理编码）
     */
    public JSONObject addressToLngLat(String address) {
        try {
            // 直接拼接参数字符串
            String param = "key=" + amapConfig.getKey()
                    + "&address=" + URLEncoder.encode(address, "UTF-8")
                    + "&output=JSON";

            String result = HttpUtils.sendGet(GEOCODE_URL, param);
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("地址转坐标失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * IP定位
     */
    public JSONObject ipLocation(String ip) {
        try {
            // 基础参数
            String param = "key=" + amapConfig.getKey() + "&output=JSON";

            // 如果有IP，添加IP参数
            if (ip != null && !ip.isEmpty()) {
                param = param + "&ip=" + ip;
            }

            String result = HttpUtils.sendGet(IP_URL, param);
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("IP定位失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前服务器IP的地理位置
     */
    public JSONObject currentIpLocation() {
        return ipLocation(null);
    }
}