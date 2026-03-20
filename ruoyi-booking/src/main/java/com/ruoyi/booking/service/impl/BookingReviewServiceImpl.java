package com.ruoyi.booking.service.impl;

import com.ruoyi.booking.domain.BookingReview;
import com.ruoyi.booking.domain.BookingReviewImage;
import com.ruoyi.booking.domain.DeptRatingStats;
import com.ruoyi.booking.domain.dto.CreateReviewDTO;
import com.ruoyi.booking.mapper.BookingReviewMapper;
import com.ruoyi.booking.mapper.BookingReviewImageMapper;
import com.ruoyi.booking.service.IBookingReviewService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BookingReviewServiceImpl implements IBookingReviewService {

    @Autowired
    private BookingReviewMapper reviewMapper;

    @Autowired
    private BookingReviewImageMapper imageMapper;

    @Override
    @Transactional
    public AjaxResult createReview(CreateReviewDTO dto) {
        // 检查是否已经评价过
        BookingReview existing = reviewMapper.selectReviewByBookingId(dto.getBookingId());
        if (existing != null) {
            return AjaxResult.error("该预约已经评价过了");
        }

        // 创建评价
        BookingReview review = new BookingReview();
        review.setReviewNo(generateReviewNo());
        review.setBookingId(dto.getBookingId());
        // 这里需要根据bookingId获取bookingNo和deptId
        review.setUserId(SecurityUtils.getUserId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setIsAnonymous(dto.getIsAnonymous());
        review.setHasImage(dto.getImages() != null && !dto.getImages().isEmpty() ? 1 : 0);

        reviewMapper.insertReview(review);

        // 保存图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<BookingReviewImage> imageList = new ArrayList<>();
            for (int i = 0; i < dto.getImages().size(); i++) {
                BookingReviewImage image = new BookingReviewImage();
                image.setReviewId(review.getId());
                image.setImageUrl(dto.getImages().get(i));
                image.setSortOrder(i);
                imageList.add(image);
            }
            imageMapper.batchInsertImages(imageList);
        }

        // 更新门店评分统计
        updateDeptRatingStats(review.getDeptId());

        return AjaxResult.success("评价成功");
    }

    @Override
    public Map<String, Object> getDeptReviews(Long deptId, int pageNum, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        int offset = (pageNum - 1) * pageSize;
        List<BookingReview> list = reviewMapper.selectReviewsByDeptId(deptId, offset, pageSize);

        // 为每条评价加载图片
        for (BookingReview review : list) {
            List<String> images = imageMapper.selectImagesByReviewId(review.getId());
            review.setImages(images);

            // 匿名处理
            if (review.getIsAnonymous() == 1) {
                review.setUserName("匿名用户");
                review.setAvatar(null);
            }
        }

        int total = reviewMapper.countReviewsByDeptId(deptId);
        DeptRatingStats stats = getDeptRatingStats(deptId);

        result.put("list", list);
        result.put("total", (long) total);  // 转换为 Long 类型
        result.put("stats", stats);

        return result;
    }

    @Override
    public DeptRatingStats getDeptRatingStats(Long deptId) {
        DeptRatingStats stats = reviewMapper.selectDeptRatingStats(deptId);
        if (stats == null) {
            stats = new DeptRatingStats();
            stats.setDeptId(deptId);
            stats.setAvgRating(BigDecimal.ZERO);
            stats.setTotalReviews(0);
            stats.setFiveStarCount(0);
            stats.setFourStarCount(0);
            stats.setThreeStarCount(0);
            stats.setTwoStarCount(0);
            stats.setOneStarCount(0);
            stats.setImageReviews(0);
        }
        return stats;
    }

    @Override
    @Transactional
    public AjaxResult replyReview(Long reviewId, String replyContent) {
        BookingReview review = reviewMapper.selectReviewById(reviewId);
        if (review == null) {
            return AjaxResult.error("评价不存在");
        }

        review.setReplyContent(replyContent);
        review.setReplyTime(new Date());
        review.setReplyUserId(SecurityUtils.getUserId());

        reviewMapper.updateReview(review);

        return AjaxResult.success("回复成功");
    }

    @Override
    @Transactional
    public AjaxResult deleteReview(Long reviewId) {
        BookingReview review = reviewMapper.selectReviewById(reviewId);
        if (review == null) {
            return AjaxResult.error("评价不存在");
        }

        // 逻辑删除
        review.setStatus(0);
        reviewMapper.updateReview(review);

        // 更新门店评分统计
        updateDeptRatingStats(review.getDeptId());

        return AjaxResult.success("删除成功");
    }

    @Override
    public Map<String, Object> getMyReviews(int pageNum, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        Long userId = SecurityUtils.getUserId();

        BookingReview query = new BookingReview();
        query.setUserId(userId);

        List<BookingReview> list = reviewMapper.selectReviewList(query);

        // 为每条评价加载图片
        for (BookingReview review : list) {
            List<String> images = imageMapper.selectImagesByReviewId(review.getId());
            review.setImages(images);
        }

        result.put("list", list);
        result.put("total", (long) list.size());  // 如果没有分页，直接返回总数

        return result;
    }

    /**
     * 生成评价单号
     */
    private String generateReviewNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "REV" + sdf.format(new Date()) + String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 更新门店评分统计
     */
    @Transactional
    public void updateDeptRatingStats(Long deptId) {
        // 查询所有有效评价
        BookingReview query = new BookingReview();
        query.setDeptId(deptId);
        query.setStatus(1);
        List<BookingReview> reviews = reviewMapper.selectReviewList(query);

        if (reviews.isEmpty()) {
            return;
        }

        // 计算统计数据
        int total = reviews.size();
        int fiveStar = 0, fourStar = 0, threeStar = 0, twoStar = 0, oneStar = 0;
        int imageCount = 0;
        int totalRating = 0;

        for (BookingReview review : reviews) {
            totalRating += review.getRating();
            if (review.getHasImage() == 1) imageCount++;

            switch (review.getRating()) {
                case 5: fiveStar++; break;
                case 4: fourStar++; break;
                case 3: threeStar++; break;
                case 2: twoStar++; break;
                case 1: oneStar++; break;
            }
        }

        BigDecimal avgRating = BigDecimal.valueOf(totalRating)
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);

        DeptRatingStats stats = new DeptRatingStats();
        stats.setDeptId(deptId);
        stats.setAvgRating(avgRating);
        stats.setTotalReviews(total);
        stats.setFiveStarCount(fiveStar);
        stats.setFourStarCount(fourStar);
        stats.setThreeStarCount(threeStar);
        stats.setTwoStarCount(twoStar);
        stats.setOneStarCount(oneStar);
        stats.setImageReviews(imageCount);

        // 更新或插入统计
        DeptRatingStats existing = reviewMapper.selectDeptRatingStats(deptId);
        if (existing == null) {
            reviewMapper.insertDeptRatingStats(stats);
        } else {
            reviewMapper.updateDeptRatingStats(stats);
        }
    }
}