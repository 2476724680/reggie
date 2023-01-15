package com.w.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.mapper.OrderDetailMapper;
import com.w.pojo.OrderDetail;
import com.w.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
