package com.ruoyi.booking.controller;

import com.ruoyi.booking.domain.CarBooking;
import com.ruoyi.booking.domain.DeptCarSpace;
import com.ruoyi.booking.domain.DeptSchedule;
import com.ruoyi.booking.service.IBookingService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/booking")
public class BookingController extends BaseController {

    @Autowired
    private IBookingService bookingService;

    /* ======== 前端用接口 ======== */

    /** 7 天日历数据 */
    @GetMapping("/calendar")
    public AjaxResult getCalendar(@RequestParam Long deptId,
                                  @RequestParam(required = false) Date startDate) {
        if (startDate == null) startDate = new Date();
        return AjaxResult.success(bookingService.getCalendarData(deptId, startDate));
    }

    /** 创建预约 */
    @PostMapping("/create")
    public AjaxResult createBooking(@RequestBody CarBooking booking) {
        Map<String, Object> res = bookingService.createBooking(booking);
        return (Boolean) res.get("success")
                ? AjaxResult.success("预约成功", res)
                : AjaxResult.error((String) res.get("msg"));
    }

    /** 核销预约 */
    @PostMapping("/verify")
    public AjaxResult verifyBooking(@RequestParam String code) {
        return bookingService.verifyBooking(code)
                ? AjaxResult.success("核销成功")
                : AjaxResult.error("验证码无效或已使用");
    }

    /** 取消预约 */
    @PostMapping("/cancel/{bookingId}")
    public AjaxResult cancelBooking(@PathVariable Long bookingId) {
        return toAjax(bookingService.cancelBooking(bookingId));
    }

    /* ======== 后台管理接口 ======== */

    @GetMapping("/space/list")
    public AjaxResult listCarSpace(DeptCarSpace space) {
        return AjaxResult.success(bookingService.selectCarSpaceList(space));
    }

    @PostMapping("/space")
    @Log(title = "车位管理", businessType = BusinessType.INSERT)
    public AjaxResult addCarSpace(@RequestBody DeptCarSpace space) {
        return toAjax(bookingService.insertCarSpace(space));
    }

    @PutMapping("/space")
    @Log(title = "车位管理", businessType = BusinessType.UPDATE)
    public AjaxResult editCarSpace(@RequestBody DeptCarSpace space) {
        return toAjax(bookingService.updateCarSpace(space));
    }

    @DeleteMapping("/space/{ids}")
    @Log(title = "车位管理", businessType = BusinessType.DELETE)
    public AjaxResult removeCarSpace(@PathVariable Long[] ids) {
        return toAjax(bookingService.deleteCarSpaceByIds(ids));
    }

    @GetMapping("/schedule/list")
    public AjaxResult listSchedule(DeptSchedule schedule) {
        return AjaxResult.success(bookingService.selectScheduleList(schedule));
    }

    @PostMapping("/schedule")
    @Log(title = "排班管理", businessType = BusinessType.INSERT)
    public AjaxResult addSchedule(@RequestBody DeptSchedule schedule) {
        return toAjax(bookingService.insertSchedule(schedule));
    }

    @PutMapping("/schedule")
    @Log(title = "排班管理", businessType = BusinessType.UPDATE)
    public AjaxResult editSchedule(@RequestBody DeptSchedule schedule) {
        return toAjax(bookingService.updateSchedule(schedule));
    }

    @DeleteMapping("/schedule/{ids}")
    @Log(title = "排班管理", businessType = BusinessType.DELETE)
    public AjaxResult removeSchedule(@PathVariable Long[] ids) {
        return toAjax(bookingService.deleteScheduleByIds(ids));
    }

    /** 初始化门店 7 天排班 */
    @PostMapping("/schedule/init/{deptId}")
    public AjaxResult initSchedule(@PathVariable Long deptId) {
        bookingService.initScheduleForDept(deptId, 7);
        return AjaxResult.success("初始化成功");
    }
}