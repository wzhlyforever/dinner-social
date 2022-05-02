package com.imooc.seckill.mapper;

import com.imooc.commons.model.pojo.SeckillVouchers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @program: food-social-contact-parent
 * @description:
 * @author: Mr.Wang
 * @create: 2022-04-15 11:49
 **/
public interface SecKillVouchersMapper {

// 新增秒杀活动
  @Insert("insert into t_seckill_vouchers (fk_voucher_id, amount, start_time, end_time, is_valid, create_date, update_date) " +
      " values (#{fkVoucherId}, #{amount}, #{startTime}, #{endTime}, 1, now(), now())")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int save(SeckillVouchers seckillVouchers);

  // 查询该优惠卷是否参与过秒杀
  // 根据代金券 ID 查询该代金券是否参与抢购活动
  @Select("select id, fk_voucher_id, amount, start_time, end_time, is_valid " +
      " from t_seckill_vouchers where fk_voucher_id = #{voucherId}")
  SeckillVouchers selectVoucher(Integer voucherId);


  // 扣减库存
  // 减库存
  @Update("update t_seckill_vouchers set amount = amount - 1 " +
      " where id = #{seckillId}")
  int stockDecrease(@Param("seckillId") int seckillId);

}
