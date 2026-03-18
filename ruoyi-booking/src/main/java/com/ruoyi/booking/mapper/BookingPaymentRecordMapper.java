package com.ruoyi.booking.mapper;

import com.ruoyi.booking.domain.BookingPaymentRecord;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BookingPaymentRecordMapper {

    int insertPaymentRecord(BookingPaymentRecord record);

    List<BookingPaymentRecord> selectRecordsByOrderNo(@Param("orderNo") String orderNo);

    BookingPaymentRecord selectByTransactionId(@Param("transactionId") String transactionId);

    int updateRecordByTransactionId(BookingPaymentRecord record);
}