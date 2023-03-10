package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.DishDao;
import com.example.domain.Dish;
import com.example.domain.DishFlavor;
import com.example.dto.DishDto;
import com.example.service.DishFlavorService;
import com.example.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishDao, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到dish表
        super.save(dishDto);

        //保存口味数据到dish_flavor表
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors=flavors.stream().map((flavor)->{
            flavor.setDishId(dishId);
            return flavor;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //dish表查询基本信息
        Dish dish=this.getById(id);
        //dishFlavor表查询口味
        LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(lqw);
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(list);

        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表的基本信息
        this.updateById(dishDto);
        //更新dish_flavor表的口味信息,因为口味表一个dish_id对应多条数据，修改比较麻烦--先删除，再新增
        LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lqw);//根据dish_id删除

        Long dishId = dishDto.getId();//设置好dish_id,再把flavors集合存入dish_flavor表
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors=flavors.stream().map((flavor)->{
            flavor.setDishId(dishId);
            return flavor;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

    }
}
