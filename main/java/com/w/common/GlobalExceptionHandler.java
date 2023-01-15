package com.w.common;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice(annotations = {RestController.class})
@ResponseBody  //最后要返回JSON数据
public class GlobalExceptionHandler {


    /*
    *  处理新增内容重复异常
    *
    * */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){

        //处理重复异常
        if(ex.getMessage().contains("Duplicate entry")){
            String[] splits = ex.getMessage().split(" ");
            return R.error(splits[2]+"已存在");
        }

        return R.error("未知错误");
    }



    /*
    * 处理删除菜品分类关联菜品和套餐异常
    *
    * */
    @ExceptionHandler(CustomException.class)
    public R<String> customExceptionHandler(CustomException ex){

        return R.error(ex.getMessage());

    }






}
