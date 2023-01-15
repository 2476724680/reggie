package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.dto.DishDto;
import com.w.pojo.Dish;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DishService extends IService<Dish> {


    /*
    *
    * 根据DishDto封装数据 在dish和dish_flavor两张表中存储数据
    *
    * */

    @Transactional
    void saveWithFlavor(DishDto dishDto);

    /*
     *
     * 数据回显 菜品+口味
     *
     * */
    @Transactional
    DishDto getByIdWithFlavor(Long id);

    /*
    * 数据更新 菜品+口味
    *
    * */
    @Transactional
    void updateWithFlavor(DishDto dishDto);

    /*
    * 菜品删除
    * */

    @Transactional
    void removeWithFlavor(List<Long> ids);


    /*
    *
    *  菜品批量停售
    * */
    void haltSales(List<Long> ids);


    /*
     * 根据categoryId查找对应菜品  还要看对应菜品在购物车里面是否存在 返回对应数量
     *
     * */

    List<DishDto> list(Dish dish);


}
