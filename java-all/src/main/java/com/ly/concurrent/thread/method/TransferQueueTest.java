package com.ly.concurrent.thread.method;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/15 17:54
 */
public class TransferQueueTest {
    public static void main(String[] args) {
        char[] numC = "1234567".toCharArray();
        char[] charC = "ABCDEFG".toCharArray();


        //必须放和取
        TransferQueue<Character> queue = new LinkedTransferQueue<>();

        new Thread(() -> {
            try {
                for (char num : numC) {
                    System.out.print(queue.take());
                    queue.transfer(num);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t1").start();
        new Thread(() -> {
            try {
                for (char c : charC) {
                    queue.transfer(c);
                    System.out.print(queue.take());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t2").start();


    }
}
