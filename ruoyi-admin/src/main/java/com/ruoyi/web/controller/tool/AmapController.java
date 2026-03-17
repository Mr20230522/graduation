package com.ruoyi.web.controller.tool;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.AmapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 高德地图测试Controller
 */
@RestController
@RequestMapping("/tool/amap")
public class AmapController extends BaseController {

    @Autowired
    private AmapUtils amapUtils;

    /**
     * 地址转坐标
     */
    @GetMapping("/testAddress")
    public AjaxResult testAddress(String address) {
        if (address == null) {
            address = "北京市朝阳区阜通东大街6号";
        }
        JSONObject result = amapUtils.addressToLngLat(address);
        return AjaxResult.success(result);
    }

    /**
     * IP定位
     */
    @GetMapping("/testIp")
    public AjaxResult testIp(String ip) {
        JSONObject result = amapUtils.ipLocation(ip);
        return AjaxResult.success(result);
    }
}