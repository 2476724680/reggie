package com.w.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.common.BaseContext;
import com.w.common.CustomException;
import com.w.common.R;
import com.w.mapper.OrderMapper;
import com.w.pojo.*;
import com.w.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Annotation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDetailService orderDetailService;

    /*
    * 订单提交
    * */
    @Override
    public void submit(Orders orders) {


        //获得当前用户id  根据id获得相应购物车内容
        Long userId = BaseContext.getCurrentId();
        orders.setUserId(userId);

        //获取订单号
        long orderId = IdWorker.getId();

        //获取用户信息
        User user = userService.getById(userId);


        //获得购物车内容
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<ShoppingCart>();
        lqw.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lqw);


        //记录订单总金额
        AtomicInteger amount = new AtomicInteger(0);

        //将购物车内容转移到订单详情中
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("当前购物车没有内容，不能支付");
        }

        List<OrderDetail> orderDetails = shoppingCarts.stream().map(shoppingCart -> {

            OrderDetail orderDetail = new OrderDetail();

            BeanUtils.copyProperties(shoppingCart, orderDetail);

            //设置订单号
            orderDetail.setOrderId(orderId);

            //记录每样菜品的金额
            amount.addAndGet((shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber()))).intValue());

            return orderDetail;
        }).collect(Collectors.toList());


        //设置订单  获取地址
        LambdaQueryWrapper<AddressBook> lqwAddress = new LambdaQueryWrapper<AddressBook>();
        lqwAddress.eq(AddressBook::getUserId, userId);
        lqwAddress.eq(AddressBook::getIsDefault, 1);

        AddressBook addressBook = addressBookService.getOne(lqwAddress);


        //设置相关order属性
        orders.setUserId(userId);
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setStatus(2);  //已付款
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())+
                (addressBook.getCityName() == null ? "" : addressBook.getCityName()) +
                (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName()) +
                (addressBook.getDetail() == null ? "" : addressBook.getDetail()));


        //保存order数据
        super.save(orders);

        //保存orderDetail数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(lqw);
    }
}
