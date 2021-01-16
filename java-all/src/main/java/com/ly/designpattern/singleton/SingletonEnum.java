package com.ly.designpattern.singleton;

/**
 * @Description 单例模式-枚举类型
 * 线程安全的
 * @Created by Administrator
 * @Date 2020/10/15 23:34
 */
public enum SingletonEnum {
    INSTANCE;

    public void method(){
        //todo
    }

    public static void main(String[] args) {
        System.out.println(SingletonEnum.INSTANCE);
    }

}
