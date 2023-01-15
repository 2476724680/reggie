package com.w.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.w.pojo.MealDish;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MealDishMapper extends BaseMapper<MealDish> {
}
