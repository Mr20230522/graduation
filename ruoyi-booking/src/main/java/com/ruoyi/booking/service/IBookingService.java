package com.ruoyi.booking.service;

import com.ruoyi.booking.domain.CarBooking;
import com.ruoyi.booking.domain.DeptCarSpace;
import com.ruoyi.booking.domain.DeptSchedule;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IBookingService {

    /* ========== 车位管理 ========== */
    List<DeptCarSpace> selectCarSpaceList(DeptCarSpace space);
    List<DeptCarSpace> selectCarSpaceByDeptId(Long deptId);
    int insertCarSpace(DeptCarSpace space);
    int updateCarSpace(DeptCarSpace space);
    int deleteCarSpaceByIds(Long[] ids);

    /* ========== 排班管理 ========== */
    List<DeptSchedule> selectScheduleList(DeptSchedule schedule);
    List<DeptSchedule> selectScheduleByDeptIdAndDateRange(Long deptId, Date startDate, Date endDate);
    DeptSchedule selectScheduleByDeptIdAndDate(Long deptId, Date workDate);
    int insertSchedule(DeptSchedule schedule);
    int updateSchedule(DeptSchedule schedule);
    int deleteScheduleByIds(Long[] ids);
    void initScheduleForDept(Long deptId, int days);

    /* ========== 预约核心 ========== */
    Map<String, Object> getCalendarData(Long deptId, Date startDate);
    Map<String, Object> createBooking(CarBooking booking);
    boolean verifyBooking(String code);
    boolean cancelBooking(Long bookingId);

    /* ========== 我的预约 ========== */
    List<CarBooking> selectMyBookingList(Long userId, List<Integer> statusList, boolean onlyNotExpired);
    Map<String, Object> selectMyHistoryList(Long userId, int pageNum, int pageSize);
}