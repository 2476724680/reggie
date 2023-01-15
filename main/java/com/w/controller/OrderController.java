package com.w.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.w.common.BaseContext;
import com.w.common.R;
import com.w.dto.OrderDto;
import com.w.pojo.OrderDetail;
import com.w.pojo.Orders;
import com.w.pojo.ShoppingCart;
import com.w.service.OrderDetailService;
import com.w.service.OrderService;
import com.w.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;


    /*
    * 订单提交
    * */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){

        orderService.submit(orders);

        return R.success("订单提交成功");
    }

    /*
    * 订单展示  订单以及订单详情
    * */
    @GetMapping("/userPage")
    public R<Page<OrderDto>> page(int page,int pageSize){

        Page<Orders> pageInfo=new Page<Orders>(page,pageSize);

        LambdaQueryWrapper<Orders> lqw=new LambdaQueryWrapper<Orders>();
        lqw.orderByDesc(Orders::getCheckoutTime);
        lqw.eq(Orders::getUserId, BaseContext.getCurrentId());

        Page<Orders> pages = orderService.page(pageInfo, lqw);

        //根据订单获取对应的订单细节
        Page<OrderDto> orderDtoPage=new Page<OrderDto>();

        //拷贝
        BeanUtils.copyProperties(pages,orderDtoPage,"records");

        List<OrderDto> orderDtos = pages.getRecords().stream().map(item -> {

            OrderDto orderDto = new OrderDto();

            //拷贝
            BeanUtils.copyProperties(item, orderDto);

            //获取订单细节
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<OrderDetail>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(lambdaQueryWrapper);

            //存入
            orderDto.setOrderDetails(orderDetails);

            return orderDto;

        }).collect(Collectors.toList());

        orderDtoPage.setRecords(orderDtos);

        return R.success(orderDtoPage);


    }

    /*
    * 订单分页查询
    * */

    @GetMapping("/page")
    public R<Page<Orders>> list(int page, int pageSize, String beginTime,String endTime,Integer number){

        Page<Orders> ordersPage=new Page<Orders>(page,pageSize);
        LambdaQueryWrapper<Orders> lqw=new LambdaQueryWrapper<Orders>();
        //根据订单号查询
        lqw.like(number!=null,Orders::getNumber,number);

        //将beginTime和endTime 转换成LocalDateTime
        if(beginTime!=null&&endTime!=null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
             LocalDateTime beginTimeLocal = LocalDateTime.parse(beginTime, df);
             LocalDateTime endTimeLocal= LocalDateTime.parse(endTime, df);
            //根据时间查询
            lqw.le(endTimeLocal!=null,Orders::getCheckoutTime,endTimeLocal);
            lqw.ge(beginTime!=null,Orders::getCheckoutTime,beginTimeLocal);
        }

        //根据付款时间倒序
        lqw.orderByDesc(Orders::getCheckoutTime);

        orderService.page(ordersPage, lqw);

        return  R.success(ordersPage);
    }


    /*
    * 修改订单状态
    * */
    @PutMapping
    public R<String> status(@RequestBody Orders orders){

        LambdaUpdateWrapper<Orders> luw=new LambdaUpdateWrapper<Orders>();
        luw.eq(orders.getId()!=null,Orders::getId,orders.getId());
        luw.set(orders.getStatus()!=null,Orders::getStatus,orders.getStatus());

        orderService.update(luw);

        return R.success("修改成功");
    }


    /*
    * 再来一单  获得原先订单细节 加到购物车中
    * */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){


        //根据订单id获取订单细节
        LambdaUpdateWrapper<OrderDetail> lqw=new LambdaUpdateWrapper<OrderDetail>();
        lqw.eq(orders.getId()!=null,OrderDetail::getOrderId,orders.getId());
        List<OrderDetail> orderDetails = orderDetailService.list(lqw);

        //先清空购物车数据  根据userId
        shoppingCartService.clean(BaseContext.getCurrentId());

        //将再来一单的订单细节加入到购物车
        orderDetails.stream().forEach(orderDetail -> {

            ShoppingCart shoppingCart=new ShoppingCart();

            //拷贝
            BeanUtils.copyProperties(orderDetail,shoppingCart);

            //购物车添加菜品
            shoppingCartService.add(shoppingCart);
        });

        return R.success("再来一单完成");

    }
}
