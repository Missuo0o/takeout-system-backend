package com.missuo.pojo.entity;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishFlavor implements Serializable {
  @Serial private static final long serialVersionUID = 1;

  private Long id;

  private Long dishId;

  private String name;

  private String value;
}
