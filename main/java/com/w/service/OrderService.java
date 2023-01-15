package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.pojo.Orders;


public interface OrderService extends IService<Orders> {


    /*
    * 订单提交
    * */
    void submit(Orders orders);
}
