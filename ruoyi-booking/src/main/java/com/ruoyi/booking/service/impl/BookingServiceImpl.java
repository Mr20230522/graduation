package com.ruoyi.booking.service.impl;

import com.ruoyi.booking.domain.CarBooking;
import com.ruoyi.booking.domain.DeptCarSpace;
import com.ruoyi.booking.domain.DeptSchedule;
import com.ruoyi.booking.mapper.BookingMapper;
import com.ruoyi.booking.service.IBookingService;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BookingServiceImpl implements IBookingService {

    @Autowired
    private BookingMapper bookingMapper;

    // ==================== 车位管理 ====================
    @Override
    public List<DeptCarSpace> selectCarSpaceList(DeptCarSpace space) {
        return bookingMapper.selectCarSpaceList(space);
    }

    @Override
    public List<DeptCarSpace> selectCarSpaceByDeptId(Long deptId) {
        return bookingMapper.selectCarSpaceByDeptId(deptId);
    }

    @Override
    public int insertCarSpace(DeptCarSpace space) {
        return bookingMapper.insertCarSpace(space);
    }

    @Override
    public int updateCarSpace(DeptCarSpace space) {
        return bookingMapper.updateCarSpace(space);
    }

    @Override
    public int deleteCarSpaceByIds(Long[] ids) {
        return bookingMapper.deleteCarSpaceByIds(ids);
    }

    // ==================== 排班管理 ====================
    @Override
    public List<DeptSchedule> selectScheduleList(DeptSchedule schedule) {
        return bookingMapper.selectScheduleList(schedule);
    }

    @Override
    public List<DeptSchedule> selectScheduleByDeptIdAndDateRange(Long deptId, Date startDate, Date endDate) {
        return bookingMapper.selectScheduleByDeptIdAndDateRange(deptId, startDate, endDate);
    }

    @Override
    public DeptSchedule selectScheduleByDeptIdAndDate(Long deptId, Date workDate) {
        return bookingMapper.selectScheduleByDeptIdAndDate(deptId, workDate);
    }

    @Override
    public int insertSchedule(DeptSchedule schedule) {
        return bookingMapper.insertSchedule(schedule);
    }

    @Override
    public int updateSchedule(DeptSchedule schedule) {
        return bookingMapper.updateSchedule(schedule);
    }

    @Override
    public int deleteScheduleByIds(Long[] ids) {
        return bookingMapper.deleteScheduleByIds(ids);
    }

    @Override
    public void initScheduleForDept(Long deptId, int days) {
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < days; i++) {
            Date date = cal.getTime();
            if (selectScheduleByDeptIdAndDate(deptId, date) == null) {
                DeptSchedule s = new DeptSchedule();
                s.setDeptId(deptId);
                s.setWorkDate(date);
                s.setOpenTime("08:00:00");
                s.setCloseTime("20:00:00");
                s.setSlotMinutes(30);
                s.setCarSpaces(4);
                s.setStatus(0);
                insertSchedule(s);
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    // ==================== 预约核心 ====================
    @Override
    public Map<String, Object> getCalendarData(Long deptId, Date startDate) {
        Map<String, Object> result = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.DAY_OF_YEAR, 6);
        Date endDate = cal.getTime();

        // 查询排班
        List<DeptSchedule> schedules = selectScheduleByDeptIdAndDateRange(deptId, startDate, endDate);
        // 查询车位
        List<DeptCarSpace> spaces = selectCarSpaceByDeptId(deptId);
        // 查询预约
        List<CarBooking> bookings = bookingMapper.selectBookingByDeptIdAndDateRange(deptId, startDate, endDate);

        // 生成7天日期列表
        List<String> dateList = new ArrayList<>();
        cal.setTime(startDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            dateList.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        result.put("dates", dateList);

        // 组装每天的数据
        Map<String, Object> scheduleMap = new HashMap<>();
        for (String dateStr : dateList) {
            Map<String, Object> dayData = new HashMap<>();

            // 找当天的排班，没有就用默认
            DeptSchedule schedule = schedules.stream()
                    .filter(s -> sdf.format(s.getWorkDate()).equals(dateStr))
                    .findFirst()
                    .orElse(null);

            // 生成时段
            List<String> slots;
            if (schedule != null && schedule.getStatus() == 0) {
                slots = generateTimeSlots(schedule.getOpenTime(), schedule.getCloseTime(),
                        schedule.getSlotMinutes() != null ? schedule.getSlotMinutes() : 30);
            } else {
                slots = generateDefaultSlots();
            }
            dayData.put("slots", slots);

            // 车位列表
            List<String> spaceNos = new ArrayList<>();
            for (DeptCarSpace space : spaces) {
                if (space.getStatus() == 0) {
                    spaceNos.add(space.getSpaceNo());
                }
            }
            if (spaceNos.isEmpty()) {
                spaceNos = Arrays.asList("A1", "A2", "A3", "A4");
            }
            dayData.put("spaces", spaceNos);

            // 生成占用矩阵
            Map<String, List<String>> matrix = new HashMap<>();
            Date workDate;
            try {
                workDate = sdf.parse(dateStr);
            } catch (Exception e) {
                workDate = new Date();
            }

            for (String spaceNo : spaceNos) {
                List<String> row = new ArrayList<>();
                for (String slot : slots) {
                    boolean isBusy = checkSlotBusy(bookings, workDate, spaceNo, slot,
                            schedule != null && schedule.getSlotMinutes() != null ? schedule.getSlotMinutes() : 30);
                    row.add(isBusy ? "busy" : "free");
                }
                matrix.put(spaceNo, row);
            }
            dayData.put("matrix", matrix);
            scheduleMap.put(dateStr, dayData);
        }
        result.put("schedule", scheduleMap);
        return result;
    }

    private List<String> generateTimeSlots(String open, String close, int slotMinutes) {
        List<String> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat out = new SimpleDateFormat("HH:mm");
        try {
            Date start = sdf.parse(open);
            Date end = sdf.parse(close);
            Calendar c = Calendar.getInstance();
            c.setTime(start);
            while (c.getTime().before(end)) {
                list.add(out.format(c.getTime()));
                c.add(Calendar.MINUTE, slotMinutes);
            }
        } catch (Exception e) {
            return generateDefaultSlots();
        }
        return list;
    }

    private List<String> generateDefaultSlots() {
        List<String> slots = new ArrayList<>();
        for (int hour = 8; hour < 20; hour++) {
            slots.add(String.format("%02d:00", hour));
            slots.add(String.format("%02d:30", hour));
        }
        return slots;
    }

    private boolean checkSlotBusy(List<CarBooking> bookings, Date date, String spaceNo, String slotStr, int slotMinutes) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date slotStart = sdf.parse(slotStr);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            Calendar slotCal = Calendar.getInstance();
            slotCal.setTime(slotStart);
            slotCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
            slotCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
            slotCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
            slotStart = slotCal.getTime();

            Date slotEnd = new Date(slotStart.getTime() + slotMinutes * 60000);

            for (CarBooking b : bookings) {
                if (!date.equals(b.getWorkDate())) continue;
                if (!spaceNo.equals(b.getSpaceNo())) continue;
                if (b.getStatus() != 0 && b.getStatus() != 1) continue;
                if (b.getStartTime().before(slotEnd) && b.getEndTime().after(slotStart)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    @Transactional
    public Map<String, Object> createBooking(CarBooking booking) {
        Map<String, Object> result = new HashMap<>();

        // 计算结束时间
        Calendar c = Calendar.getInstance();
        c.setTime(booking.getStartTime());
        c.add(Calendar.MINUTE, booking.getComboMinutes());
        booking.setEndTime(c.getTime());

        // 冲突检查
        int conflict = bookingMapper.countConflictBooking(
                booking.getDeptId(), booking.getSpaceNo(), booking.getWorkDate(),
                booking.getStartTime(), booking.getEndTime());
        if (conflict > 0) {
            result.put("success", false);
            result.put("msg", "该时段已被预约");
            return result;
        }

        // 设置用户ID
        Long userId = SecurityUtils.getUserId();
        booking.setUserId(userId);
        booking.setCreateBy(String.valueOf(userId));

        // 生成预约号和验证码
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String bookingNo = "BK" + sdf.format(new Date()) + String.format("%04d", new Random().nextInt(10000));
        String code = String.format("%06d", new Random().nextInt(1000000));
        booking.setBookingNo(bookingNo);
        booking.setCode(code);
        booking.setStatus(0);

        bookingMapper.insertBooking(booking);
        result.put("success", true);
        result.put("bookingNo", bookingNo);
        result.put("code", code);
        return result;
    }

    @Override
    public boolean verifyBooking(String code) {
        CarBooking b = bookingMapper.selectBookingByCode(code);
        if (b == null || b.getStatus() != 0) return false;
        b.setStatus(1);
        return bookingMapper.updateBooking(b) > 0;
    }

    @Override
    public boolean cancelBooking(Long bookingId) {
        CarBooking b = new CarBooking();
        b.setId(bookingId);
        b.setStatus(3);
        return bookingMapper.updateBooking(b) > 0;
    }

    // ==================== 我的预约 ====================
    @Override
    public List<CarBooking> selectMyBookingList(Long userId, List<Integer> statusList, boolean onlyNotExpired) {
        Date now = new Date();
        return bookingMapper.selectMyBookingList(userId, statusList, onlyNotExpired, now);
    }

    @Override
    public Map<String, Object> selectMyHistoryList(Long userId, int pageNum, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        int offset = (pageNum - 1) * pageSize;
        List<CarBooking> list = bookingMapper.selectMyHistoryList(userId, offset, pageSize);
        int total = bookingMapper.countMyHistory(userId);
        result.put("list", list);
        result.put("total", total);
        return result;
    }
}