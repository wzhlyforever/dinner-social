package com.imooc.restaurant.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.Restaurant;
import com.imooc.commons.model.vo.ReviewsVO;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.restaurant.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("reviews")
public class ReviewsController {

    @Resource
    private ReviewService reviewService;
    @Resource
    private HttpServletRequest request;

    /**
     * 添加餐厅评论
     *
     * @param restaurantId
     * @param access_token
     * @param content
     * @param likeIt
     * @return
     */
    @PostMapping("{restaurantId}")
    public ResultInfo<String> addReview(@PathVariable Integer restaurantId,
                                        String access_token,
                                        @RequestParam("content") String content,
                                        @RequestParam("likeIt") int likeIt) {
        reviewService.addReview(restaurantId, access_token, content, likeIt);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "添加成功");
    }

    /**
     * 获取餐厅最新评论
     *
     * @param restaurantId
     * @param access_token
     * @return
     */
    @GetMapping("{restaurantId}/news")
    public ResultInfo<List<ReviewsVO>> findNewReviews(@PathVariable Integer restaurantId,
        String access_token) {
        List<ReviewsVO> reviewsList = reviewService.findNewReviews(restaurantId, access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), reviewsList);
    }


}
