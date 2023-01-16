package com.w.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.CustomException;
import com.w.dto.MealDto;
import com.w.mapper.MealMapper;
import com.w.pojo.Meal;
import com.w.pojo.MealDish;
import com.w.pojo.ShoppingCart;
import com.w.service.DishService;
import com.w.service.MealDishService;
import com.w.service.MealService;
import com.w.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MealServiceImpl extends ServiceImpl<MealMapper, Meal> implements MealService {


    @Autowired
    private MealDishService mealDishService;


    @Autowired
    @Lazy
    private ShoppingCartService shoppingCartService;

    @Autowired
    private RedisTemplate redisTemplate;
    /*
    *
    * 新建套餐 保存套餐信息以及对应dish信息
    * */
    @Override
    public void saveWithDishes(MealDto mealDto) {

        //先保存套餐信息
        super.save(mealDto);

        //删除redis 对应分类缓存
        redisTemplate.delete("meal_"+mealDto.getCategoryId()+"_1");

        //保存对应dish信息 ——>保存到mael_dish表中
        //遍历List内容 把setmealId 存进去

        List<MealDish> setmealDishes = mealDto.getSetmealDishes();

        for (MealDish setmealDish : setmealDishes) {

            //存入setmealId
            setmealDish.setSetmealId(mealDto.getId());


        }

        //保存dish数据
        mealDishService.saveBatch(setmealDishes);

    }

    /*
     * 删除套餐  套餐以及对应的套餐菜品都要删除
     *
     * */
    @Override
    public void removeWithDishes(List<Long> ids) {

        //查询套餐状态，看是否可以删除
        LambdaQueryWrapper<Meal> lqw=new LambdaQueryWrapper<Meal>();

        lqw.in(Meal::getId,ids);
        lqw.eq(Meal::getStatus,1);  //0 停售 1 起售

        int count = super.count(lqw);

        if(count>0){
            //不能删除 抛出业务异常
            throw new CustomException("当前套餐正在售卖，不能删除");
        }


        LambdaQueryWrapper<Meal> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Meal::getId,ids);

        //删除对应套餐分类缓存
        super.list(lambdaQueryWrapper)
                .stream()
                .map(meal -> meal.getCategoryId())
                .distinct()
                .forEach(category->redisTemplate.delete("meal_"+category+"_1"));

        //如果可以删除  先删除套餐数据
        super.removeByIds(ids);

        //再删除套餐对应菜品
        LambdaQueryWrapper<MealDish> lambdaQueryWrapperDish=new LambdaQueryWrapper<MealDish>();

        lambdaQueryWrapperDish.in(MealDish::getSetmealId,ids);

        mealDishService.remove(lambdaQueryWrapperDish);
    }



    /*
     *
     * 套餐数据回显
     * */
    @Override
    public MealDto getByIds(Long id) {

        //获取套餐本身数据
        Meal meal = super.getById(id);

        //拷贝到MealDto中
        MealDto mealDto=new MealDto();
        BeanUtils.copyProperties(meal,mealDto);

        //根据套餐id 获得对应菜品
        LambdaQueryWrapper<MealDish> lqw=new LambdaQueryWrapper<MealDish>();

        lqw.eq(MealDish::getSetmealId,id);

        List<MealDish> list = mealDishService.list(lqw);

        mealDto.setSetmealDishes(list);

        return mealDto;

    }


    /*
     *
     *  套餐数据修改
     * 1.直接修改套餐数据
     * 2.删除之前对应的dish
     * 3.新增dish
     *
     * */
    @Override
    public void updateWithDishes(MealDto mealDto) {

        //修改套餐数据
        super.updateById(mealDto);

        //删除该套餐对应的分类redis缓存
        redisTemplate.delete("meal_"+mealDto.getCategoryId()+"_1");

        //删除对应mealId数据
        LambdaQueryWrapper<MealDish> lqw=new LambdaQueryWrapper<MealDish>();
        lqw.eq(MealDish::getSetmealId,mealDto.getId());
        mealDishService.remove(lqw);

        //新增mealDish  存入mealId
        List<MealDish> setmealDishes = mealDto.getSetmealDishes();

        //获得mealId
        Long id = mealDto.getId();

        setmealDishes =setmealDishes.stream().map(mealDish -> {
            mealDish.setSetmealId(id);
            return mealDish;
        }).collect(Collectors.toList());

        //存储数据
        mealDishService.saveBatch(setmealDishes);

    }


    /*
     * 根据分类id以及状态查出对应套餐
     *
     * */
    public List<MealDto> list(Meal meal){

        List<MealDto> mealDtoes =null;

        //先去redis查询
        String key="meal_"+meal.getCategoryId()+"_1";
        mealDtoes = (List<MealDto>) (redisTemplate.opsForValue().get(key));

        //如果能查询 直接返回 如果为空 去数据库查询后缓存到redis

        if(mealDtoes!=null&&mealDtoes.size()!=0){
            return mealDtoes;
        }

        //根据分类id查询对应套餐
        LambdaQueryWrapper<Meal> lqw=new LambdaQueryWrapper<Meal>();
        lqw.eq(meal.getCategoryId()!=null,Meal::getCategoryId,meal.getCategoryId());
        lqw.eq(meal.getStatus()!=null,Meal::getStatus,meal.getStatus());
        lqw.orderByDesc(Meal::getUpdateTime);

        List<Meal> meals = super.list(lqw);

        //查询购物车中是否有该套餐
        mealDtoes = meals.stream().map(meal1 -> {

            //拷贝
            MealDto mealDto = new MealDto();
            BeanUtils.copyProperties(meal1, mealDto);

            //查询购物车是否有该套餐
            LambdaQueryWrapper<ShoppingCart> lqwShop=new LambdaQueryWrapper<ShoppingCart>();
            lqwShop.eq(ShoppingCart::getSetmealId,meal1.getId());
            ShoppingCart shoppingCart = shoppingCartService.getOne(lqwShop);

            if(shoppingCart!=null){

                //说明购物车有这个套餐 存入数量
                mealDto.setNumber(shoppingCart.getNumber());

            }
            return mealDto;
        }).collect(Collectors.toList());

        //将查询到的数据存到redis中
        redisTemplate.opsForValue().set(key,mealDtoes,60, TimeUnit.MINUTES);

        return mealDtoes;

    }
}
