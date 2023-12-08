package com.ly.concurrent.thread.method;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description 交替打印1A2B3C4D5E6F7G
 * Lock Condition实现
 * @Created by Administrator
 * @Date 2020/10/15 17:08
 */
public class LockConditionTest {
    public static void main(String[] args) {
        char[] numC = "1234567".toCharArray();
        char[] charC = "ABCDEFG".toCharArray();

        //ReentrantLock的实现是一种自旋锁，通过循环调用CAS操作来实现加锁。
        // 它的性能比较好也是因为避免了使线程进入内核态的阻塞状态。想尽办法避免线程进入内核的阻塞状态是我们去分析和理解锁设计的关键钥匙。
        Lock lock = new ReentrantLock();

        //可以有多个condition 即多个等待队列实现分组唤醒需要唤醒的线程们，而不是像synchronized要么随机唤醒一个线程要么唤醒全部线程。
        Condition condition1 = lock.newCondition(); //队列 线程t1...
        Condition condition2 = lock.newCondition(); //队列 线程t2...


        new Thread(() -> {
            lock.lock();
            try {
                for (char num : numC) {
                    System.out.print(num);
                    condition2.signal();
                    condition1.await();//为当前线程t1产生一个节点并加入等待队列condition1
                }
                condition2.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t1").start();
        new Thread(() -> {
            lock.lock();
            try {
                for (char c : charC) {
                    System.out.print(c);
                    condition1.signal();
                    condition2.await();
                }
                condition1.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t2").start();


    }
}
