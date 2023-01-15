package com.w.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w.mapper.DishFlavorMapper;
import com.w.pojo.DishFlavor;
import com.w.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
