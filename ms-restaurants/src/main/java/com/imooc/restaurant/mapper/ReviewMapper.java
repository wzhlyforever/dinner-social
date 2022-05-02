package com.imooc.restaurant.mapper;

import com.imooc.commons.model.pojo.Reviews;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-26 14:21
 **/
public interface ReviewMapper {

  // 插入餐厅评论
  @Insert("insert into t_reviews (fk_restaurant_id, fk_diner_id, content, like_it, is_valid, create_date, update_date)" +
      " values (#{fkRestaurantId}, #{fkDinerId}, #{content}, #{likeIt}, 1, now(), now())")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int saveReviews(Reviews reviews);



}
