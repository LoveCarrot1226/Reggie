package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.domain.Category;
import com.example.domain.Dish;
import com.example.domain.Employee;
import com.example.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    //新增分类
    @PostMapping
    public Result<String> save(@RequestBody Category category){

        categoryService.save(category);
        return Result.success("新增分类成功");
    }

    //分类的分页查询
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize){//参数名和前端设置的字段名匹配，自动封装
        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper();
        lqw.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo,lqw);
        return Result.success(pageInfo);
    }
    //根据条件查询分类数据
    @GetMapping("/list")
    public Result<List<Category>> list(Category category){//使用分类的实体类封装传过来的参数
        LambdaQueryWrapper<Category> lqw=new LambdaQueryWrapper<>();
        lqw.eq(category.getType()!=null,Category::getType,category.getType());
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list=categoryService.list(lqw);

        return Result.success(list);
    }
    //删除分类
    @DeleteMapping
    public Result<String> delete(Long id){
        categoryService.removeById(id);//调用重写的removeById方法
        return Result.success("分类信息删除成功");
    }
    //修改分类
    @PutMapping
    public Result<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return Result.success("分类信息修改成功");
    }
}
