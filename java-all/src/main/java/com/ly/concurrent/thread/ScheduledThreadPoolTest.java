package com.ly.concurrent.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/12 16:33
 */
public class ScheduledThreadPoolTest {

    private static final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, Executors.defaultThreadFactory());

    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static void main(String[] args) {

        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (haveMsgAtCurrentTime()) {
                    System.out.println(df.format(new Date()));
                    System.out.println("大家注意了，我要发消息了");
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

    }

    private static boolean haveMsgAtCurrentTime() {
        return true;
    }


}
