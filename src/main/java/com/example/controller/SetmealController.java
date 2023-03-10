package com.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.domain.*;
import com.example.dto.DishDto;
import com.example.dto.SetmealDto;
import com.example.service.CategoryService;
import com.example.service.SetmealDishService;
import com.example.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    //保存套餐
    @PostMapping
    public Result<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDishes(setmealDto);

        return Result.success("新增套餐成功");
    }

    //套餐分页查询
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){//参数名和前端设置的字段名匹配，自动封装
        log.info("page= {},pageSize= {},name= {}",page,pageSize,name);
        //构造分页构造器
        Page<Setmeal> pageInfo=new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage=new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Setmeal> lqw=new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);//用户按name查询菜品的话
        lqw.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo,lqw);
        //拷贝成SetmealDto，加入菜品分类的字段
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list=records.stream().map((item)->{
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category!=null) setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(list);
        return Result.success(setmealDtoPage);
    }

    //修改套餐前的回显
    @GetMapping("/{id}")
    public Result<SetmealDto> getById(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDished(id);

        return Result.success(setmealDto);
    }
    //修改套餐
    @PutMapping
    public Result<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDishes(setmealDto);
        return Result.success("菜品修改成功");
    }

    /**
     * s删除套餐同时删除和套餐关联的菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDishes(ids);
        return Result.success("删除套餐成功");
    }

    @GetMapping("/list")
    public Result<List<Setmeal>> list(Setmeal setmeal){//使用分类的实体类封装传过来的参数
        LambdaQueryWrapper<Setmeal> lqw=new LambdaQueryWrapper<>();
        //添加查询条件：1.根据套餐分类id查询菜品 2.只查询在售套v餐
        lqw.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lqw.eq(Setmeal::getStatus,1);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list=setmealService.list(lqw);

        return Result.success(list);
    }

}
