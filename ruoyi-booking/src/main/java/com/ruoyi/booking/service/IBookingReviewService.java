package com.ruoyi.booking.service;

import com.ruoyi.booking.domain.BookingReview;
import com.ruoyi.booking.domain.DeptRatingStats;
import com.ruoyi.booking.domain.dto.CreateReviewDTO;
import com.ruoyi.common.core.domain.AjaxResult;
import java.util.List;
import java.util.Map;

public interface IBookingReviewService {

    AjaxResult createReview(CreateReviewDTO dto);

    Map<String, Object> getDeptReviews(Long deptId, int pageNum, int pageSize);

    DeptRatingStats getDeptRatingStats(Long deptId);

    AjaxResult replyReview(Long reviewId, String replyContent);

    AjaxResult deleteReview(Long reviewId);

    Map<String, Object> getMyReviews(int pageNum, int pageSize);
}