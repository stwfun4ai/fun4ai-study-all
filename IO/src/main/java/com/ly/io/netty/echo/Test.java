package com.ly.io.netty.echo;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/9 0009 0:19
 */
public class Test {
    public static void main(String[] args) {
        int val = 4;
        // test isPowerOfTwo ??  0100 & 1100 = 0100
        System.out.println((val & -val) == val);
    }

}
