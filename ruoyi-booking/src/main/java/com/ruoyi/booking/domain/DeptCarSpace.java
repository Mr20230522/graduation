package com.ruoyi.booking.domain;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class DeptCarSpace extends BaseEntity {
    private Long id;
    private Long deptId;
    private String spaceNo;
    private String spaceName;
    private Integer status; // 0启用 1禁用
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getSpaceNo() { return spaceNo; }
    public void setSpaceNo(String spaceNo) { this.spaceNo = spaceNo; }
    public String getSpaceName() { return spaceName; }
    public void setSpaceName(String spaceName) { this.spaceName = spaceName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}