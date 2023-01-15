package com.w.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.BaseContext;
import com.w.mapper.ShoppingCartMapper;
import com.w.pojo.ShoppingCart;
import com.w.service.DishService;
import com.w.service.MealService;
import com.w.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {


    @Autowired
    private DishService dishService;

    @Autowired
    private MealService mealService;

    /*
    * 购物车添加菜品
    * */
    @Override
    public ShoppingCart add(ShoppingCart shoppingCart) {

        //添加userId和增加时间
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setCreateTime(LocalDateTime.now());


        //判断当前加入购物车的是套餐还是单独菜品  判断在购物车是否已经存在相同的
        //存在相同的 就直接在数量上加1  不存在就新建一条

        //根据userId查询对应购物车内容
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<ShoppingCart>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        Long dishId = shoppingCart.getDishId();

        if(dishId!=null){
            //说明当前加入的是单独菜品  根据dishId查询
            lqw.eq(ShoppingCart::getDishId,dishId);
        }else {
            //说明当前加入的是套餐  根据setmealId查询
            lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart sc = super.getOne(lqw);

        if(sc!=null){
            //说明已经存在 在原来基础上+1
            sc.setNumber(sc.getNumber()+1);

            super.updateById(sc);

        }else {
            //说明不存在 直接存入
            shoppingCart.setNumber(1);
            super.save(shoppingCart);

            //统一返回sc
            sc=shoppingCart;
        }

        return sc;
    }



    /*
     * 购物车删除菜品/减少菜品数量
     * 数量>1 ->数量-1
     * 数量=1 ->删除数据
     * */
    public ShoppingCart reduce(ShoppingCart shoppingCart){

        ShoppingCart shoppingCart1;


        Long dishId = shoppingCart.getDishId();
        //先判断是减少套餐还是单独菜品
        if(dishId!=null){

            //说明是单独菜品  根据dishId查询
            LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<ShoppingCart>();
            lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
            lqw.eq(ShoppingCart::getDishId,dishId);

            shoppingCart1 = super.getOne(lqw);

            //对应份数
            Integer number = shoppingCart1.getNumber();
            if(number>1){
                //数量-1
                shoppingCart1.setNumber(number-1);
                super.updateById(shoppingCart1);

            } else if (number == 1) {

                shoppingCart1.setNumber(0);
                //直接删除
                super.removeById(shoppingCart1);

            }
        }else {

            //说明是套餐  根据setmealId查询
            LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<ShoppingCart>();
            lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
            lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());

            shoppingCart1 = super.getOne(lqw);

            //对应份数
            Integer number = shoppingCart1.getNumber();
            if(number>1){
                //数量-1
                shoppingCart1.setNumber(number-1);
                super.updateById(shoppingCart1);

            } else if (number == 1) {

                shoppingCart1.setNumber(0);
                //直接删除
                super.removeById(shoppingCart1);

            }
        }

        return shoppingCart1;
    }




    /*
     * 清空购物车
     * */
    public void clean(Long userId){

        //通过userId删除购物车内容
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<ShoppingCart>();
        lqw.eq(ShoppingCart::getUserId,userId);

        super.remove(lqw);
    }
}
