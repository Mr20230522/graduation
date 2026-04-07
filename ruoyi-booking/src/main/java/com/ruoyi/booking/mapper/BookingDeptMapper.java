package com.ruoyi.booking.mapper;

import com.ruoyi.common.core.domain.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 门店查询Mapper（用于预约系统，无需权限控制）
 */
@Mapper
public interface BookingDeptMapper {

    /**
     * 查询所有营业中的门店（绕过数据权限过滤）
     */
    @Select("SELECT d.dept_id, d.parent_id, d.ancestors, d.dept_name, d.order_num, d.leader, " +
            "d.phone, d.email, d.status, d.del_flag, d.create_by, d.create_time, d.address, " +
            "d.longitude, d.latitude, d.license, d.car_space_count, d.image_url, d.image_type " +
            "FROM sys_dept d " +
            "WHERE d.del_flag = '0' AND d.status = '0' " +
            "ORDER BY d.order_num, d.dept_id")
    List<SysDept> selectAllActiveDepts();

    /**
     * 根据ID查询门店详情
     */
    @Select("SELECT d.dept_id, d.parent_id, d.ancestors, d.dept_name, d.order_num, d.leader, " +
            "d.phone, d.email, d.status, d.del_flag, d.create_by, d.create_time, d.address, " +
            "d.longitude, d.latitude, d.license, d.car_space_count, d.image_url, d.image_type, " +
            "d.alipay_app_id, d.alipay_private_key, d.alipay_public_key, d.alipay_gateway " +
            "FROM sys_dept d " +
            "WHERE d.dept_id = #{deptId} AND d.del_flag = '0'")
    SysDept selectDeptById(Long deptId);
}
