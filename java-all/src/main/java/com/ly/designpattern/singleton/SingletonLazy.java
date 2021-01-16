package com.ly.designpattern.singleton;

/**
 * @Description 单例模式-懒汉模式
 *  双重校验锁实现对象单例（线程安全）
 * @Created by Administrator
 * @Date 2020/10/12 23:29
 */
public class SingletonLazy {
    //volatile 1、线程可见性 2、禁止指令重排
    private volatile static SingletonLazy singleton;

    private SingletonLazy() {
    }

    public static SingletonLazy getInstance() {
        if (singleton == null) {    //DCL 双重检查锁double check lock
            synchronized (SingletonLazy.class) {
                if (singleton == null) {
                    //  1.在堆内存开辟内存空间。默认参数基本类型（0，0.0，空）
                    //  2.在堆内存中初始化SingleTon里面的各个参数。
                    //  3.把对象指向堆内存空间。
                    singleton = new SingletonLazy();
                }
            }
        }
        return singleton;
    }


}
