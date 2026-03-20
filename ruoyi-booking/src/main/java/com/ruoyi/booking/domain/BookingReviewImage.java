package com.ruoyi.booking.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class BookingReviewImage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long reviewId;
    private String imageUrl;
    private Integer sortOrder;
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}