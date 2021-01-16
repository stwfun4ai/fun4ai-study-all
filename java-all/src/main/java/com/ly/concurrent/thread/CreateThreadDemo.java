package com.ly.concurrent.thread;

import java.util.concurrent.*;

/**
 * @Description 创建线程方式
 * @Created by Administrator
 * @Date 2020/10/11 23:09
 */
public class CreateThreadDemo {

    public static void main(String[] args) throws Exception {
        //1、继承Thread
        Thread myThread = new MyThread();
        myThread.start();

        //2、实现Runnable接口
        new Thread(new MyThread1()).start();
        // Java 8 函数式编程，可以省略MyThread1类
        new Thread(() -> System.out.println("Java 8 匿名内部类")).start();

        //建个线程池
        ExecutorService executor = Executors.newCachedThreadPool();
        //3、实现Callable接口    有返回值，而且支持泛型。
        Task task = new Task();
        Future<Integer> future = executor.submit(task);
        // 注意调用get方法会阻塞当前线程，直到得到结果。
        // 所以实际编码中建议使用可以设置超时时间的重载get方法。
        System.out.println(future.get());

        //4、使用FutureTask类
        FutureTask<Integer> futureTask = new FutureTask<>(new Task());
        executor.submit(futureTask);    //无返回值
        System.out.println(futureTask.get());

        //关闭线程池
        executor.shutdown();

    }

    public static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("MyThread extends Thread.");
        }
    }

    public static class MyThread1 implements Runnable {

        @Override
        public void run() {
            System.out.println("MyThread implements Runnable.");
        }
    }

    public static class Task implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            // 模拟计算需要一秒
            Thread.sleep(1000);
            return 2;
        }
    }


}
