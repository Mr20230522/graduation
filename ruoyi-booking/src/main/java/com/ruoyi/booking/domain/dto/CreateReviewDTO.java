package com.ruoyi.booking.domain.dto;

import java.util.List;

public class CreateReviewDTO {
    private Long bookingId;
    private Integer rating;
    private String content;
    private Integer isAnonymous;
    private List<String> images;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Integer isAnonymous) { this.isAnonymous = isAnonymous; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}