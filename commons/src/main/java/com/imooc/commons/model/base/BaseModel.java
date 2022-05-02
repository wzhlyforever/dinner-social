package com.imooc.commons.model.base;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * @program: food-social-contact-parent
 * @description:   通用字段
 * @author: Mr.Wang
 * @create: 2022-04-02 15:09
 **/
@Getter
@Setter
public class BaseModel implements Serializable {

  private Integer id;

  private Date createDate;

  private Date updateDate;

  private int isValid;

}
