package com.ly.concurrent.forkjoin;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/12 16:03
 */
public class plainRecursion {

    public static int testPlainRecursion(int n) {
        if (n == 1 || n == 2) {
            return 1;
        } else {
            return testPlainRecursion(n - 1) + testPlainRecursion(n - 2);
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        int result = testPlainRecursion(40);
        long end = System.currentTimeMillis();
        System.out.println("计算结果:" + result);
        System.out.println(String.format("耗时：%d millis", end - start));
    }

}
