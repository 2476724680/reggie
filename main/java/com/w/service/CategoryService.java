package com.w.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.w.common.R;
import com.w.pojo.Category;

public interface CategoryService extends IService<Category> {

    //删除功能扩展
    public R<String> remove(Long id);
}
