package com.ruoyi.booking.service;

import com.ruoyi.booking.domain.CarBooking;
import com.ruoyi.booking.domain.DeptCarSpace;
import com.ruoyi.booking.domain.DeptSchedule;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IBookingService {

    /* ========== 车位 ========== */
    List<DeptCarSpace> selectCarSpaceList(DeptCarSpace space);
    List<DeptCarSpace> selectCarSpaceByDeptId(Long deptId);
    int insertCarSpace(DeptCarSpace space);
    int updateCarSpace(DeptCarSpace space);
    int deleteCarSpaceByIds(Long[] ids);

    /* ========== 排班 ========== */
    List<DeptSchedule> selectScheduleList(DeptSchedule schedule);
    List<DeptSchedule> selectScheduleByDeptIdAndDateRange(Long deptId, Date startDate, Date endDate);
    DeptSchedule selectScheduleByDeptIdAndDate(Long deptId, Date workDate);
    int insertSchedule(DeptSchedule schedule);
    int updateSchedule(DeptSchedule schedule);
    int deleteScheduleByIds(Long[] ids);
    void initScheduleForDept(Long deptId, int days);

    /* ========== 预约 ========== */
    List<CarBooking> selectBookingList(CarBooking booking);
    List<CarBooking> selectBookingByDeptIdAndDate(Long deptId, Date workDate);
    Map<String, Object> getCalendarData(Long deptId, Date startDate);
    Map<String, Object> createBooking(CarBooking booking);
    boolean verifyBooking(String code);
    boolean cancelBooking(Long bookingId);
}