package com.w.dto;


import com.w.pojo.Dish;
import com.w.pojo.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors=new ArrayList<DishFlavor>();

    private String categoryName;

    private Integer codes;

    //在购物车中的数量
    private Integer number;


}
