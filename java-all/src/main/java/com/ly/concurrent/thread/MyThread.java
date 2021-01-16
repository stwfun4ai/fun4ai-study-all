package com.ly.concurrent.thread;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/8 21:12
 */
public class MyThread implements Runnable {

    @Override
    public void run() {
        System.out.println("1、进入run（）方法");
        try {

            Thread.sleep(10000L);
            System.out.println("2、线程休眠10s完成");


        } catch (InterruptedException e) {
            System.out.println("3、线程休眠被打断");
            return;
        }
        System.out.println("4、run()方法正常结束");
    }


    public static void main(String[] args) {
        MyThread myThread = new MyThread();
        Thread t = new Thread(myThread, "我的线程");
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("线程t2");
            }
        });
        Thread t3 = new Thread(()-> System.out.println("线程t3"));
        t.start();
        t2.start();
        t3.start();


        try {
            Thread.sleep(2000L);
            System.out.println("线程休眠2s完成");
        } catch (InterruptedException e) {
            System.out.println("main中线程休眠被打断");

        }
        t.interrupt();

    }






}
