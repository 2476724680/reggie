package com.w.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.CustomException;
import com.w.common.R;
import com.w.mapper.CategoryMapper;
import com.w.pojo.Category;
import com.w.pojo.Dish;
import com.w.pojo.Meal;
import com.w.service.CategoryService;
import com.w.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private DishService dishService;
    @Autowired
    private MealServiceImpl mealService;


    /*
    *
    * 删除功能业务扩展
    *
    * */
    @Override
    public R<String> remove(Long id) {

        //查看菜品中是否有分类关联  categoryId ——>条件查询
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<Dish>();

        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);

        int dishCount = dishService.count(dishLambdaQueryWrapper);

        //如果查询条数大于0 抛出业务异常
        if(dishCount>0){
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        //查看套餐中是否有分类关联  categoryId ——>条件查询
        LambdaQueryWrapper<Meal> mealLambdaQueryWrapper=new LambdaQueryWrapper<Meal>();

        mealLambdaQueryWrapper.eq(Meal::getCategoryId,id);

        int mealCount = mealService.count(mealLambdaQueryWrapper);

        //如果查询条数大于0 抛出业务异常
        if(mealCount>0){
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //完全没有关联才能删除
        super.removeById(id);

        return R.success("删除分类成功");
    }
}
