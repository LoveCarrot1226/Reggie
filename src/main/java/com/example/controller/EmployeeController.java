package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.domain.Employee;
import com.example.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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

    //登录请求
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码进行MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据页面提交的username查询数据库
        LambdaQueryWrapper<Employee> lqw=new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(lqw);
        //3.没有查询到此用户，返回失败信息
        if(emp==null){
            return Result.error("登陆失败");
        }
        //4.比对密码
        if(!emp.getPassword().equals(password)){
            return Result.error("密码不正确");
        }
        //5.查看员工状态是否被禁用，1为可用
        if (emp.getStatus()==0){
            return  Result.error("账号已禁用");
        }
        //6.登陆成功，将员工id存入Session并返回登陆成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return Result.success(emp);
    }

    //登出请求
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request){
        //1.清理Session中保存的员工id
        request.getSession().removeAttribute("employee");

        return Result.success("退出成功");
    }
    //新增员工
    @PostMapping
    public Result<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息： {}",employee.toString());
        //给新增员工设置初始密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //Long employeeId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(employeeId);
        //employee.setUpdateUser(employeeId);
        employeeService.save(employee);
        return Result.success("新增员工成功");
    }
    //员工分页查询
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){//参数名和前端设置的字段名匹配，自动封装
        log.info("page= {},pageSize= {},name= {}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> lqw=new LambdaQueryWrapper();
        lqw.like(StringUtils.isNotEmpty(name),Employee::getName,name);//用户按name查询员工的话
        lqw.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,lqw);
        return Result.success(pageInfo);
    }

    // 员工信息更新：启用/禁用员工账号---员工状态status更新
    @PutMapping
    public Result<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        //Long empId=(Long)request.getSession().getAttribute("employee");
        //employee.setUpdateUser(empId);
        //employee.setUpdateTime(LocalDateTime.now());
        employeeService.updateById(employee);

        return Result.success("员工信息更新成功");
    }

    //编辑员工信息,回显
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee=employeeService.getById(id);
        if(employee!=null){
            return Result.success(employee);
        }
        return Result.error("没有这个员工");
    }

}
