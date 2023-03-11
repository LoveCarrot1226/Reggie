package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.Result;
import com.example.domain.User;
import com.example.service.UserService;
import com.example.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String email=user.getPhone();//因为用邮箱代替手机号接收
        String subject="笑笑奶茶";
        if(email!=null){
            String code = ValidateCodeUtils.generateValidateCode4String(4);
            //生成随机的4位验证码
            String text = "【笑笑奶茶】您好，您本次奶茶订单的验证码为：" + code + "，请尽快回复获取专属奶茶哦，验证码5分钟有效";
            log.info("验证码为：" + code);
            //用邮箱发送验证码
            userService.sendMsg(email,subject,text);

            //将生成的验证码保存到session
            //session.setAttribute(email,code);

            //生成的验证码缓存到Redis，设置有效期5分钟
            redisTemplate.opsForValue().set(email,code,5, TimeUnit.MINUTES);

            return Result.success("验证码发送成功");
        }
        return Result.error("验证码发送失败，请检查您的邮箱");
    }
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map, HttpSession session){
        //获取手机号
        String email=map.get("phone").toString();//因为用邮箱代替手机号接收
        String code=map.get("code").toString();
        //获取session中保存的正确的验证码
        //Object codeRight = session.getAttribute(email);
        Object codeRight =redisTemplate.opsForValue().get(email);
        if(codeRight!=null && codeRight.equals(code)){
            //对比成功则登陆成功
            //如果User表中有这个email，直接登录；没有则是新用户，自动完成注册
            LambdaQueryWrapper<User> lqw=new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone,email);
            User user = userService.getOne(lqw);
            if(user==null){
                user=new User();
                user.setPhone(email);
                user.setStatus(1);
                //取邮箱的前10位为用户名
                user.setName(email.substring(0,10));
                userService.save(user);
            }
            //不保存这个用户名就登不上去，因为过滤器需要得到这个user才能放行，程序才知道你登录了
            session.setAttribute("user", user.getId());
            //登陆成功，则缓存中可以删除这条验证码
            redisTemplate.delete(email);
            return Result.success(user);
        }
        return Result.error("登陆失败");
    }
}
