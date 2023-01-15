package com.w.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.w.common.BaseContext;
import com.w.common.CustomException;
import com.w.common.R;
import com.w.pojo.AddressBook;
import com.w.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /*
    * 新增收获地址
    * */
    @PostMapping
    public R<String> addAddress(@RequestBody AddressBook addressBook, HttpSession session){

        log.info("UserId:{}",session.getAttribute("user"));
        //设置对应的userId
        addressBook.setUserId((Long) session.getAttribute("user"));

        addressBookService.save(addressBook);

        return R.success("新增地址成功");

    }


    /*
    * 全部地址展示
    * */
    @GetMapping("/list")
    public R<List<AddressBook>> list(HttpSession session){

        Long userId =(Long) session.getAttribute("user");

        LambdaQueryWrapper<AddressBook> lqw=new LambdaQueryWrapper<AddressBook>();
        lqw.eq(AddressBook::getUserId,userId);
        lqw.orderByDesc(AddressBook::getIsDefault).orderByDesc(AddressBook::getUpdateTime);

        List<AddressBook> list = addressBookService.list(lqw);

        return R.success(list);
    }

    /*
    *   设置默认地址  只能有一个默认地址 要先把之前的默认地址取消 再设置默认地址
    *
    * */
    @PutMapping("/default")
    public R<AddressBook> defaultAddress(@RequestBody AddressBook addressBook,HttpSession session){

        //修改原来的默认地址
        LambdaUpdateWrapper<AddressBook> luwDefault=new LambdaUpdateWrapper<AddressBook>();
        //获得该用户的默认地址
        luwDefault.eq(AddressBook::getUserId,session.getAttribute("user"));
        luwDefault.eq(AddressBook::getIsDefault,1);
        //修改本身默认地址
        luwDefault.set(AddressBook::getIsDefault,0);
        addressBookService.update(luwDefault);

        //  1为默认地址
        LambdaUpdateWrapper<AddressBook> luw=new LambdaUpdateWrapper<AddressBook>();
        luw.eq(AddressBook::getId,addressBook.getId());
        luw.set(AddressBook::getIsDefault,1);

        addressBookService.update(luw);
        addressBook.setIsDefault(1);

        return R.success(addressBook);
    }


    /*
    *
    *   地址数据回显
    * */
    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id){

        AddressBook addressBook = addressBookService.getById(id);

        if(addressBook!=null){
            return R.success(addressBook);
        }else {
            return R.error("没有找到地址");
        }

    }

    /*
    *
    * 地址数据修改
    * */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){

       addressBookService.updateById(addressBook);

       return R.success("修改成功");

    }

    /*
    * 地址数据删除
    *
    * */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){

        addressBookService.removeByIds(ids);

        return R.success("删除地址成功");

    }


    /*
    * 结算显示默认地址
    * 如果有默认地址 直接返回  如果没有 就获取最新存入的地址
    * */
    @GetMapping("/default")
    public R<AddressBook> defaultForAddress(){

        //根据userId获取对应的默认地址
        LambdaQueryWrapper<AddressBook> lqw=new LambdaQueryWrapper<AddressBook>();
        lqw.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        lqw.eq(AddressBook::getIsDefault,1);

        AddressBook addressBook = addressBookService.getOne(lqw);

        if(addressBook!=null){
            return R.success(addressBook);
        }else {
            throw new CustomException("请先添加默认地址");
        }
        /*
        //如果有默认地址 直接返回  如果没有 就获取最新存入的地址
        if(addressBook!=null){
            //说明有默认地址

            return R.success(addressBook);
        }

        //如果没有默认地址
        LambdaQueryWrapper<AddressBook> lqwNotFault=new LambdaQueryWrapper<AddressBook>();
        lqwNotFault.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        lqwNotFault.eq(AddressBook::getIsDefault,0);
        lqwNotFault.orderByDesc(AddressBook::getUpdateTime);

        List<AddressBook> list = addressBookService.list(lqwNotFault);
        AddressBook addressBook1 =  list.get(0);

        return R.success(addressBook1);

         */
    }

}
