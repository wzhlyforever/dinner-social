package com.imooc.commons.constants;

import lombok.Getter;

@Getter
public enum PointTypesContant {


  sign(0),
  follow(1),
  feed(2),
  review(3);


  private Integer type;

  PointTypesContant(Integer type) {
    this.type = type;
  }
}
