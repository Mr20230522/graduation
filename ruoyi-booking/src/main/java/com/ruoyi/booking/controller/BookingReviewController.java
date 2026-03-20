package com.ruoyi.booking.controller;

import com.ruoyi.booking.domain.dto.CreateReviewDTO;
import com.ruoyi.booking.service.IBookingReviewService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/booking/review")
public class BookingReviewController extends BaseController {

    @Autowired
    private IBookingReviewService reviewService;

    /**
     * 创建评价
     */
    @PostMapping("/create")
    public AjaxResult createReview(@RequestBody CreateReviewDTO dto) {
        return reviewService.createReview(dto);
    }

    /**
     * 获取门店评价列表
     */
    @GetMapping("/list")
    public TableDataInfo getDeptReviews(@RequestParam Long deptId,
                                        @RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = reviewService.getDeptReviews(deptId, pageNum, pageSize);

        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(0);
        rspData.setMsg("查询成功");
        rspData.setRows((java.util.List<?>) result.get("list"));
        rspData.setTotal((Long) result.get("total"));

        return rspData;
    }

    /**
     * 获取门店评分统计
     */
    @GetMapping("/stats/{deptId}")
    public AjaxResult getDeptRatingStats(@PathVariable Long deptId) {
        return AjaxResult.success(reviewService.getDeptRatingStats(deptId));
    }

    /**
     * 商家回复评价
     */
    @PostMapping("/reply/{reviewId}")
    public AjaxResult replyReview(@PathVariable Long reviewId, @RequestBody String replyContent) {
        return reviewService.replyReview(reviewId, replyContent);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/delete/{reviewId}")
    public AjaxResult deleteReview(@PathVariable Long reviewId) {
        return reviewService.deleteReview(reviewId);
    }

    /**
     * 获取我的评价列表
     */
    @GetMapping("/my")
    public TableDataInfo getMyReviews(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = reviewService.getMyReviews(pageNum, pageSize);

        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(0);
        rspData.setMsg("查询成功");
        rspData.setRows((java.util.List<?>) result.get("list"));
        rspData.setTotal((Long) result.get("total"));

        return rspData;
    }
}