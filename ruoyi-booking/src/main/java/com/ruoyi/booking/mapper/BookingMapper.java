package com.ruoyi.booking.mapper;

import com.ruoyi.booking.domain.CarBooking;
import com.ruoyi.booking.domain.DeptCarSpace;
import com.ruoyi.booking.domain.DeptSchedule;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;

public interface BookingMapper {

    /* ========== 车位 ========== */
    List<DeptCarSpace> selectCarSpaceList(DeptCarSpace space);
    List<DeptCarSpace> selectCarSpaceByDeptId(@Param("deptId") Long deptId);
    int insertCarSpace(DeptCarSpace space);
    int updateCarSpace(DeptCarSpace space);
    int deleteCarSpaceByIds(Long[] ids);

    /* ========== 排班 ========== */
    List<DeptSchedule> selectScheduleList(DeptSchedule schedule);
    List<DeptSchedule> selectScheduleByDeptIdAndDateRange(@Param("deptId") Long deptId,
                                                          @Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate);
    DeptSchedule selectScheduleByDeptIdAndDate(@Param("deptId") Long deptId,
                                               @Param("workDate") Date workDate);
    int insertSchedule(DeptSchedule schedule);
    int updateSchedule(DeptSchedule schedule);
    int deleteScheduleByIds(Long[] ids);

    /* ========== 预约 ========== */
    List<CarBooking> selectBookingList(CarBooking booking);
    List<CarBooking> selectBookingByDeptIdAndDate(@Param("deptId") Long deptId,
                                                  @Param("workDate") Date workDate);
    List<CarBooking> selectBookingByDeptIdAndDateRange(@Param("deptId") Long deptId,
                                                       @Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate);
    CarBooking selectBookingByCode(@Param("code") String code);
    int insertBooking(CarBooking booking);
    int updateBooking(CarBooking booking);
    int deleteBookingByIds(Long[] ids);

    /* 冲突检查 */
    int countConflictBooking(@Param("deptId") Long deptId,
                             @Param("spaceNo") String spaceNo,
                             @Param("workDate") Date workDate,
                             @Param("startTime") Date startTime,
                             @Param("endTime") Date endTime);
}