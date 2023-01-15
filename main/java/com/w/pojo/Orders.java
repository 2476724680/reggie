package com.w.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Orders {

    private Long id;

    //订单号
    private String number;

    //订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
    private Integer status;

    private Long userId;

    private Long addressBookId;

    private LocalDateTime orderTime;

    private LocalDateTime checkoutTime;

    //支付方式 1微信,2支付宝
    private Integer payMethod;

    //金额
    private BigDecimal amount;

    //备注
    private String remark;

    private String phone;

    private String address;

    private String userName;

    private String consignee;

}
