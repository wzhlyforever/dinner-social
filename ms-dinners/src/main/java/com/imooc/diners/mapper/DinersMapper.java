package com.imooc.diners.mapper;


import com.imooc.commons.model.dto.DinnerInfoDTO;
import com.imooc.commons.model.pojo.Dinners;
import com.imooc.commons.model.vo.ShortInDinerInfo;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 食客 Mapper
 */
public interface DinersMapper {

    // 根据手机号查询食客信息
    @Select("select id, username, phone, email, is_valid " +
            " from t_diners where phone = #{phone}")
    Dinners selectByPhone(@Param("phone") String phone);

    // 根据用户名查询食客信息
    @Select("select id, username, phone, email, is_valid " +
            " from t_diners where username = #{username}")
    Dinners selectByUsername(@Param("username") String username);

    // 新增食客信息
    @Insert("insert into " +
            " t_diners (username, password, phone, roles, is_valid, create_date, update_date) " +
            " values (#{userName}, #{password}, #{phone}, \"ROLE_USER\", 1, now(), now())")
    int save(DinnerInfoDTO dinersDTO);

    // 根据 ID 集合查询多个食客信息
    @Select("<script> " +
        " select id, nickname, avatar_url from t_diners " +
        " where is_valid = 1 and id in " +
        " <foreach item=\"id\" collection=\"ids\" open=\"(\" separator=\",\" close=\")\"> " +
        "   #{id} " +
        " </foreach> " +
        " </script>")
    List<ShortInDinerInfo> findByIds(@Param("ids") String[] ids);

}
