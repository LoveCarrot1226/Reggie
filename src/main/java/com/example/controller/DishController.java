package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.domain.Category;
import com.example.domain.Dish;
import com.example.domain.DishFlavor;
import com.example.domain.Employee;
import com.example.dto.DishDto;
import com.example.service.CategoryService;
import com.example.service.DishFlavorService;
import com.example.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;
    //新增菜品
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        //精确清理缓存
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return Result.success("菜品添加成功");
    }
    //菜品分页查询
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){//参数名和前端设置的字段名匹配，自动封装
        log.info("page= {},pageSize= {},name= {}",page,pageSize,name);
        //构造分页构造器
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(name),Dish::getName,name);//用户按name查询菜品的话
        lqw.orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageInfo,lqw);
        //拷贝成DishDto，加入菜品分类的字段
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category!=null) dishDto.setCategoryName(category.getName());
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return Result.success(dishDtoPage);
    }

    //修改菜品前的回显
    @GetMapping("/{id}")
    public Result<DishDto> getById(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return Result.success(dishDto);
    }

    //修改菜品
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);

        /*清理菜品缓存数据
        Set keys=redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);*/
        //精确清理
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return Result.success("菜品修改成功");
    }

    //根据条件查询分类数据
    /*@GetMapping("/list")
    public Result<List<Dish>> list(Dish dish){//使用分类的实体类封装传过来的参数
        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<>();
        //添加查询条件：1.根据菜品分类id查询菜品 2.只查询在售菜品
        lqw.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        lqw.eq(Dish::getStatus,1);
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list=dishService.list(lqw);

        return Result.success(list);
    }*/
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish){//使用分类的实体类封装传过来的参数,这里有CategoryId和Status
        List<DishDto> dishDtoList=null;
        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //先从Redis中获取缓存数据
        dishDtoList= (List<DishDto>) redisTemplate.opsForValue().get(key);
        if(dishDtoList!=null){
            //缓存中存在则直接返回，无需查找数据库
            return Result.success(dishDtoList);
        }
        //不存在，查询数据库
        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<>();
        //添加查询条件：1.根据菜品分类id查询菜品 2.只查询在售菜品
        lqw.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        lqw.eq(Dish::getStatus,1);
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list=dishService.list(lqw);

        dishDtoList=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //设置菜品所属分类的名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null) dishDto.setCategoryName(category.getName());
            //设置菜品的口味数据
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lqw2=new LambdaQueryWrapper<>();
            lqw2.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lqw2);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        //从数据库查询到的菜品数据，放入Redis缓存
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return Result.success(dishDtoList);
    }



}
