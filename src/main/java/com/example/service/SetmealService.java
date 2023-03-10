package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.Setmeal;
import com.example.dto.DishDto;
import com.example.dto.SetmealDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface SetmealService extends IService<Setmeal> {
    //新增套餐同时保存菜品数据
    public void saveWithDishes(SetmealDto setmealDto);
    //修改套餐前的回显套餐
    public SetmealDto getByIdWithDished(Long id);
    //修改套餐
    public void updateWithDishes(SetmealDto setmealDto);
    //删除套餐
    public void removeWithDishes(List<Long> ids);
}
