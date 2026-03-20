package com.ruoyi.booking.mapper;

import com.ruoyi.booking.domain.BookingReview;
import com.ruoyi.booking.domain.DeptRatingStats;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BookingReviewMapper {

    List<BookingReview> selectReviewList(BookingReview review);

    BookingReview selectReviewById(Long id);

    BookingReview selectReviewByBookingId(@Param("bookingId") Long bookingId);

    List<BookingReview> selectReviewsByDeptId(@Param("deptId") Long deptId,
                                              @Param("offset") int offset,
                                              @Param("pageSize") int pageSize);

    int countReviewsByDeptId(@Param("deptId") Long deptId);

    int insertReview(BookingReview review);

    int updateReview(BookingReview review);

    int deleteReviewById(Long id);

    DeptRatingStats selectDeptRatingStats(@Param("deptId") Long deptId);

    int updateDeptRatingStats(DeptRatingStats stats);

    int insertDeptRatingStats(DeptRatingStats stats);
}