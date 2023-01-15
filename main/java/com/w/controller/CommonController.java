package com.w.controller;


import com.baomidou.mybatisplus.annotation.Version;
import com.w.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;


/*
*
* 文件上传下载
* */
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){

        /*
        * file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        *
        * */

        //原始文件名
        String originalFilename = file.getOriginalFilename();
        //.jpg后缀名
        String suffix = originalFilename.substring(originalFilename.indexOf("."));
        //UUID重新生成文件名，防止文件名重复
        String fileName = UUID.randomUUID().toString()+suffix;

        //创建一个目录对象
        File dir=new File(basePath);
        //如果目录不存在  创建一个新目录
        if(!dir.exists()){
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return R.success(fileName);
    }



    /*
    * 文件下载
    *
    * */

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            //创建输入流 读取文件
            FileInputStream fileInputStream=new FileInputStream(new File(basePath+name));

            //创建输出流 输出文件
            ServletOutputStream outputStream = response.getOutputStream();

            //响应格式
            response.setContentType("image/jpeg");

            byte[] bytes=new byte[1024];
            int len=0;

            //读取
            while((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes);
                outputStream.flush();
            }
            //关闭资源
            fileInputStream.close();
            outputStream.close();

        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }




}
