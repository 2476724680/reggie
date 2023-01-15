package com.w.dto;

import com.w.pojo.OrderDetail;
import com.w.pojo.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto extends Orders {

    //订单详情
    private List<OrderDetail> orderDetails;


}
