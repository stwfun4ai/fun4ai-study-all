package com.ly.concurrent.thread;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @Description 创建线程池参数 及jdk Executors提供四种常见线程池
 * @Created by Administrator
 * @Date 2020/10/8 21:16
 */
public class MyThreadPool {
    public static void main(String[] args) {
        /**
         *  ===前5个参数必需===
         *  corePoolSize：线程池中核心线程数的最大值
         *  maximumPoolSize：该线程池中线程总数最大值
         *  keepAliveTime：表示非核心线程的存活时间。
         *  timeUnit：表示keepAliveTime的单位。
         *  workQueue：用于缓存任务的阻塞队列 常用：
         *             LinkedBlockingQueue 链式阻塞队列，底层数据结构是链表，默认大小是Integer.MAX_VALUE，也可以指定大小。
         *             ArrayBlockingQueue 数组阻塞队列，底层数据结构是数组，需要指定队列的大小。
         *             SynchronousQueue  同步队列，内部容量为0，每个put操作必须等待一个take操作，反之亦然。
         *             DelayQueue 延迟队列，该队列中的元素只有当其指定的延迟时间到了，才能够从队列中获取到该元素 。
         *  threadFactory：指定创建线程的工厂
         *  handler：表示当workQueue已满，且池中的线程数达到maximumPoolSize时，线程池拒绝添加新任务时采取的策略。
         *            ThreadPoolExecutor.AbortPolicy：默认拒绝处理策略，丢弃任务并抛出RejectedExecutionException异常。
         *            ThreadPoolExecutor.DiscardPolicy：丢弃新来的任务，但是不抛出异常。
         *            ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列头部（最旧的）的任务，然后重新尝试执行程序（如果再次失败，重复此过程）。
         *            ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务。
         */
        ExecutorService executor = new ThreadPoolExecutor(
                3,
                5,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        for (int i = 0; i < 9; i++) {
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + "====>执行任务");
                try {
                    TimeUnit.SECONDS.sleep(5L);
                    //Thread.currentThread().sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();

    }



    // Executors四种常见线程池
    // ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();

    /**
     *  当需要执行很多短时间的任务时，CacheThreadPool的线程复用率比较高， 会显著的提高性能。而且线程60s后会回收，意味着即使没有任务进来，CacheThreadPool并不会占用很多资源。
     *  运行流程：
     * 1、提交任务进线程池。
     * 2、因为corePoolSize为0的关系，不创建核心线程，线程池最大为Integer.MAX_VALUE。
     * 3、尝试将任务添加到SynchronousQueue队列。
     * 4、如果SynchronousQueue入列成功，等待被当前运行的线程空闲后拉取执行。如果当前没有空闲线程，那么就创建一个非核心线程，然后从SynchronousQueue拉取任务并在当前线程执行。
     * 5、如果SynchronousQueue已有任务在等待，入列操作将会阻塞。
     * @return
     */
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    /**
     *  只能创建核心线程。无界队列LinkedBlockingQueue的默认大小是Integer.MAX_VALUE
     *  没有任务的情况下， FixedThreadPool占用资源更多。
     * @param nThreads
     * @return
     */
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(
                nThreads,
                nThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    /**
     *  有且仅有一个核心线程（ corePoolSize == maximumPoolSize=1），使用了LinkedBlockingQueue（容量很大），
     *  所以，不会创建非核心线程。所有任务按照先来先执行的顺序执行。如果这个唯一的线程不空闲，那么新来的任务会存储在任务队列里等待执行。
     * @return
     */
    public static ExecutorService newSingleThreadExecutor() {
        return
                //new FinalizableDelegatedExecutorService
                (new ThreadPoolExecutor(
                        1,
                        1,
                        0L,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>()));
    }

    /**
     * 创建一个定长线程池，支持定时及周期性任务执行。
     * @param corePoolSize
     * @return
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        //return new ScheduledThreadPoolExecutor(corePoolSize);
        //return new ThreadPoolExecutor(
        //        corePoolSize,
        //        Integer.MAX_VALUE,
        //        0,
        //        NANOSECONDS,
        //        new DelayedWorkQueue());
        return null;

    }


}
