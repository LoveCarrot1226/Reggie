package com.example.controller;


import com.example.common.Result;
import com.example.domain.Orders;
import com.example.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;


    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){//封装的orders只有address_id,remark备注,,payMethod

        orderService.submit(orders);
        return Result.success("下单成功");

    }
}
