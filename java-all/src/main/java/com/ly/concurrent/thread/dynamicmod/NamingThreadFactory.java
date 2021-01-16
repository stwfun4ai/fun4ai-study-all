package com.ly.concurrent.thread.dynamicmod;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 自定义线程工厂
 * 1.可以设置有意见的线程名，这样方便我们开发调试，问题日志查找及定位。
 * 2.可以设置守护线程。
 * 3.设置线程优先级
 * 4.处理未捕获的异常
 * @Created by Administrator
 * @Date 2020/10/13 16:19
 */
public class NamingThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;


    public NamingThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        //守护线程
        if (t.isDaemon())
            t.setDaemon(false);
        //线程优先级
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        //处理未捕捉的异常
        //t.setUncaughtExceptionHandler((t1, e) -> System.out.println("do something to handle uncaughtException"));
        return t;
    }

}
