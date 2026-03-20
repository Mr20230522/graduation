package com.ruoyi.booking.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.util.List;

public class BookingReview extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String reviewNo;
    private Long bookingId;
    private String bookingNo;
    private Long userId;
    private Long deptId;
    private Integer rating;
    private String content;
    private Integer hasImage;
    private String replyContent;
    private Date replyTime;
    private Long replyUserId;
    private String additionalContent;
    private Date additionalTime;
    private Integer isAnonymous;
    private Integer status;

    // 非数据库字段
    private String userName;
    private String deptName;
    private List<String> images;
    private String avatar;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReviewNo() { return reviewNo; }
    public void setReviewNo(String reviewNo) { this.reviewNo = reviewNo; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingNo() { return bookingNo; }
    public void setBookingNo(String bookingNo) { this.bookingNo = bookingNo; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getHasImage() { return hasImage; }
    public void setHasImage(Integer hasImage) { this.hasImage = hasImage; }

    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String replyContent) { this.replyContent = replyContent; }

    public Date getReplyTime() { return replyTime; }
    public void setReplyTime(Date replyTime) { this.replyTime = replyTime; }

    public Long getReplyUserId() { return replyUserId; }
    public void setReplyUserId(Long replyUserId) { this.replyUserId = replyUserId; }

    public String getAdditionalContent() { return additionalContent; }
    public void setAdditionalContent(String additionalContent) { this.additionalContent = additionalContent; }

    public Date getAdditionalTime() { return additionalTime; }
    public void setAdditionalTime(Date additionalTime) { this.additionalTime = additionalTime; }

    public Integer getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Integer isAnonymous) { this.isAnonymous = isAnonymous; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}