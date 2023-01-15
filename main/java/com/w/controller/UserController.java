package com.w.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.w.common.R;
import com.w.pojo.User;
import com.w.service.UserService;
import com.w.utils.SMSUtils;
import com.w.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;


    //手机登录  ——>发送验证码
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){

        //获取手机号
        String phone = user.getPhone();

        //生成四位验证码
        if(phone!=null){

            String code= ValidateCodeUtils.generateValidateCode(4).toString();

            log.info("code:{}",code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //将生成的验证码存到Session中
            //session.setAttribute(phone,code);

            //将生成验证码缓存到redis中 并设置有效期为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("短信发送失败");
    }

    //手机登录  ——>移动端用户登录
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){

        //获取手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        //从Session中获取保存的code验证码
        //String codeInSession = session.getAttribute(phone).toString();

        //从redis中获取验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        //比对验证码
        if(codeInSession!=null&&codeInSession.equals(code)){
            //能够登录 看对应的手机号是否存在于数据库 不存在就直接保存到数据库中
            LambdaQueryWrapper<User> lqw=new LambdaQueryWrapper<User>();
            lqw.eq(User::getPhone,phone);
            User user = userService.getOne(lqw);

            //如果查不到 就存入新的用户
            if(user==null){
                user=new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //登录成功 要将userId存入Session中
            session.setAttribute("user",user.getId());

            //登录成功 将redis中的验证码删除
            redisTemplate.delete(phone);

            return R.success(user);
        }
        return R.error("登录失败");
    }


    /*
    * 用户退出
    * */
    @PostMapping("/loginout")
    public R<String> logout(HttpSession session){

        //删除Session中的数据
        session.removeAttribute("user");

        return R.success("退出成功");
    }
}
