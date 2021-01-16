package com.ly.designpattern.singleton;

/**
 * @Description 单例模式-饿汉模式
 * @Created by Administrator
 * @Date 2020/10/15 22:25
 */
public class SingletonHungry {
    private static SingletonHungry instance = new SingletonHungry();

    private SingletonHungry() {
    }

    public static SingletonHungry getInstance() {
        return instance;
    }
}
