package com.w.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.w.pojo.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
