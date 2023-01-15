package com.w.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.BaseContext;
import com.w.common.CustomException;
import com.w.dto.DishDto;
import com.w.mapper.DishMapper;
import com.w.pojo.*;
import com.w.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private MealDishService mealDishService;

    @Autowired
    private MealService mealService;

    @Autowired
    @Lazy
    private ShoppingCartService shoppingCartService;

    /*
     *
     * 根据DishDto封装数据 在dish和dish_flavor两张表中存储数据
     *
     * */
    public void saveWithFlavor(DishDto dishDto){

        //存储dish数据
        super.save(dishDto);

        //获得dish id
        Long dishId = dishDto.getId();

        //对dishFlavor数据进行处理 ——>加入dish id
        List<DishFlavor> dishFlavors = dishDto.getFlavors();

        dishFlavors= dishFlavors.stream().map(dishFlavor -> {
            dishFlavor.setDishId(dishId);
            return dishFlavor;
        }).collect(Collectors.toList());

        //存储dishFlavor数据
        dishFlavorService.saveBatch(dishFlavors);


    }


    /*
    *
    * 数据回显 菜品+口味
    *
    * */
    @Override
    public DishDto getByIdWithFlavor(Long id) {

        //获得菜品数据
        Dish dish = super.getById(id);

        DishDto dishDto=new DishDto();

        //拷贝到DishDto中
        BeanUtils.copyProperties(dish,dishDto);

        //口味条件查询
        LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<DishFlavor>();

        lqw.eq(DishFlavor::getDishId,id);

        List<DishFlavor> flavors = dishFlavorService.list(lqw);

        //口味封装到DishDto中
        dishDto.setFlavors(flavors);

        return dishDto;

    }

    /*
     * 数据更新 菜品+口味
     *
     * */
    @Override
    public void updateWithFlavor(DishDto dishDto) {


        //修改dish数据
        super.updateById(dishDto);

        //删除原本的fishFlavors数据 再重新添加
        //根据原先的dishId删除
        LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<DishFlavor>();

        //条件删除
        lqw.eq(DishFlavor::getDishId,dishDto.getId());
        //删除原来口味
        dishFlavorService.remove(lqw);

        //添加口味  获取flavors  遍历添加dish id
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors =flavors.stream().map(dishFlavor -> {
            dishFlavor.setDishId(dishId);
            return dishFlavor;
        }).collect(Collectors.toList());

        //存储数据
        dishFlavorService.saveBatch(flavors);
    }


    /*
     * 菜品删除
     * 能删除前提：
     * 1.菜品本身是停售的
     * 2.菜品没有与套餐关联
     * */

    @Override
    public void removeWithFlavor(List<Long> ids){

        //查询对应id 是否是停售的
        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<Dish>();

        lqw.in(Dish::getId,ids);
        lqw.eq(Dish::getStatus,1);

        //有一个菜品没有停售都不能删除
        int count = super.count(lqw);

        //如果没有停售 抛出业务异常
        if(count>0){
            throw new CustomException("当前选择的菜品存在正在售卖菜品，不能删除");
        }

        //判断该菜品与套餐是否有关联
        LambdaQueryWrapper<MealDish> lqw1=new LambdaQueryWrapper<MealDish>();

        lqw1.in(MealDish::getDishId,ids);

        int count1 = mealDishService.count(lqw1);

        //如果有关联 抛出业务异常
        if(count1>0){
            throw new CustomException("当前菜品存在与套餐关联菜品，不能删除");
        }

        //如果能删除 先删除菜品 在删除菜品对应口味
        super.removeByIds(ids);

        //删除口味
        LambdaQueryWrapper<DishFlavor> lqw2=new LambdaQueryWrapper<DishFlavor>();

        lqw2.in(DishFlavor::getDishId,ids);

        dishFlavorService.remove(lqw2);

    }



    /*
     *
     *  菜品批量停售前提
     * 1.没有关联的套餐或者关联的套餐是停售的
     *
     *
     * */
    @Override
    public void haltSales(List<Long> ids) {

        if(ids==null||ids.size()==0){
            throw new CustomException("请选择内容停售");
        }

        //没有关联的套餐或者关联的套餐是停售的
        //先查看meal_dish表获得对应的stemealId
        LambdaQueryWrapper<MealDish> lqw=new LambdaQueryWrapper<MealDish>();
        lqw.in(MealDish::getDishId,ids);
        List<MealDish> list = mealDishService.list(lqw);

        //如果list不为空 说明有套餐关联菜品 要进一步查看套餐是否是停售的
        //由于菜品对应相同的套餐 先获取不同的套餐id 再去数据库查看状态
        if(list!=null&&list.size()!=0) {
            List<Long> mealId = list.stream()
                                    .map(lists -> lists.getSetmealId())
                                    .distinct()
                                    .collect(Collectors.toList());

            //查看套餐状态
            LambdaQueryWrapper<Meal> lqw1 = new LambdaQueryWrapper<Meal>();
            lqw1.in(Meal::getId, mealId);
            lqw1.eq(Meal::getStatus, 1); //0 停售 1 起售

            int count = mealService.count(lqw1);

            if (count > 0) {
                //说明关联套餐中存在起售套餐 不能停售 抛出业务异常
                throw new CustomException("当前菜品关联套餐正在售卖 不能停售");
            }
        }
        //如果可以停售

        LambdaUpdateWrapper<Dish> luw=new LambdaUpdateWrapper<Dish>();

        luw.in(Dish::getId,ids);
        luw.set(Dish::getStatus,0);  //停售
        super.update(luw);

    }



    /*
     * 根据categoryId查找对应菜品  还要看对应菜品在购物车里面是否存在 返回对应数量
     *
     * */

    public List<DishDto> list(Dish dish){

        //根据categoryId查询
        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<Dish>();

        lqw.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //起售才能找到
        lqw.eq(Dish::getStatus,1);

        List<Dish> dishes = super.list(lqw);



        List<DishDto> dishDtos = dishes.stream().map(dish1 -> {

            DishDto dishDto=new DishDto();

            //拷贝
            BeanUtils.copyProperties(dish1, dishDto);

            LambdaQueryWrapper<DishFlavor> lqwFlavor = new LambdaQueryWrapper<DishFlavor>();
            //根据菜品查询对应口味
            lqwFlavor.eq(DishFlavor::getDishId, dish1.getId());
            List<DishFlavor> list = dishFlavorService.list(lqwFlavor);
            dishDto.setFlavors(list);


            //根据菜品id以及用户id 查询购物车中有没有添加
            LambdaQueryWrapper<ShoppingCart> lqwShop=new LambdaQueryWrapper<ShoppingCart>();
            lqwShop.eq(ShoppingCart::getDishId,dish1.getId());
            lqwShop.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
            ShoppingCart shoppingCart = shoppingCartService.getOne(lqwShop);

            if(shoppingCart!=null){
                //说明在购物车有这个菜品  存入数量
                dishDto.setNumber(shoppingCart.getNumber());
            }

            return dishDto;

        }).collect(Collectors.toList());

        return dishDtos;
    }
}
