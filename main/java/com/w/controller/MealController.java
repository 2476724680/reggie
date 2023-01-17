package com.w.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.w.common.CustomException;
import com.w.common.R;
import com.w.dto.DishDto;
import com.w.dto.MealDishDto;
import com.w.dto.MealDto;
import com.w.pojo.Category;
import com.w.pojo.Dish;
import com.w.pojo.Meal;
import com.w.pojo.MealDish;
import com.w.service.CategoryService;
import com.w.service.DishService;
import com.w.service.MealDishService;
import com.w.service.MealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class MealController {

    @Autowired
    private MealService mealService;

    @Autowired
    private MealDishService mealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /*
    * 新增套餐
    *
    * */
    @PostMapping
    public R<String> save(@RequestBody MealDto mealDto){

        mealService.saveWithDishes(mealDto);

        return R.success("新增套餐成功");
    }


    /*
    * 套餐分页查询
    *
    * */
    @GetMapping("/page")
    public R<Page<MealDto>> page(int page,int pageSize,String name){

        //条件查询
        Page<Meal> pageInfo=new Page<Meal>(page,pageSize);

        LambdaQueryWrapper<Meal> lqw=new LambdaQueryWrapper<Meal>();

        lqw.like(name!=null,Meal::getName,name);
        lqw.orderByDesc(Meal::getUpdateTime);

        //查询
        mealService.page(pageInfo, lqw);

        //里面没有categoryName 只有categoryId 需要遍历添加
        Page<MealDto> mealDtoPage=new Page<MealDto>();

        //拷贝
        BeanUtils.copyProperties(pageInfo,mealDtoPage,"records");

        //获取pageInfo中的records 设置categoryName
        List<Meal> meals = pageInfo.getRecords();

        List<MealDto> records= meals.stream().map(meal -> {

            //获得categoryId
            Long categoryId = meal.getCategoryId();

            //拷贝原先内容
            MealDto mealDto=new MealDto();
            BeanUtils.copyProperties(meal,mealDto);

            //根据categoryId获得categoryName
            Category category = categoryService.getById(categoryId);

            if(category!=null) {
                //设置categoryId
                mealDto.setCategoryName(category.getName());
            }
            return mealDto;
        }).collect(Collectors.toList());

        mealDtoPage.setRecords(records);

        return R.success(mealDtoPage);
    }

    /*
    * 删除套餐  套餐以及对应的套餐菜品都要删除
    *
    *
    * */
    @DeleteMapping
    public R<String> remove(@RequestParam List<Long> ids){

        if(ids==null||ids.size()==0){
            throw new CustomException("请选择内容");
        }

        mealService.removeWithDishes(ids);

        return R.success("删除成功");
    }

    /*
    *  套餐数据回显
    * */
    @GetMapping("/{id}")
    public R<MealDto> getById(@PathVariable Long id){

        MealDto mealDto = mealService.getByIds(id);

        return R.success(mealDto);

    }


    /*
    *
    * 套餐数据修改
    * */
    @PutMapping
    public R<String> update(@RequestBody MealDto mealDto){

        mealService.updateWithDishes(mealDto);

        return R.success("套餐数据修改成功");
    }


    /*
    * 批量停售
    * */
    @PostMapping("/status/0")
    public R<String> haltSales(@RequestParam List<Long> ids){

        if(ids==null||ids.size()==0){
            throw new CustomException("请选择内容");
        }

        LambdaUpdateWrapper<Meal> luw=new LambdaUpdateWrapper<Meal>();
        luw.in(Meal::getId,ids);
        luw.set(Meal::getStatus,0);
        mealService.update(luw);

        //删除对应redis分类缓存
        LambdaQueryWrapper<Meal> lqw=new LambdaQueryWrapper<>();
        lqw.in(Meal::getId,ids);
        mealService.list(lqw).stream()
                .map(meal -> meal.getCategoryId())
                .distinct()
                .forEach(category->redisTemplate.delete("meal_"+category+"_1"));

        return R.success("停售成功");
    }


    /*
     * 批量起售
     * */
    @PostMapping("/status/1")
    public R<String> startSales(@RequestParam List<Long> ids){

        if(ids==null||ids.size()==0){
            throw new CustomException("请选择内容");
        }

        mealService.startSales(ids);


        return R.success("起售成功");
    }


    /*
    * 根据分类id以及状态查出对应套餐
    *
    * */
    @GetMapping("/list")
    public R<List<MealDto>> list(Meal meal){

        List<MealDto> meals = mealService.list(meal);

        return R.success(meals);

    }


    /*
    * 根据套餐获得对应菜品
    * */
    @GetMapping("/dish/{id}")
    public R<List<MealDishDto>> getDishes(@PathVariable Long id){

        LambdaQueryWrapper<MealDish> lqw=new LambdaQueryWrapper<MealDish>();
        lqw.eq(MealDish::getSetmealId,id);

        List<MealDish> list = mealDishService.list(lqw);

        //查出来的mealDish没有对应的照片路径
        List<MealDishDto> collect = list.stream().map(mealDish -> {
            //根据dish id获得dish 在获得对应照片路径
            LambdaQueryWrapper<Dish> lqwDish = new LambdaQueryWrapper<Dish>();
            lqwDish.eq(Dish::getId, mealDish.getDishId());
            Dish dish = dishService.getOne(lqwDish);

            //拷贝
            MealDishDto mealDishDto = new MealDishDto();
            BeanUtils.copyProperties(mealDish, mealDishDto);

            mealDishDto.setImage(dish.getImage());

            return mealDishDto;
        }).collect(Collectors.toList());

        return R.success(collect);
    }
}
