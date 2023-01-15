package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.w.dto.MealDto;
import com.w.pojo.Meal;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MealService extends IService<Meal> {

    /*
    * 新增套餐
    * */
    @Transactional
    void saveWithDishes(MealDto mealDto);


    /* *
     *
     * 删除套餐+菜品
     * 先判断是不是停售状态再删除
     */

    @Transactional
    void removeWithDishes(List<Long> ids);

    /*
    *
    * 套餐数据回显
    * */
    MealDto getByIds(Long id);


    /*
     *
     *  套餐数据修改
     *
     * */

    @Transactional
    void updateWithDishes(MealDto mealDto);


    /*
     * 根据分类id以及状态查出对应套餐
     *
     * */
    List<MealDto> list(Meal meal);

}
