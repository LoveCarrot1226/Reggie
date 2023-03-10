package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.BaseContext;
import com.example.common.Result;
import com.example.domain.ShoppingCart;
import com.example.service.DishService;
import com.example.service.SetmealService;
import com.example.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    //添加到购物车  和  点"+"号
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //设置用户id
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //查询要加入的是否在购物车中已经存在
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        Long dishId = shoppingCart.getDishId();
        if (dishId != null){//是菜品
            lqw.eq(ShoppingCart::getDishId,dishId);
        }else {//是套餐
            lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(lqw);
        //已经存在，就给原来的number+1
        if (one!=null){
            one.setNumber(one.getNumber()+1);
            shoppingCartService.updateById(one);
        }else {//没有，number=1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one=shoppingCart;
        }
        return Result.success(one);
    }

    //查询购物车以便展示---根据user_id
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        lqw.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(lqw);
        return Result.success(list);
    }
    //数量减1 即 点"-"号
    @PostMapping("/sub")
    public Result<String> sub(@RequestBody ShoppingCart shoppingCart){//实际只传了一个dish_id
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        if (dishId != null){//是菜品
            lqw.eq(ShoppingCart::getDishId,dishId);
        }else {//是套餐
            lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(lqw);
        Integer number = one.getNumber();
        //number>1,直接-1
        if (number>1){
            one.setNumber(number-1);
            shoppingCartService.updateById(one);
        }else {//number=1，清空购物车
            shoppingCartService.remove(lqw);
        }
        return Result.success("数量减1");

    }
    //清空购物车
    @DeleteMapping("/clean")
    public Result<String> clean(){
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(lqw);
        return Result.success("清空购物车成功");
    }


}
