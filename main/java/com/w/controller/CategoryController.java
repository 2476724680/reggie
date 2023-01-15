package com.w.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.w.common.CustomException;
import com.w.common.R;
import com.w.pojo.Category;
import com.w.pojo.Dish;
import com.w.pojo.Meal;
import com.w.service.CategoryService;
import com.w.service.DishService;
import com.w.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private MealService mealService;

    /*
    * 新增菜品分类
    * */
    @PostMapping
    public R<String> save(@RequestBody Category category){

        categoryService.save(category);

        return R.success("新增分类成功");
    }

    /*
    * 菜品分类分页查询
    *
    * */
    @GetMapping("/page")
    public R<Page<Category>> page(int page,int pageSize){

        Page<Category> page1=new Page<>(page,pageSize);

        //添加排序规则
        LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<Category>();

        lqw.orderByAsc(Category::getSort);

        categoryService.page(page1,lqw);

        return R.success(page1);

    }

    /*
    *
    * 修改菜品分类功能
    *
    * */
    @PutMapping
    public R<String> update(@RequestBody Category category){

        categoryService.updateById(category);

        return R.success("修改分类成功");

    }

    /*
    * 删除分类功能
    * 前提：当前分类没有与菜品和套餐关联
    * */

    @DeleteMapping
    public R<String> delete(Long ids){

        //当前分类与菜品和套餐都没有关联 才能删除
        categoryService.remove(ids);

        return R.success("删除成功");
    }



    /*
    * 根据type查询菜品分类
    *
    * */

    @GetMapping("/list")
    public R<List<Category>> list(Category category){

        //条件查询
        LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<Category>();

        //1 菜品分类  2 套餐分类
        lqw.eq(category.getType()!=null,Category::getType,category.getType());
        //排序规则
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(lqw);

        return R.success(list);

    }
}
