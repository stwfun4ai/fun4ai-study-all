package com.ly.concurrent.thread.method;

import com.sun.javafx.geom.transform.Translate2D;

import java.util.concurrent.locks.LockSupport;

/**
 * @Description 交替打印1A2B3C4D5E6F7G
 *  LockSupport park unpack实现
 * @Created by Administrator
 * @Date 2020/10/15 15:55
 */
public class LockSupportTest {


    private static Thread t1, t2;

    public static void main(String[] args) {

        char[] numC = "1234567".toCharArray();
        char[] charC = "ABCDEFG".toCharArray();


        /**
         * LockSupport实现
         */
        System.out.println("===LockSupport实现===");
        t1 = new Thread(() -> {
            for (char num : numC) {
                System.out.print(num);
                LockSupport.unpark(t2); //叫醒t2
                LockSupport.park(); //t1阻塞 当前线程阻塞
            }

        }, "t1");
        t2 = new Thread(() -> {
            for (char c : charC) {
                LockSupport.park(); //t2阻塞
                System.out.print(c);
                LockSupport.unpark(t1); //叫醒t1
            }

        }, "t2");
        t1.start();
        t2.start();





    }
}
