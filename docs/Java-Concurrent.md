# 工具类

## Semaphore



## Exchanger



## CountDownLatch



## CyclicBarrier



## Phaser



# Thread

## ThreadLocal

ThreadLocal提供了**线程局部变量**，一个线程局部变量在多个线程中，分别有独立的值（副本）。

### 实现原理：

- 每个Thread中都存储着一个成员变量ThreadLocalMap
- ThreadLocal本身不存储数据，像是一个工具类，基于ThreadLocal去操作ThreadLocalMap
- ThreadLocalMap本身就是基于Enrty[]实现的，因为一个线程可以绑定多个ThreadLocal，这样依赖，可能需要存储多个数据，所有采用Entry[]的形式实现。
- 每一个线程都有自己独立的ThreadLocalMap，再基于ThreadLocal对象本身作为key，对value进行存取。
- ThreadLocalMap的key是一个弱引用，即在GC时必须被回收。这里是为了防止key内存泄露。（ThreadLocal对象失去引用后若key是强引用，会导致ThreadLocal对象无法被回收。）

### 内存泄露

- 如果ThreadLocal引用丢失，key因为弱引用会被GC回收掉，如果同时线程还没有被回收，就会导致内存泄露，内存中的value无法被回收，同时也无法被获取到。
- 只需要在使用ThreadLocal对象完毕后，及时手动调用`remove()`方法，移除Entry即可。



```java
// Thread类里的变量：
ThreadLocal.ThreadLocalMap threadLocals = null;

static class ThreadLocalMap {
        //ThreadLocalMap真正存数据的是Entry，且Entry的key使用的是弱引用(WeakReferences)
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }


            // ....省略
        }
```

























