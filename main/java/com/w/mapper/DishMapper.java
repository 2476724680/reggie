package com.w.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.w.pojo.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {


}
