package com.ruoyi.booking.mapper;

import com.ruoyi.booking.domain.BookingOrder;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface BookingOrderMapper {

    BookingOrder selectBookingOrderById(Long id);

    BookingOrder selectBookingOrderByNo(@Param("orderNo") String orderNo);

    BookingOrder selectBookingOrderByBookingNo(@Param("bookingNo") String bookingNo);

    List<BookingOrder> selectBookingOrderList(BookingOrder order);

    int insertBookingOrder(BookingOrder order);

    int updateBookingOrder(BookingOrder order);

    int deleteBookingOrderByIds(Long[] ids);

    int updateStatusByOrderNo(@Param("orderNo") String orderNo,
                              @Param("status") Integer status,
                              @Param("payTime") Date payTime);
}