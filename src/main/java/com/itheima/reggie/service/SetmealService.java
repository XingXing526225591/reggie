package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，需要同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    void removeWithDish(List<Long> ids);

    /**
     * 根据id查询套餐所有信息
     * @param id
     * @return
     */
    SetmealDto getByIdWithAll(Long id);

    /**
     * 更新套餐数据，需要操作setmeal和setmeal_dish两张表
     * @param setmealDto
     */
    void updateWithSetmealDish(SetmealDto setmealDto);

    /**
     * 批量启停售
     * @param ids
     */
    void updateStatus(Integer status,List<Long> ids);
}
