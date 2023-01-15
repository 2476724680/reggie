package com.w.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.w.common.BaseContext;
import com.w.common.R;
import com.w.pojo.ShoppingCart;
import com.w.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {


    @Autowired
    private ShoppingCartService shoppingCartService;

    /*
    * 购物车添加菜品
    * */

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        ShoppingCart sc = shoppingCartService.add(shoppingCart);

        return R.success(sc);

    }


    /*
    * 购物车删除菜品/减少菜品数量
    * */

    @PostMapping("/sub")
    public R<ShoppingCart> reduce(@RequestBody ShoppingCart shoppingCart){

        /*
        * 如果数量是1 就直接删除 此时要返回对应的菜品或者是套餐
        *
        * 如果数量>1 数量-1 返回shoppingCart
        *
        * */
        ShoppingCart shoppingCart1= shoppingCartService.reduce(shoppingCart);

        return R.success(shoppingCart1);
    }


    /*
    * 展示购物车内容
    * */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){



        //通过userId获得对应购物车内容
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<ShoppingCart>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        lqw.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(lqw);

        return R.success(list);

    }

    /*
    * 清空购物车
    * */
    @DeleteMapping("/clean")
    public R<String> clean(){

        shoppingCartService.clean(BaseContext.getCurrentId());

        return R.success("清空成功");
    }






}
