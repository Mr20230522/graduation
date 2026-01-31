package com.ruoyi.booking.domain;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class DeptSchedule extends BaseEntity {
    private Long id;
    private Long deptId;
    private Date workDate;
    private String openTime;   // HH:mm:ss
    private String closeTime;  // HH:mm:ss
    private Integer slotMinutes;
    private Integer carSpaces;
    private Integer status; // 0正常 1休息
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
    public Integer getSlotMinutes() { return slotMinutes; }
    public void setSlotMinutes(Integer slotMinutes) { this.slotMinutes = slotMinutes; }
    public Integer getCarSpaces() { return carSpaces; }
    public void setCarSpaces(Integer carSpaces) { this.carSpaces = carSpaces; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}