package com.ruoyi.booking.mapper;

import com.ruoyi.booking.domain.BookingDeptPaymentConfig;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BookingDeptPaymentConfigMapper {

    BookingDeptPaymentConfig selectConfigById(Long id);

    BookingDeptPaymentConfig selectConfigByDeptId(@Param("deptId") Long deptId);

    BookingDeptPaymentConfig selectDefaultConfig();

    List<BookingDeptPaymentConfig> selectConfigList(BookingDeptPaymentConfig config);

    int insertConfig(BookingDeptPaymentConfig config);

    int updateConfig(BookingDeptPaymentConfig config);

    int deleteConfigByIds(Long[] ids);

    int clearDefaultFlag();
}