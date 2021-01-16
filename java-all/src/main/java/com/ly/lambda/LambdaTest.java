package com.ly.lambda;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/8 22:25
 */
public class LambdaTest {
    public static void main(String[] args) {

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("线程t2");
            }
        },"线程t2");

        Thread t3 = new Thread(() -> System.out.println("线程t3"));




    }
}
