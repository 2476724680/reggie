package com.w.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.w.common.R;
import com.w.pojo.Employee;
import com.w.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {


    @Autowired
    private EmployeeService employeeService;



    /*
    * 登录功能
    * 成功登录条件：
    * 1.用户存在+状态不是禁用
    * 2.密码正确
    *
    * */

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //根据username查询用户
        LambdaQueryWrapper<Employee> lqw=new LambdaQueryWrapper<Employee>();
        lqw.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(lqw);

        //判断用户存在和状态
        if(emp==null){
            return R.error("登录失败");
        }else if(emp.getStatus()==0){
            return R.error("状态被禁用");
        }else {
            //如果用户存在且为启用状态 -->加密
            String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
            if(!(password.equals(emp.getPassword()))){
                return R.error("登陆失败");
            }
        }

        //如果完全通过  就将该员工id放到Session中
        request.getSession().setAttribute("employee",emp.getId());

        return R.success(emp);
    }

    /*
    * 退出功能
    * 1.删除Session中的id
    * 2.返回退出结果
    *
    * */

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){

        //删除Session中的id
        request.getSession().removeAttribute("employee");

        return R.success("退出成功");


    }

    /*
    * 添加员工功能
    *
    * */
    @PostMapping
    public R<String> add(@RequestBody Employee employee,HttpServletRequest request){

        //设置初始密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //设置必要属性   放到公共字段中处理
        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateUser((Long)request.getSession().getAttribute("employee"));
        employee.setUpdateUser((Long)request.getSession().getAttribute("employee"));*/

        //调用方法 保存数据
        employeeService.save(employee);

        return R.success("新增成功");
    }


    /*
    * 分页查询功能
    *  page->当前页面
    *  pageSize-> 查询条数
    *  name —>根据名字模糊查询
    */
    @GetMapping("/page")
    public R<Page<Employee>> page(int page,int pageSize,String name){

        //获得page对象
        Page<Employee> pages=new Page<Employee>(page,pageSize);

        //条件查询
        LambdaQueryWrapper<Employee> lqw=new LambdaQueryWrapper<Employee>();
         //1.添加过滤条件
        lqw.like(name!=null,Employee::getName,name);
         //2.添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pages,lqw);

        return R.success(pages);
    }

    /*
    *
    * 根据id修改员工信息功能
    *
    * */
    @PutMapping
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){

        //更新修改时间和修改人员
        /*employee.setUpdateUser((Long)request.getSession().getAttribute("employee") );
        employee.setUpdateTime(LocalDateTime.now());*/

        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }


    /*
    *
    *   员工数据回显
    *
    * */

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){

        Employee emp = employeeService.getById(id);
        if(emp!=null){
            return R.success(emp);
        }
       return R.error("查询失败");
    }






}
