package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.User;

public interface UserService extends IService<User> {
    //发送邮件
    void sendMsg(String to,String subject,String text);
}
