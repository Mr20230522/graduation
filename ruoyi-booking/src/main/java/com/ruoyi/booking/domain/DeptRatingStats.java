package com.ruoyi.booking.domain;

import java.math.BigDecimal;
import java.util.Date;

public class DeptRatingStats {
    private Long deptId;
    private BigDecimal avgRating;
    private Integer totalReviews;
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;
    private Integer imageReviews;
    private Date updateTime;

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public Integer getFiveStarCount() { return fiveStarCount; }
    public void setFiveStarCount(Integer fiveStarCount) { this.fiveStarCount = fiveStarCount; }

    public Integer getFourStarCount() { return fourStarCount; }
    public void setFourStarCount(Integer fourStarCount) { this.fourStarCount = fourStarCount; }

    public Integer getThreeStarCount() { return threeStarCount; }
    public void setThreeStarCount(Integer threeStarCount) { this.threeStarCount = threeStarCount; }

    public Integer getTwoStarCount() { return twoStarCount; }
    public void setTwoStarCount(Integer twoStarCount) { this.twoStarCount = twoStarCount; }

    public Integer getOneStarCount() { return oneStarCount; }
    public void setOneStarCount(Integer oneStarCount) { this.oneStarCount = oneStarCount; }

    public Integer getImageReviews() { return imageReviews; }
    public void setImageReviews(Integer imageReviews) { this.imageReviews = imageReviews; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}