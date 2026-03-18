package com.ruoyi.booking.controller;

import com.ruoyi.booking.domain.CarBooking;
import com.ruoyi.booking.domain.dto.CreateOrderDTO;
import com.ruoyi.booking.domain.dto.PayDTO;
import com.ruoyi.booking.mapper.BookingMapper;
import com.ruoyi.booking.service.IBookingOrderService;
import com.ruoyi.booking.service.IBookingService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController extends BaseController {

    @Autowired
    private IBookingService bookingService;
    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private IBookingOrderService bookingOrderService;

    /** 获取7天日历数据 */
    @GetMapping("/calendar")
    public AjaxResult getCalendar(@RequestParam Long deptId,
                                  @RequestParam(required = false) Date startDate) {
        if (startDate == null) startDate = new Date();
        return AjaxResult.success(bookingService.getCalendarData(deptId, startDate));
    }

    /** 获取车位列表 */
    @GetMapping("/space/list")
    public AjaxResult listCarSpace(@RequestParam Long deptId) {
        return AjaxResult.success(bookingService.selectCarSpaceByDeptId(deptId));
    }

    /** 获取指定日期的预约列表（用于前端判断红绿色） */
    @GetMapping("/list")
    public AjaxResult listBooking(@RequestParam Long deptId, @RequestParam String workDate) {
        CarBooking query = new CarBooking();
        query.setDeptId(deptId);
        query.setWorkDate(java.sql.Date.valueOf(workDate));
        List<CarBooking> list = bookingMapper.selectBookingList(query);
        return AjaxResult.success(list);
    }

    /**
     * 创建预约（支付成功后调用）
     * 兼容旧版：如果没有订单号，传null即可
     */
    @PostMapping("/create")
    public AjaxResult createBooking(@RequestBody CarBooking booking,
                                    @RequestParam(required = false) String orderNo) {
        return AjaxResult.success(bookingService.createBooking(booking, orderNo));
    }

    /**
     * 兼容旧版：无订单号的创建预约
     * 为了不让前端修改，保留这个接口
     */
    @PostMapping("/create/old")
    public AjaxResult createBookingOld(@RequestBody CarBooking booking) {
        return AjaxResult.success(bookingService.createBooking(booking, null));
    }

    /** 取消预约 */
    @PostMapping("/cancel/{bookingId}")
    public AjaxResult cancelBooking(@PathVariable Long bookingId) {
        return toAjax(bookingService.cancelBooking(bookingId));
    }

    /** 核销预约 */
    @PostMapping("/verify")
    public AjaxResult verifyBooking(@RequestParam String code) {
        return toAjax(bookingService.verifyBooking(code));
    }

    /** 我的预约列表 */
    @GetMapping("/my/list")
    public AjaxResult listMyBooking(@RequestParam(required = false) List<Integer> statusList,
                                    @RequestParam(required = false, defaultValue = "false") boolean onlyNotExpired) {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(bookingService.selectMyBookingList(userId, statusList, onlyNotExpired));
    }

    /** 历史记录 */
    @GetMapping("/my/history")
    public AjaxResult listMyHistory(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(bookingService.selectMyHistoryList(userId, pageNum, pageSize));
    }

    /**
     * 创建订单（预约前）
     */
    @PostMapping("/order/create")
    public AjaxResult createOrder(@RequestBody CreateOrderDTO dto) {
        return AjaxResult.success(bookingOrderService.createOrder(dto));
    }

    /**
     * 发起支付
     */
    @PostMapping("/order/pay")
    public AjaxResult pay(@RequestBody PayDTO dto) {
        return AjaxResult.success(bookingOrderService.pay(dto));
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/order/status/{orderNo}")
    public AjaxResult getPayStatus(@PathVariable String orderNo) {
        return AjaxResult.success(bookingOrderService.getPayStatus(orderNo));
    }

    /**
     * 支付回调（支付宝/微信异步通知）
     */
    @PostMapping("/order/callback/{paymentMethod}")
    public String payCallback(@PathVariable String paymentMethod, @RequestBody String callbackData) {
        return bookingOrderService.handlePayCallback(paymentMethod, callbackData);
    }

    /**
     * 取消订单
     */
    @PostMapping("/order/cancel/{orderNo}")
    public AjaxResult cancelOrder(@PathVariable String orderNo) {
        return AjaxResult.success(bookingOrderService.cancelOrder(orderNo));
    }
}