package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {
    /**
     * 插入操作自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充");
           log.info(metaObject.toString());
           metaObject.setValue("createTime", LocalDateTime.now());
           metaObject.setValue("updateTime", LocalDateTime.now());
           metaObject.setValue("createUser", BaseContext.getCurrentId());
          metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    /**
     * 更新操作自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        long id = Thread.currentThread().getId();
        log.info("线程 {}",id);
        log.info("公共字段自动更新");
        metaObject.setValue("updateTime", LocalDateTime.now());
        Long currentId = BaseContext.getCurrentId();
        log.info("取出来的id为 {}",currentId);
        metaObject.setValue("updateUser", currentId);
        log.info(metaObject.toString());
    }
}
