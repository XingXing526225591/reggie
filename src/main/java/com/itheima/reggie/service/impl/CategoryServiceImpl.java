package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomerException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategroyService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategroyService {
   @Autowired
   private DishService dishService;
   @Autowired
   private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前要进行判断
     * @param id
     */
    @Override
    public void remove(Long id){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类Id进行查询
        queryWrapper.eq(Dish::getCategoryId,id);
        int count = dishService.count(queryWrapper);
        //查询当前分类是否关联了菜品，如已关联则抛出一个异常
        if(count > 0){
            //已经关联菜品，抛出一个业务异常
            throw new CustomerException("当前分类下关联了菜品，不能删除");
        }
        // 查询当前分类是否关联了套餐，如已关联则抛出一个异常
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();

        setmealQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count1 = setmealService.count(setmealQueryWrapper);

        if(count1 > 0){
            //已经关联套餐，抛出一个业务异常
            throw new CustomerException("当前分类下关联了套餐，不能删除");
        }
        //正常删除
        super.removeById(id);
    };
}
