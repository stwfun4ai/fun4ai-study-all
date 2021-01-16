package com.ly.concurrent.forkjoin;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/12 16:05
 */
public class ComputeFibonacciForeach {
    // 通过循环来计算，复杂度为O(n)
    private static int computeFibonacci(int n) {
        // 假设n >= 0
        if (n <= 1) {
            return n;
        } else {
            int first = 1;
            int second = 1;
            int third = 0;
            for (int i = 3; i <= n; i ++) {
                // 第三个数是前两个数之和
                third = first + second;
                // 前两个数右移
                first = second;
                second = third;
            }
            return third;
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        int result = computeFibonacci(40);
        long end = System.currentTimeMillis();
        System.out.println("计算结果:" + result);
        System.out.println(String.format("耗时：%d millis",  end -start));
    }
}
