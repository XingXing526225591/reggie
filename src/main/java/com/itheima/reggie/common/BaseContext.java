package com.itheima.reggie.common;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseContext {
    private static  ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void set(Long id){
        log.info("存入的id值为 {} 线程地址为 {} 当前线程为 {}",id,threadLocal,Thread.currentThread().getId());
        threadLocal.set(id);
    }
    public static Long getCurrentId(){
        log.info("取出的id值为 {} 线程地址为 {} 当前线程为 {}",threadLocal.get(),threadLocal,Thread.currentThread().getId());
        return threadLocal.get();
    }
}

