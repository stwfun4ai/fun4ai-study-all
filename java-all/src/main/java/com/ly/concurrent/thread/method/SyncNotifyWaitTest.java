package com.ly.concurrent.thread.method;

/**
 * @Description 交替打印1A2B3C4D5E6F7G
 *  Synchronize wait notify 实现
 * @Created by Administrator
 * @Date 2020/10/15 16:28
 */
public class SyncNotifyWaitTest {
    public static void main(String[] args) {
        char[] numC = "1234567".toCharArray();
        char[] charC = "ABCDEFG".toCharArray();
        /**
         * Synchronize wait notify 实现
         * 如果需要确定顺序先1还是先A可以使用：
         *  1、添加boolean变量判断控制
         *  2、使用CountDownLatch 门闩确定线程执行先后顺序
         */
        System.out.println("===Synchronize wait notify 实现===");
        final Object o = new Object();
        new Thread(() -> {
            synchronized (o) {
                for (char num : numC) {
                    System.out.print(num);
                    try {
                        o.notify();     //是对锁的方法调用 而不是线程
                        o.wait();   //让出锁   一定是先notify再wait,释放锁了就notify不了了
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                o.notify(); //必须，否则无法停止程序,有一个线程wait必须唤醒
            }
        }, "t3").start();
        new Thread(() -> {
            synchronized (o) {
                for (char c : charC) {
                    System.out.print(c);
                    try {
                        o.notify();
                        o.wait();   //让出锁
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                o.notify();
            }
        }, "t4").start();


    }
}
