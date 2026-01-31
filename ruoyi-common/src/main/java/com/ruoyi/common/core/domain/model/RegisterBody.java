package com.ruoyi.common.core.domain.model;

import com.ruoyi.common.core.domain.entity.SysDept;

/**
 * 用户注册对象
 *
 * @author ruoyi
 */
public class RegisterBody extends LoginBody
{
    private static final long serialVersionUID = 1L;

    /** 用户类型：1老板 2员工 3普通用户 */
    private String userType;

    /** 公司名称（老板填公司名，员工填所属公司名） */
    private String companyName;

    /** 法人姓名（老板） */
    private String legalPerson;

    /** 营业执照编号（老板） */
    private String license;

    /** 详细地址（老板） */
    private String address;

    /** 经度（老板） */
    private Double longitude;

    /** 纬度（老板） */
    private Double latitude;

    /** 邮箱（老板） */
    private String email;

    /** 状态（老板） */
    private String status;

    /** 车位数（老板注册时填写） */
    private Integer carSpaceCount;

    public String getUserType()
    {
        return userType;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    public String getLegalPerson()
    {
        return legalPerson;
    }

    public void setLegalPerson(String legalPerson)
    {
        this.legalPerson = legalPerson;
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getCarSpaceCount()
    {
        return carSpaceCount;
    }

    public void setCarSpaceCount(Integer carSpaceCount)
    {
        this.carSpaceCount = carSpaceCount;
    }
}