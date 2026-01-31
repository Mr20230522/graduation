package com.ruoyi.common.core.domain.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RegisterBody extends LoginBody {

    /* =============== 业务字段 =============== */
    private String phonenumber;   // 手机号（员工/老板）
    private String email;         // 邮箱（老板）
    private String companyName;   // 公司名称（员工/老板）
    private String legalPerson;   // 法人（老板）
    private String license;       // 营业执照（老板）
    private String address;       // 详细地址（老板）
    private Double longitude;     // 经度（老板）
    private Double latitude;      // 纬度（老板）
    private String userType;      // 用户的角色

    /* =============== 一键生成 =============== */
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }

    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @NotBlank(message = "公司名称不能为空")
    @Size(max = 30, message = "公司名称长度不能超过30个字符")
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    @Size(max = 20, message = "法人姓名长度不能超过20个字符")
    public String getLegalPerson() { return legalPerson; }
    public void setLegalPerson(String legalPerson) { this.legalPerson = legalPerson; }

    @Size(max = 50, message = "营业执照编号长度不能超过50个字符")
    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }

    @Size(max = 200, message = "详细地址长度不能超过200个字符")
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }


    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    private String status = "0";   // 默认已审核
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}