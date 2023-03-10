package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.Orders;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService extends IService<Orders> {
    //用户下单
    public void submit(Orders orders);
}
