package com.ruoyi.booking.service.impl;

import com.ruoyi.booking.domain.CarBooking;
import com.ruoyi.booking.domain.DeptCarSpace;
import com.ruoyi.booking.domain.DeptSchedule;
import com.ruoyi.booking.mapper.BookingMapper;
import com.ruoyi.booking.service.IBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BookingServiceImpl implements IBookingService {

    @Autowired
    private BookingMapper bookingMapper;

    // ==================== 车位 ====================
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

    // ==================== 排班 ====================
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
    public List<CarBooking> selectBookingList(CarBooking booking) {
        return bookingMapper.selectBookingList(booking);
    }
    @Override
    public List<CarBooking> selectBookingByDeptIdAndDate(Long deptId, Date workDate) {
        return bookingMapper.selectBookingByDeptIdAndDate(deptId, workDate);
    }

    @Override
    public Map<String, Object> getCalendarData(Long deptId, Date startDate) {
        Map<String, Object> result = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.DAY_OF_YEAR, 6);
        Date endDate = cal.getTime();

        List<DeptSchedule> schedules = selectScheduleByDeptIdAndDateRange(deptId, startDate, endDate);
        List<DeptCarSpace> spaces = selectCarSpaceByDeptId(deptId);
        List<CarBooking> bookings = bookingMapper.selectBookingByDeptIdAndDateRange(deptId, startDate, endDate);

        List<String> dateList = new ArrayList<>();
        cal.setTime(startDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            dateList.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        result.put("dates", dateList);

        Map<String, Object> scheduleMap = new HashMap<>();
        for (DeptSchedule schedule : schedules) {
            if (schedule.getStatus() == 1) continue;
            String dateStr = sdf.format(schedule.getWorkDate());
            Map<String, Object> dayData = new HashMap<>();

            List<String> slots = generateTimeSlots(schedule.getOpenTime(), schedule.getCloseTime(), schedule.getSlotMinutes());
            dayData.put("slots", slots);

            List<String> spaceNos = new ArrayList<>();
            int limit = Math.min(schedule.getCarSpaces(), spaces.size());
            for (int i = 0; i < limit; i++) {
                if (spaces.get(i).getStatus() == 0) spaceNos.add(spaces.get(i).getSpaceNo());
            }
            dayData.put("spaces", spaceNos);

            Map<String, List<String>> matrix = new HashMap<>();
            for (String spaceNo : spaceNos) {
                List<String> row = new ArrayList<>();
                for (String slot : slots) {
                    row.add(checkSlotBusy(bookings, schedule.getWorkDate(), spaceNo, slot, schedule.getSlotMinutes()) ? "busy" : "free");
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
            list.add("08:00");
            list.add("08:30");
        }
        return list;
    }

    private boolean checkSlotBusy(List<CarBooking> bookings, Date date, String spaceNo, String slotStr, int slotMinutes) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date slotStart = sdf.parse(slotStr);
            Calendar c = Calendar.getInstance();
            c.setTime(slotStart);
            c.add(Calendar.MINUTE, slotMinutes);
            Date slotEnd = c.getTime();

            for (CarBooking b : bookings) {
                if (!date.equals(b.getWorkDate())) continue;
                if (!spaceNo.equals(b.getSpaceNo())) continue;
                if (b.getStatus() != 0 && b.getStatus() != 1) continue;
                if (b.getStartTime().before(slotEnd) && b.getEndTime().after(slotStart)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    @Transactional
    public Map<String, Object> createBooking(CarBooking booking) {
        Map<String, Object> result = new HashMap<>();
        Calendar c = Calendar.getInstance();
        c.setTime(booking.getStartTime());
        c.add(Calendar.MINUTE, booking.getComboMinutes());
        booking.setEndTime(c.getTime());

        int conflict = bookingMapper.countConflictBooking(
                booking.getDeptId(), booking.getSpaceNo(), booking.getWorkDate(),
                booking.getStartTime(), booking.getEndTime());
        if (conflict > 0) {
            result.put("success", false);
            result.put("msg", "该时段已被预约");
            return result;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String bookingNo = "BK" + sdf.format(new Date()) + String.format("%04d", new java.util.Random().nextInt(10000));
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
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
}