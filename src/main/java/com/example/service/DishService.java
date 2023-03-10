package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.Dish;
import com.example.dto.DishDto;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface DishService extends IService<Dish> {
    //新增菜品同时保存口味数据
    public void saveWithFlavor(DishDto dishDto);
    //根据id查询菜品和对应口味
    public DishDto getByIdWithFlavor(Long id);
    //更新菜品信息和口味
    public void updateWithFlavor(DishDto dishDto);
}
