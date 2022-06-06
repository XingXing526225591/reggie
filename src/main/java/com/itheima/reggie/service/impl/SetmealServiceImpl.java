package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomerException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.mapper.SetmealDishMapper;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.EmployeeService;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetMealDishService setMealDishService;
    /**
     * 新增套餐，需要同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息，操作setmeal,执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
              item.setSetmealId(setmealDto.getId());
              return item;
        }).collect(Collectors.toList());
        //保存套餐和菜品的关联关系,操作setmeal_dish,执行insert操作
        setMealDishService.saveBatch(setmealDishes);

    }
    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态,确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        //如果不能删除，抛出一个业务异常
        if(count > 0){
            throw new CustomerException("套餐正在售卖中，不能删除");
        }
        //如果可以删除,先删除表中的数据----setmeal
        this.removeByIds(ids);

        //如果可以删除,先删除表中的数据----setmeal__dish
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<String> idss = new ArrayList<>();
        for (Long id : ids) {
            idss.add(String.valueOf(id));
        }
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,idss);

        setMealDishService.remove(lambdaQueryWrapper);

    }
    /**
     * 根据id查询套餐所有信息
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithAll(Long id) {
        //通过id获取setmeal
        Setmeal byId = this.getById(id);
        //判断setmeal是否为空，若为空则抛出一个异常
        if(byId == null){
            throw new CustomerException("获取信息有误，请刷新后重试");
        }

        //获取setmal,并将其装配到SetmealDto中
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        String s = String.valueOf(byId.getId());
        queryWrapper.eq(SetmealDish::getSetmealId,s);
        List<SetmealDish> list = setMealDishService.list(queryWrapper);

        SetmealDto setmealDto = new SetmealDto();

        BeanUtils.copyProperties(byId,setmealDto);

        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }
    /**
     * 更新套餐数据，需要操作setmeal和setmeal_dish两张表
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithSetmealDish(SetmealDto setmealDto) {
       //更新setmeal中的数据
        this.updateById(setmealDto);

        //删除setmeal中原表信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        String s = String.valueOf(setmealDto.getId());
        queryWrapper.eq(SetmealDish::getSetmealId,s);
        setMealDishService.remove(queryWrapper);

        //向setmeal中添加信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setMealDishService.saveBatch(setmealDishes);

    }
    /**
     * 批量停售
     * @param ids
     */
    @Override
    public void updateStatus(Integer status,List<Long> ids) {
        //计数器
        Integer start = 0;
        Integer stop = 0;
        //根据ids获取setmeal

        //创建条件构造器

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        List<Setmeal> list = this.list(queryWrapper);
        for (Setmeal setmeal : list) {
            if (setmeal.getStatus() == 1){
                  start += 1;
            }else {
                stop += 1;
            }
        }
        //判断所选状态是否可更改
        if(start != 0 && stop != 0){
            throw new CustomerException("所选对象状态不一样，请重试");
        }else if(start != 0 && status == 1){
            throw new CustomerException("所选对象均已开启");
        }else if(start == 0 && status == 0){
            throw new CustomerException("所选对象均已关闭");
        }
        //判断是启动还是停止
        if(status == 0){
            //停售
            // 创建条件构造器
        LambdaQueryWrapper<Setmeal> stopQueryWrapper = new LambdaQueryWrapper<>();
        stopQueryWrapper.in(Setmeal::getId,ids);
        stopQueryWrapper.eq(Setmeal::getStatus,1);
        //设置要更新的值
        Setmeal stopSetmeal =new Setmeal();
        stopSetmeal.setStatus(0);
        this.update(stopSetmeal,queryWrapper);
        } else if(status == 1){
            //启售
            //创建条件构造器
            LambdaQueryWrapper<Setmeal> requeryWrapper = new LambdaQueryWrapper<>();
            requeryWrapper.in(Setmeal::getId,ids);
            requeryWrapper.eq(Setmeal::getStatus,0);
            //设置要更新的值
            Setmeal  resetmeal =new Setmeal();
            resetmeal.setStatus(1);
            this.update(resetmeal,requeryWrapper);
        }


    }
}
