package com.w.dto;

import com.w.pojo.Meal;
import com.w.pojo.MealDish;
import lombok.Data;

import java.util.List;

@Data
public class MealDto extends Meal {

    private List<MealDish> setmealDishes;

    private String categoryName;

    //购物车中数量
    private Integer number;

}
