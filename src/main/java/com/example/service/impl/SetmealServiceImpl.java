package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.CustomException;
import com.example.dao.SetmealDao;
import com.example.domain.DishFlavor;
import com.example.domain.Setmeal;
import com.example.domain.SetmealDish;
import com.example.dto.SetmealDto;
import com.example.service.SetmealDishService;
import com.example.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealDao, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Override
    public void saveWithDishes(SetmealDto setmealDto) {
        this.save(setmealDto);

        //保存菜品和套餐的关联关系
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes=setmealDishes.stream().map((setmealDish)->{
            Long id=setmealDto.getId();
            setmealDish.setSetmealId(id);
            return setmealDish;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public SetmealDto getByIdWithDished(Long id) {
        SetmealDto setmealDto=new SetmealDto();
        Setmeal setmeal = this.getById(id);
        BeanUtils.copyProperties(setmeal,setmealDto);
        LambdaQueryWrapper<SetmealDish> lqw=new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list=setmealDishService.list(lqw);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    @Override
    public void updateWithDishes(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        //更新setmealDish表的菜品信息,因为口味表一个setmeal_id对应多条数据，修改比较麻烦--先删除，再新增
        LambdaQueryWrapper<SetmealDish> lqw=new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(lqw);//根据dish_id删除
        List<SetmealDish> list=setmealDto.getSetmealDishes();
        list=list.stream().map((item)->{
            Long setmealId = setmealDto.getId();
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(list);
    }
    @Override
    public void removeWithDishes(List<Long> ids){
        //先查询套餐状态，是否停售，是才删除
        LambdaQueryWrapper<Setmeal> lqw=new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);
        int count = (int)this.count(lqw);
        //不能删除，抛出一个业务异常
        if (count>0){
            throw new CustomException("存在在售套餐，不能删除");
        }
        //可以删除，先删除套餐表数据
        this.removeByIds(ids);

        //再删除SetmealDish表中套餐关联的菜品
        LambdaQueryWrapper<SetmealDish> lqw2=new LambdaQueryWrapper<>();
        lqw2.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lqw2);

    }

}
