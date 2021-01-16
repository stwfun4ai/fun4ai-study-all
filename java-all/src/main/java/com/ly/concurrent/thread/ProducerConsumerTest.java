package com.ly.concurrent.thread;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @Description 生产者-消费者模型 测试ArrayBlockingQueue阻塞队列
 * @Created by Administrator
 * @Date 2020/10/11 22:51
 */
public class ProducerConsumerTest {

    private int queueSize = 10;
    private ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(queueSize);

    public static void main(String[] args) {
        ProducerConsumerTest test = new ProducerConsumerTest();
        Consumer consumer = test.new Consumer();
        Producer producer = test.new Producer();

        producer.start();
        consumer.start();

    }


    class Consumer extends Thread {
        @Override
        public void run() {
            comsumer();
        }

        private void comsumer() {
            while (true) {
                try {
                    queue.take();
                    System.out.println("从队列取走一个元素，队列剩余" + queue.size() + "个元素");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Producer extends Thread {
        @Override
        public void run() {
            producer();
        }

        private void producer() {
            while (true) {
                try {
                    queue.put(1);
                    System.out.println("向队列取中插入一个元素，队列剩余空间：" + (queueSize - queue.size()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
