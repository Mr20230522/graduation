package com.ruoyi.booking.domain;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class CarBooking extends BaseEntity {
    private Long id;
    private String bookingNo;
    private Long deptId;
    private String spaceNo;
    private Date workDate;
    private Date startTime;
    private Date endTime;
    private String carNumber;
    private String carModel;
    private String carColor;
    private String comboName;
    private Integer comboMinutes;
    private String code;
    private Integer status; // 0待到店 1服务中 2已完成 3已取消
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingNo() { return bookingNo; }
    public void setBookingNo(String bookingNo) { this.bookingNo = bookingNo; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getSpaceNo() { return spaceNo; }
    public void setSpaceNo(String spaceNo) { this.spaceNo = spaceNo; }
    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getCarNumber() { return carNumber; }
    public void setCarNumber(String carNumber) { this.carNumber = carNumber; }
    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public String getCarColor() { return carColor; }
    public void setCarColor(String carColor) { this.carColor = carColor; }
    public String getComboName() { return comboName; }
    public void setComboName(String comboName) { this.comboName = comboName; }
    public Integer getComboMinutes() { return comboMinutes; }
    public void setComboMinutes(Integer comboMinutes) { this.comboMinutes = comboMinutes; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}