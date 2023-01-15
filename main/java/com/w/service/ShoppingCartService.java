package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.pojo.ShoppingCart;


public interface ShoppingCartService extends IService<ShoppingCart> {

    /*
    * 购物车添加菜品
    * */
    ShoppingCart add(ShoppingCart shoppingCart);

    /*
     * 购物车删除菜品/减少菜品数量
     * */
    ShoppingCart reduce(ShoppingCart shoppingCart);


    /*
    * 清空购物车
    * */
    void clean(Long userId);
}
