package com.ruoyi.booking.job;

import com.ruoyi.booking.domain.CarBooking;
import com.ruoyi.booking.mapper.BookingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预约历史记录定时迁移任务
 * 每天中午12点将已过期的预约从 car_booking 迁移到 booking_history
 */
@Component
public class BookingHistoryJob {

    @Autowired
    private BookingMapper bookingMapper;

    /**
     * 每天中午12:00执行
     * cron = "0 0 12 * * ?" 表示每天12点0分0秒执行
     */
    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void migrateExpiredBooking() {
        Date now = new Date();

        // 查询 end_time < now 的预约（已过期）
        List<CarBooking> expiredList = bookingMapper.selectExpiredBookings(now);

        if (expiredList.isEmpty()) {
            System.out.println("【定时任务】无过期预约需要迁移");
            return;
        }

        // 批量插入历史表
        bookingMapper.batchInsertHistory(expiredList);

        // 批量删除主表（只删除已过期的）
        List<Long> ids = expiredList.stream()
                .map(CarBooking::getId)
                .collect(Collectors.toList());
        bookingMapper.deleteBookingByIds(ids.toArray(new Long[0]));

        System.out.println("【定时任务】中午12点，迁移 " + expiredList.size() + " 条过期预约到历史表");
    }
}