package com.ly.concurrent.thread.dynamicmod;

import java.util.concurrent.*;

/**
 * @Description 动态修改线程池参数
 * @Created by Administrator
 * @Date 2020/10/13 16:03
 */
public class ThreadPoolDynamicModifyExecutorDemo {
    public static void main(String[] args) throws InterruptedException {
        dynamicModifyExecutor();
    }

    /**
     * 先提交任务给线程池，并修改线程池参数
     */
    private static void dynamicModifyExecutor() throws InterruptedException {
        ThreadPoolExecutor executor = buildThreadPoolExecutor();
        for (int i = 0; i < 15; i++) {
            executor.execute(() -> {
                threadPoolStatus(executor, "==创建任务==");
                try {
                    TimeUnit.SECONDS.sleep(10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        threadPoolStatus(executor, "==改变之前==");
        executor.setCorePoolSize(10);
        executor.setMaximumPoolSize(10);
        executor.prestartAllCoreThreads();
        ResizableCapacityLinkedBlockingQueue<Runnable> queue = (ResizableCapacityLinkedBlockingQueue<Runnable>) executor.getQueue();
        queue.setCapacity(100);
        threadPoolStatus(executor, "==改变之后==");
        Thread.currentThread().join();
    }

    /**
     * 自定义线程池
     *
     * @return
     */
    private static ThreadPoolExecutor buildThreadPoolExecutor() {
        return new ThreadPoolExecutor(
                3,
                5,
                60L,
                TimeUnit.SECONDS,
                new ResizableCapacityLinkedBlockingQueue<>(10),
                new NamingThreadFactory("fun4ai"),
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 打印线程池状态
     *
     * @param executor
     * @param name
     */
    private static void threadPoolStatus(ThreadPoolExecutor executor, String name) {
        ResizableCapacityLinkedBlockingQueue<Runnable> queue = (ResizableCapacityLinkedBlockingQueue<Runnable>) executor.getQueue();
        System.out.println(Thread.currentThread().getName() + name + ":" +
                " 核心线程数" + executor.getCorePoolSize() +
                " 活动线程数" + executor.getActiveCount() +
                " 最大线程数" + executor.getMaximumPoolSize() +
                " 线程池活跃度" + divide(executor.getActiveCount(), executor.getMaximumPoolSize()) +
                " 任务完成度" + executor.getCompletedTaskCount() +
                " 队列大小" + (queue.size() + queue.remainingCapacity()) +
                " 当前排队线程数" + queue.size() +
                " 队列剩余大小" + queue.remainingCapacity() +
                " 队列使用度" + divide(queue.size(), queue.size() + queue.remainingCapacity())
        );
    }

    /**
     * 保留2位小数
     *
     * @param num1
     * @param num2
     * @return
     */
    public static String divide(int num1, int num2) {
        return String.format("%1.2f%%", Double.parseDouble(num1 + "") / Double.parseDouble(num2 + "") * 100);
    }


}
