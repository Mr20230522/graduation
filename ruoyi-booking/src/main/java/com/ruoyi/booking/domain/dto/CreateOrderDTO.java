package com.ruoyi.booking.domain.dto;

import java.util.Date;

public class CreateOrderDTO {
    private Long deptId;
    private Date workDate;
    private String spaceNo;
    private String slot;
    private Integer comboMinutes;
    private String carNumber;
    private String carModel;
    private String carColor;

    // Getters and Setters
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }
    public String getSpaceNo() { return spaceNo; }
    public void setSpaceNo(String spaceNo) { this.spaceNo = spaceNo; }
    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }
    public Integer getComboMinutes() { return comboMinutes; }
    public void setComboMinutes(Integer comboMinutes) { this.comboMinutes = comboMinutes; }
    public String getCarNumber() { return carNumber; }
    public void setCarNumber(String carNumber) { this.carNumber = carNumber; }
    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public String getCarColor() { return carColor; }
    public void setCarColor(String carColor) { this.carColor = carColor; }
}