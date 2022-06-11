package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategroyService;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetMealController {
    @Autowired
  private CategroyService categroyService;
   @Autowired
    private SetmealService setmealService;

   @Autowired
    private SetMealDishService setMealDishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
   @PostMapping
   @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
       log.info("套餐信息 :{}",setmealDto);

       setmealService.saveWithDish(setmealDto);

       return R.success("新增套餐成功");
   }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
   @GetMapping("/page")
   public R<Page> page(Integer page,Integer pageSize,String name){
       //构造一个分页构造器
       Page<Setmeal> pageInfo = new Page<>(page,pageSize);
       Page<SetmealDto> setmealDtoPage = new Page<>();
       LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
       //根据查询条件,根据name进行like模糊查询
       queryWrapper.like(name != null,Setmeal::getName,name);

       //添加排序条件，根据更新时间降序排序
       queryWrapper.orderByDesc(Setmeal::getUpdateTime);

       setmealService.page(pageInfo,queryWrapper);

       //对象拷贝
       BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
       List<Setmeal> records = pageInfo.getRecords();
       List<SetmealDto> list =   records.stream().map((item) -> {
            SetmealDto setmealDto =new SetmealDto();
            //对象拷贝
           BeanUtils.copyProperties(item,setmealDto);
           //分类Id
           Long categoryId = item.getCategoryId();
           //根据分类Id查询分类名称
           Category byId = categroyService.getById(categoryId);
           if (byId != null){
               //分类名称
               String categoryName = byId.getName();

               setmealDto.setCategoryName(categoryName);
           }
           return setmealDto;
       }).collect(Collectors.toList());

       setmealDtoPage.setRecords(list);
       return R.success(setmealDtoPage);
   }

   @DeleteMapping
   @CacheEvict(value = "setmealCache",allEntries = true)
   public R<String> delete(@RequestParam List<Long> ids){
       log.info("dis : {}",ids);
       setmealService.removeWithDish(ids);
       return R.success("删除成功");

   }

   @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
       SetmealDto byIdWithAll = setmealService.getByIdWithAll(id);

       return R.success(byIdWithAll);
   }

    /**
     * 更新套餐信息
     * @param setmealDto
     * @return
     */
   @PutMapping
   @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){
       log.info("setmealDto 的值为 {}",setmealDto);
       setmealService.updateWithSetmealDish(setmealDto);
       return R.success("修改套餐成功");
   }
   @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status,@RequestParam List<Long> ids){
       log.info("ids的值为: {},状态为{}",ids,status);
        setmealService.updateStatus(status,ids);
       return R.success("更新成功！");
   };

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
   @GetMapping("/list")
   @Cacheable(value = "setmealCache",key = "#p0.categoryId + '_' + #setmeal.status")
   public R<List<Setmeal>> list(Setmeal setmeal){
      LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
      queryWrapper.eq(setmeal.getStatus()!= null,Setmeal::getStatus,setmeal.getStatus());
      queryWrapper.orderByDesc(Setmeal::getUpdateTime);

       List<Setmeal> list = setmealService.list(queryWrapper);

       return R.success(list);
   }
}
