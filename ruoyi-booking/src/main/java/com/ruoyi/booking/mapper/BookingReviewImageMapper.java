package com.ruoyi.booking.mapper;

import com.ruoyi.booking.domain.BookingReviewImage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BookingReviewImageMapper {

    int insertImage(BookingReviewImage image);

    int batchInsertImages(@Param("list") List<BookingReviewImage> list);

    List<String> selectImagesByReviewId(@Param("reviewId") Long reviewId);

    List<BookingReviewImage> selectImageListByReviewId(@Param("reviewId") Long reviewId);

    int deleteImagesByReviewId(@Param("reviewId") Long reviewId);
}