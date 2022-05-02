package com.imooc.commons.model.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * @program: food-social-contact-parent
 * @description: 返回结果类
 * @author: Mr.Wang
 * @create: 2022-02-27 17:53
 **/
@Getter
@Setter
@ApiModel(value = "返回说明")
public class ResultInfo<T> implements Serializable {


    @ApiModelProperty(value = "成功标识0=失败，1=成功")
    private Integer code;
    @ApiModelProperty(value = "描述信息")
    private String message;
    @ApiModelProperty(value = "访问路径")
    private String path;
    @ApiModelProperty(value = "返回数据对象")
    private T data;

}
