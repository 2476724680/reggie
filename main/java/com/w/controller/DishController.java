package com.w.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.w.common.CustomException;
import com.w.common.R;
import com.w.dto.DishDto;
import com.w.pojo.Category;
import com.w.pojo.Dish;
import com.w.pojo.DishFlavor;
import com.w.service.CategoryService;
import com.w.service.DishFlavorService;
import com.w.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;


    /*
    * 添加菜品功能
    * */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){

        dishService.saveWithFlavor(dishDto);
        return R.success("添加菜品成功");
    }

    /*
    * 菜品分页查询
    * 注意：查询到的page里面没有菜品分类名称
    * */

    @GetMapping("/page")
    public R<Page<DishDto>> page(int page,int pageSize,String name){

        //分页查询
        Page<Dish> pageInfo=new Page<Dish>(page,pageSize);

        //条件查询
        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<Dish>();

        lqw.like(name!=null,Dish::getName,name);

        lqw.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,lqw);

        //查询到的page里面没有菜品分类名称
        Page<DishDto> dishDtoPage=new Page<DishDto>();

        //拷贝  不拷贝 records——>封装菜品内容
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        //对pageInfo里面的records进行遍历 对菜品分类数据进行处理
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> collect = records.stream().map(dish -> {
            DishDto dishDto = new DishDto();

            //将原先内容进行拷贝
            BeanUtils.copyProperties(dish, dishDto);


            //得到dish里面的categoryId 从category中找到对应名称
            Category category = categoryService.getById(dish.getCategoryId());

            if(category!=null) {
                //将category名称封装到dishDto中
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(collect);

        return R.success(dishDtoPage);

    }


    /*
    * 菜品数据回显
    * */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){


        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);

    }

    /*
    *
    * 菜品数据修改
    *
    * */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品成功");
    }


    /*
    * 菜品批量停售
    *
    * */

    @PostMapping("/status/0")
    public R<String> haltSales(@RequestParam List<Long> ids){

        if(ids==null||ids.size()==0){
            throw new CustomException("请选择内容");
        }

        dishService.haltSales(ids);

        return R.success("修改停售成功");
    }


    /*
     * 菜品批量起售
     *
     * */

    @PostMapping("/status/1")
    public R<String> startSales(@RequestParam List<Long> ids) {


        if(ids==null||ids.size()==0){
            throw new CustomException("请选择内容");
        }

        LambdaUpdateWrapper<Dish> luw=new LambdaUpdateWrapper<Dish>();
        luw.in(Dish::getId,ids);
        luw.set(Dish::getStatus,1); //起售
        dishService.update(luw);

        return R.success("修改起售成功");
    }


    /*
    *
    * 菜品批量删除 菜品+口味都要删除
    *
    * */
    @DeleteMapping
    public R<String> remove(@RequestParam List<Long> ids){

        if(ids==null||ids.size()==0){
            throw new CustomException("请选择内容");
        }

        dishService.removeWithFlavor(ids);

        return R.success("批量删除成功");

    }



    /*
    * 根据categoryId查找对应菜品  还要看对应菜品在购物车里面是否存在 返回对应数量
    *
    * */
    @GetMapping("/list")

    public R<List<DishDto>> list(Dish dish){

        List<DishDto> list = dishService.list(dish);

        return R.success(list);
    }


}
