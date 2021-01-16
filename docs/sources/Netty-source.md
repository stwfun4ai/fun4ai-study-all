# 源码

```xml
<dependency>
   <groupId>io.netty</groupId>
   <artifactId>netty-example</artifactId>
   <version>4.1.25.Final</version>
</dependency>
```

## Echo 例子

Netty 作为 NIO 的库，自然既可以作为服务端接受请求，也可以作为客户端发起请求。使用 Netty 开发客户端或服务端都是非常简单的，Netty 做了很好的封装，我们通常只要开发一个或多个 **handler** 用来处理我们的自定义逻辑就可以了。

下面，我们来看一个经常会见到的例子，它叫 **Echo**，也就是**回声**，客户端传过去什么值，服务端原样返回什么值。

> 打开 netty-example 的源码，把 `echo` 包下面的代码复制出来玩一玩。

![5](D:\Java\document\study\images\netty-5.png)

> 左边是服务端代码，右边是客户端代码。

上面的代码基本就是模板代码，每次使用都是这一个套路，唯一需要我们开发的部分是 handler(…) 和 childHandler(…) 方法中指定的各个 handler，如 **EchoServerHandler** 和 **EchoClientHandler**，当然 Netty 源码也给我们提供了很多的 handler，比如上面的 LoggingHandler，它就是 Netty 源码中为我们提供的，需要的时候直接拿过来用就好了。

我们先来看一下上述代码中涉及到的一些内容：

- ServerBootstrap 类用于创建服务端实例，Bootstrap 用于创建客户端实例。

- 两个 EventLoopGroup：bossGroup 和 workerGroup，它们涉及的是 Netty 的线程模型，可以看到服务端有两个 group，而客户端只有一个，它们就是 Netty 中的线程池。

- Netty 中的 Channel，没有直接使用 Java 原生的 ServerSocketChannel 和 SocketChannel，而是包装了 NioServerSocketChannel 和 NioSocketChannel 与之对应。

  > 当然，也有对其他协议的支持，如支持 UDP 协议的 NioDatagramChannel，本文只关心 TCP 相关的。

- 左边 handler(…) 方法指定了一个 handler（LoggingHandler），这个 handler 是给服务端收到新的请求的时候处理用的。右边 handler(...) 方法指定了客户端处理请求过程中需要使用的 handlers。

  > 如果你想在 EchoServer 中也指定多个 handler，也可以像右边的 EchoClient 一样使用 ChannelInitializer

- 左边 childHandler(…) 指定了 childHandler，这边的 handlers 是给新创建的连接用的，我们知道服务端 ServerSocketChannel 在 accept 一个连接以后，需要创建 SocketChannel 的实例，childHandler(…) 中设置的 handler 就是用于处理新创建的 SocketChannel 的，而不是用来处理 ServerSocketChannel 实例的。

- pipeline：handler 可以指定多个（需要上面的 ChannelInitializer 类辅助），它们会组成了一个 pipeline，它们其实就类似拦截器的概念，现在只要记住一点，每个 NioSocketChannel 或 NioServerSocketChannel 实例内部都会有一个 pipeline 实例。pipeline 中还涉及到 handler 的执行顺序。

- ChannelFuture：这个涉及到 Netty 中的异步编程，和 JDK 中的 Future 接口类似。

对于不了解 Netty 的读者，也不要有什么压力，我会一一介绍它们，本文主要面向新手，我觉得比较难理解或比较重要的部分，会花比较大的篇幅来介绍清楚。

上面的源码中没有展示消息发送和消息接收的处理，此部分我会在介绍完上面的这些内容以后再进行介绍。

下面，将分块来介绍这些内容。鉴于读者对 NIO 或 Netty 的了解程度可能参差不齐，为了照顾初学者，很多地方需要啰嗦一些，所以希望读者一节一节往下看，对于自己熟悉的内容可以适当看快一些。

## Netty 中的 Channel

这节我们来看看 NioSocketChannel 是怎么和 JDK 底层的 SocketChannel 联系在一起的，它们是一对一的关系。NioServerSocketChannel 和 ServerSocketChannel 同理，也是一对一的关系。

![3](D:\Java\document\study\images\netty-3.png)

在 Bootstrap（客户端） 和 ServerBootstrap（服务端） 的启动过程中都会调用 channel(…) 方法：

![10](D:\Java\document\study\images\netty-10.png)

下面，我们来看 channel(…) 方法的源码：

```java
// AbstractBootstrap
public B channel(Class<? extends C> channelClass) {
    if (channelClass == null) {
        throw new NullPointerException("channelClass");
    }
    return channelFactory(new ReflectiveChannelFactory<C>(channelClass));
}
```

我们可以看到，这个方法只是设置了 channelFactory 为 ReflectiveChannelFactory 的一个实例，然后我们看下这里的 ReflectiveChannelFactory 到底是什么：

![1](D:\Java\document\study\images\netty-1.png)

**newChannel()** 方法是 ChannelFactory 接口中的唯一方法，**工厂模式**大家都很熟悉。我们可以看到，`ReflectiveChannelFactory#newChannel()` 方法中使用了反射调用 Channel 的无参构造方法来创建 Channel，我们只要知道，ChannelFactory 的 newChannel() 方法什么时候会被调用就可以了。

- 对于 NioSocketChannel，由于它充当客户端的功能，它的创建时机在 `connect(…)` 的时候；
- 对于 NioServerSocketChannel 来说，它充当服务端功能，它的创建时机在绑定端口 `bind(…)` 的时候。

接下来，我们来简单追踪下充当客户端的 Bootstrap 中 NioSocketChannel 的创建过程，看看 NioSocketChannel 是怎么和 JDK 中的 SocketChannel 关联在一起的：

```java
// Bootstrap
public ChannelFuture connect(String inetHost, int inetPort) {
    return connect(InetSocketAddress.createUnresolved(inetHost, inetPort));
}
```

然后再往里看，到这个方法：

```java
public ChannelFuture connect(SocketAddress remoteAddress) {
    if (remoteAddress == null) {
        throw new NullPointerException("remoteAddress");
    // validate 只是校验一下各个参数是不是正确设置了
    validate();
    return doResolveAndConnect(remoteAddress, config.localAddress());
}
```

继续：

```java
// 再往里就到这里了
private ChannelFuture doResolveAndConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
    // 我们要说的部分在这里
    final ChannelFuture regFuture = initAndRegister();
    final Channel channel = regFuture.channel();
    ......
}
```

然后，我们看 `initAndRegister()` 方法：

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;
    try {
        // 前面我们说过，这里会进行 Channel 的实例化
        channel = channelFactory.newChannel();
        init(channel);
    } catch (Throwable t) {
        ...
    }
    ...
    return regFuture;
}
```

我们找到了 `channel = channelFactory.newChannel()` 这行代码，根据前面说的，这里会调用相应 Channel 的无参构造方法。

然后我们就可以去看 NioSocketChannel 的构造方法了：

```java
public NioSocketChannel() {
    // SelectorProvider 实例用于创建 JDK 的 SocketChannel 实例
    this(DEFAULT_SELECTOR_PROVIDER);
}

public NioSocketChannel(SelectorProvider provider) {
    // 看这里，newSocket(provider) 方法会创建 JDK 的 SocketChannel
    this(newSocket(provider));
}
```

我们可以看到，在调用 newSocket(provider) 的时候，会创建 JDK NIO 的一个 SocketChannel 实例：

```java
private static SocketChannel newSocket(SelectorProvider provider) {
    try {
        // 创建 SocketChannel 实例
        return provider.openSocketChannel();
    } catch (IOException e) {
        throw new ChannelException("Failed to open a socket.", e);
    }
}
```

NioServerSocketChannel 同理，也非常简单，从 `ServerBootstrap#bind(...)` 方法一路点进去就清楚了。

所以我们知道了，NioSocketChannel 在实例化过程中，会先实例化 JDK 底层的 SocketChannel，NioServerSocketChannel 也一样，会先实例化 ServerSocketChannel 实例：

![18](D:\Java\document\study\images\netty-18.png)

说到这里，我们顺便再继续往里看一下 NioSocketChannel 的构造方法：

```java
public NioSocketChannel(SelectorProvider provider) {
    this(newSocket(provider));
}
```

刚才我们看到这里，newSocket(provider) 创建了底层的 SocketChannel 实例，我们继续往下看构造方法：

```java
public NioSocketChannel(Channel parent, SocketChannel socket) {
    super(parent, socket);
    config = new NioSocketChannelConfig(this, socket.socket());
}
```

上面有两行代码，第二行代码很简单，实例化了内部的 NioSocketChannelConfig 实例，它用于保存 channel 的配置信息，这里没有我们现在需要关心的内容，直接跳过。

第一行调用父类构造器，除了设置属性外，还设置了 SocketChannel 的非阻塞模式：

```java
protected AbstractNioByteChannel(Channel parent, SelectableChannel ch) {
    // 毫无疑问，客户端关心的是 OP_READ 事件，等待读取服务端返回数据
    super(parent, ch, SelectionKey.OP_READ);
}

// 然后是到这里
protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent);
    this.ch = ch;
    // 我们看到这里只是保存了 SelectionKey.OP_READ 这个信息，在后面的时候会用到
    this.readInterestOp = readInterestOp;
    try {
        // ******设置 channel 的非阻塞模式******
        ch.configureBlocking(false);
    } catch (IOException e) {
        ......
    }
}
```

NioServerSocketChannel 的构造方法类似，也设置了非阻塞，然后设置服务端关心的 SelectionKey.OP_ACCEPT 事件：

```java
public NioServerSocketChannel(ServerSocketChannel channel) {
    // 对于服务端来说，关心的是 SelectionKey.OP_ACCEPT 事件，等待客户端连接
    super(null, channel, SelectionKey.OP_ACCEPT);
    config = new NioServerSocketChannelConfig(this, javaChannel().socket());
}
```



**这节关于 Channel 的内容我们先介绍这么多，主要就是==实例化了 JDK 层==的 `SocketChannel `或 `ServerSocketChannel`，然后设置了==非阻塞模式==，我们后面再继续深入下去。**



## Netty 中的异步编程: Future 和 Promise

Netty 中非常多的异步调用，所以在介绍更多 NIO 相关的内容之前，我们来看看它的异步接口是怎么使用的。

前面我们在介绍 Echo 例子的时候，已经用过了 ChannelFuture 这个接口了：

![6](D:\Java\document\study\images\netty-6.png)

争取在看完本节后，读者能搞清楚上面的这几行划线部分是怎么走的。

关于 Future 接口，我想大家应该都很熟悉，用得最多的就是在使用 Java 的线程池 ThreadPoolExecutor 的时候了。在 **submit** 一个任务到线程池中的时候，返回的就是一个 **Future** 实例，通过它来获取提交的任务的执行状态和最终的执行结果，我们最常用它的 `isDone()` 和 `get()` 方法。

下面是 JDK  中的 Future 接口 java.util.concurrent.Future：

```java
public interface Future<V> {
    // 取消该任务
    boolean cancel(boolean mayInterruptIfRunning);
    // 任务是否已取消
    boolean isCancelled();
    // 任务是否已完成
    boolean isDone();
    // 阻塞获取任务执行结果
    V get() throws InterruptedException, ExecutionException;
    // 带超时参数的获取任务执行结果
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
```

Netty 中的 Future 接口（同名）继承了 JDK 中的 Future 接口，然后添加了一些方法：

// io.netty.util.concurrent.Future

```java
public interface Future<V> extends java.util.concurrent.Future<V> {

    // 是否成功
    boolean isSuccess();

    // 是否可取消
    boolean isCancellable();

    // 如果任务执行失败，这个方法返回异常信息
    Throwable cause();

    // 添加 Listener 来进行回调
    Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);
    Future<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners);

    Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);
    Future<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners);

    // 阻塞等待任务结束，如果任务失败，将“导致失败的异常”重新抛出来
    Future<V> sync() throws InterruptedException;
    // 不响应中断的 sync()，这个大家应该都很熟了
    Future<V> syncUninterruptibly();

    // 阻塞等待任务结束，和 sync() 功能是一样的，不过如果任务失败，它不会抛出执行过程中的异常
    Future<V> await() throws InterruptedException;
    Future<V> awaitUninterruptibly();
    boolean await(long timeout, TimeUnit unit) throws InterruptedException;
    boolean await(long timeoutMillis) throws InterruptedException;
    boolean awaitUninterruptibly(long timeout, TimeUnit unit);
    boolean awaitUninterruptibly(long timeoutMillis);

    // 获取执行结果，不阻塞。我们都知道 java.util.concurrent.Future 中的 get() 是阻塞的
    V getNow();

    // 取消任务执行，如果取消成功，任务会因为 CancellationException 异常而导致失败
    //      也就是 isSuccess()==false，同时上面的 cause() 方法返回 CancellationException 的实例。
    // mayInterruptIfRunning 说的是：是否对正在执行该任务的线程进行中断(这样才能停止该任务的执行)，
    //       似乎 Netty 中 Future 接口的各个实现类，都没有使用这个参数
    @Override
    boolean cancel(boolean mayInterruptIfRunning);
}
```

看完上面的 Netty 的 Future 接口，我们可以发现，它加了 sync() 和 await() 用于阻塞等待，还加了 Listeners，只要任务结束去回调 Listener 们就可以了，那么我们就不一定要主动调用 isDone() 来获取状态，或通过 get() 阻塞方法来获取值。

> 所以它其实有两种使用范式

顺便说下 sync() 和 await() 的区别：sync() 内部会先调用 await() 方法，等 await() 方法返回后，会检查下**这个任务是否失败**，如果失败，重新将导致失败的异常抛出来。也就是说，如果使用 await()，任务抛出异常后，await() 方法会返回，但是不会抛出异常，而 sync() 方法返回的同时会抛出异常。

> 我们也可以看到，Future 接口没有和 IO 操作关联在一起，还是比较*纯净*的接口。

接下来，我们来看 Future 接口的子接口 ChannelFuture，这个接口用得最多，它将和 IO 操作中的 Channel 关联在一起了，用于异步处理 Channel 中的事件。

```java
public interface ChannelFuture extends Future<Void> {

    // ChannelFuture 关联的 Channel
    Channel channel();

    // 覆写以下几个方法，使得它们返回值为 ChannelFuture 类型 
    @Override
    ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener);
    @Override
    ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);
    @Override
    ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener);
    @Override
    ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    ChannelFuture sync() throws InterruptedException;
    @Override
    ChannelFuture syncUninterruptibly();

    @Override
    ChannelFuture await() throws InterruptedException;
    @Override
    ChannelFuture awaitUninterruptibly();

    // 用来标记该 future 是 void 的，
    // 这样就不允许使用 addListener(...), sync(), await() 以及它们的几个重载方法
    boolean isVoid();
}
```

我们看到，ChannelFuture 接口相对于 Future 接口，除了将 channel 关联进来，没有增加什么东西。还有个 isVoid() 方法算是不那么重要的存在吧。其他几个都是方法覆写，为了让返回值类型变为 ChannelFuture，而不是原来的 Future。

这里有点跳，我们来介绍下 Promise 接口，它和 ChannelFuture 接口无关，而是和前面的 Future 接口相关，Promise 这个接口非常重要。

Promise 接口和 ChannelFuture 一样，也继承了 Netty 的 Future 接口，然后加了一些 Promise 的内容：

```java
public interface Promise<V> extends Future<V> {

    // 标记该 future 成功及设置其执行结果，并且会通知所有的 listeners。
    // 如果该操作失败，将抛出异常(失败指的是该 future 已经有了结果了，成功的结果，或者失败的结果)
    Promise<V> setSuccess(V result);

    // 和 setSuccess 方法一样，只不过如果失败，它不抛异常，返回 false
    boolean trySuccess(V result);

    // 标记该 future 失败，及其失败原因。
    // 如果失败，将抛出异常(失败指的是已经有了结果了)
    Promise<V> setFailure(Throwable cause);

    // 标记该 future 失败，及其失败原因。
    // 如果已经有结果，返回 false，不抛出异常
    boolean tryFailure(Throwable cause);

    // 标记该 future 不可以被取消
    boolean setUncancellable();

    // 这里和 ChannelFuture 一样，对这几个方法进行覆写，目的是为了返回 Promise 类型的实例
    @Override
    Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);
    @Override
    Promise<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners);

    @Override
    Promise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);
    @Override
    Promise<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners);

    @Override
    Promise<V> await() throws InterruptedException;
    @Override
    Promise<V> awaitUninterruptibly();

    @Override
    Promise<V> sync() throws InterruptedException;
    @Override
    Promise<V> syncUninterruptibly();
}
```

可能有些读者对 Promise 的概念不是很熟悉，这里简单说两句。

我觉得只要明白一点，Promise 实例内部是一个任务，任务的执行往往是异步的，通常是一个线程池来处理任务。Promise 提供的 setSuccess(V result) 或 setFailure(Throwable t) 将来会被某个执行任务的线程在执行完成以后调用，同时那个线程在调用 setSuccess(result) 或 setFailure(t) 后会回调 listeners 的回调函数（当然，回调的具体内容不一定要由执行任务的线程自己来执行，它可以创建新的线程来执行，也可以将回调任务提交到某个线程池来执行）。而且，一旦 setSuccess(...) 或 setFailure(...) 后，那些 await() 或 sync() 的线程就会从等待中返回。

**所以这里就有两种编程方式，一种是用 await()，等 await() 方法返回后，得到 promise 的执行结果，然后处理它；另一种就是提供 Listener 实例，我们不太关心任务什么时候会执行完，只要它执行完了以后会去执行 listener 中的处理方法就行。**

接下来，我们再来看下 **ChannelPromise**，它继承了前面介绍的 ChannelFuture 和 Promise 接口。

![4](D:\Java\document\study\images\netty-4.png)

ChannelPromise 接口在 Netty 中使用得比较多，因为它综合了 ChannelFuture 和 Promise 两个接口：

```java
/**
 * Special {@link ChannelFuture} which is writable.
 */
public interface ChannelPromise extends ChannelFuture, Promise<Void> {

    // 覆写 ChannelFuture 中的 channel() 方法，其实这个方法一点没变
    @Override
    Channel channel();

    // 下面几个方法是覆写 Promise 中的接口，为了返回值类型是 ChannelPromise
    @Override
    ChannelPromise setSuccess(Void result);
    ChannelPromise setSuccess();
    boolean trySuccess();
    @Override
    ChannelPromise setFailure(Throwable cause);

    // 到这里大家应该都熟悉了，下面几个方法的覆写也是为了得到 ChannelPromise 类型的实例
    @Override
    ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener);
    @Override
    ChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);
    @Override
    ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener);
    @Override
    ChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    ChannelPromise sync() throws InterruptedException;
    @Override
    ChannelPromise syncUninterruptibly();
    @Override
    ChannelPromise await() throws InterruptedException;
    @Override
    ChannelPromise awaitUninterruptibly();

    /**
     * Returns a new {@link ChannelPromise} if {@link #isVoid()} returns {@code true} otherwise itself.
     */
    // 我们忽略这个方法吧。
    ChannelPromise unvoid();
}
```

我们可以看到，它综合了 ChannelFuture 和 Promise 中的方法，只不过通过覆写将返回值都变为 ChannelPromise 了而已，**没有增加什么新的功能**。

小结一下，我们上面介绍了几个接口，Future 以及它的子接口 ChannelFuture 和 Promise，然后是 ChannelPromise 接口同时继承了 ChannelFuture 和 Promise。

我把这几个接口的主要方法列在一起，这样大家看得清晰些：

![4](D:\Java\document\study\images\netty-7.png)

接下来，我们需要来一个实现类，这样才能比较直观地看出它们是怎么使用的，因为上面的这些都是接口定义，具体还得看实现类是怎么工作的。

下面，我们来介绍下 **DefaultPromise** 这个实现类，这个类很常用，它的源码也不短，我们先介绍几个关键的内容，然后介绍一个示例使用。

首先，我们看下它有哪些属性：

```java
public class DefaultPromise<V> extends AbstractFuture<V> implements Promise<V> {
      // 保存执行结果
    private volatile Object result;
    // 执行任务的线程池，promise 持有 executor 的引用，这个其实有点奇怪了
    // 因为“任务”其实没必要知道自己在哪里被执行的
    private final EventExecutor executor;
      // 监听者，回调函数，任务结束后（正常或异常结束）执行
    private Object listeners;

    // 等待这个 promise 的线程数(调用sync()/await()进行等待的线程数量)
    private short waiters;

    // 是否正在唤醒等待线程，用于防止重复执行唤醒，不然会重复执行 listeners 的回调方法
    private boolean notifyingListeners;
    ......
}
```

> 可以看出，此类实现了 Promise，但是没有实现 ChannelFuture，所以它和 Channel 联系不起来。
>
> 别急，我们后面会碰到另一个类 DefaultChannelPromise 的使用，这个类是综合了 ChannelFuture 和 Promise 的，但是它的实现其实大部分都是继承自这里的 DefaultPromise 类的。

说完上面的属性以后，大家可以看下 `setSuccess(V result)` 、`trySuccess(V result)` 和 `setFailure(Throwable cause)` 、 `tryFailure(Throwable cause)` 这几个方法：

![8](D:\Java\document\study\images\netty-8.png)

> 看出 setSuccess(result) 和 trySuccess(result) 的区别了吗？

上面几个方法都非常简单，先设置好值，然后执行监听者们的回调方法。notifyListeners() 方法感兴趣的读者也可以看一看，不过它还涉及到 Netty 线程池的一些内容，我们还没有介绍到线程池，这里就不展开了。上面的代码，在 setSuccess0 或 setFailure0 方法中都会唤醒阻塞在 sync() 或 await() 的线程

另外，就是可以看下 sync() 和 await() 的区别，其他的我觉得随便看看就好了。

```java
@Override
public Promise<V> sync() throws InterruptedException {
    await();
    // 如果任务是失败的，重新抛出相应的异常
    rethrowIfFailed();
    return this;
}
```

接下来，我们来写个实例代码吧：

```java
    public static void main(String[] args) {

        // 构造线程池
        EventExecutor executor = new DefaultEventExecutor();

        // 创建 DefaultPromise 实例
        Promise promise = new DefaultPromise(executor);

        // 下面给这个 promise 添加两个 listener
        promise.addListener(new GenericFutureListener<Future<Integer>>() {
            @Override
            public void operationComplete(Future future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("任务结束，结果：" + future.get());
                } else {
                    System.out.println("任务失败，异常：" + future.cause());
                }
            }
        }).addListener(new GenericFutureListener<Future<Integer>>() {
            @Override
            public void operationComplete(Future future) throws Exception {
                System.out.println("任务结束，balabala...");
            }
        });

        // 提交任务到线程池，五秒后执行结束，设置执行 promise 的结果
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                // 设置 promise 的结果
                // promise.setFailure(new RuntimeException());
                promise.setSuccess(123456);
            }
        });

        // main 线程阻塞等待执行结果
        try {
            promise.sync();
        } catch (InterruptedException e) {
        }
    }
```

运行代码，两个 listener 将在 5 秒后将输出：

```
任务结束，结果：123456
任务结束，balabala...
```

> 读者这里可以试一下 sync() 和 await() 的区别，在任务中调用 promise.setFailure(new RuntimeException()) 试试看。

上面的代码中，大家可能会对线程池 executor 和 promise 之间的关系感到有点迷惑。读者应该也要清楚，具体的任务不一定就要在这个 executor 中被执行。任务结束以后，需要调用 promise.setSuccess(result) 作为通知。

通常来说，promise 代表的 future 是不需要和线程池搅在一起的，future 只关心任务是否结束以及任务的执行结果，至于是哪个线程或哪个线程池执行的任务，future 其实是不关心的。

不过 Netty 毕竟不是要创建一个通用的线程池实现，而是和它要处理的 IO 息息相关的，所以我们只不过要理解它就好了。

这节就说这么多吧，我们回过头来再看一下这张图，看看大家是不是看懂了这节内容：

![6](D:\Java\document\study\images\netty-6.png)

我们就说说上图左边的部分吧，虽然我们还不知道 bind() 操作中具体会做什么工作，但是我们应该可以猜出一二。

显然，main 线程调用 b.bind(port) 这个方法会返回一个 ChannelFuture，bind() 是一个异步方法，当某个执行线程执行了真正的绑定操作后，那个执行线程一定会标记这个 future 为成功（我们假定 bind 会成功），然后这里的 sync() 方法（main 线程）就会返回了。

> 如果 bind(port) 失败，我们知道，sync() 方法会将异常抛出来，然后就会执行到 finally 块了。 

一旦绑定端口 bind 成功，进入下面一行，f.channel() 方法会返回该 future 关联的 channel。

channel.closeFuture() 也会返回一个 ChannelFuture，然后调用了 sync() 方法，这个 sync() 方法返回的条件是：**有其他的线程关闭了 NioServerSocketChannel**，往往是因为需要停掉服务了，然后那个线程会设置 future 的状态（ setSuccess(result) 或 setFailure(cause) ），这个 sync() 方法才会返回。

这节就到这里，希望大家对 Netty 中的异步编程有些了解，后续碰到源码的时候能知道是怎么使用的了。

## ChannelPipeline，和 Inbound、Outbound

我想很多读者应该或多或少都有 Netty 中 pipeline 的概念。前面我们说了，使用 Netty 的时候，我们通常就只要写一些自定义的 handler 就可以了，我们定义的这些 handler 会组成一个 pipeline，用于处理 IO 事件，这个和我们平时接触的 Filter 或 Interceptor 表达的差不多是一个意思。

每个 Channel 内部都有一个 pipeline，pipeline 由多个 handler 组成，handler 之间的顺序是很重要的，因为 IO 事件将按照顺序顺次经过 pipeline 上的 handler，这样每个 handler 可以专注于做一点点小事，由多个 handler 组合来完成一些复杂的逻辑。

![11](D:\Java\document\study\images\netty-11.png)

从图中，我们知道这是一个双向链表。

首先，我们看两个重要的概念：**Inbound** 和 **Outbound**。在 Netty 中，IO 事件被分为 Inbound 事件和 Outbound 事件。

**Outbound** 的 **out** 指的是 **出去**，有哪些 IO 事件属于此类呢？比如 connect、write、flush 这些 IO 操作是往外部方向进行的，它们就属于 Outbound 事件。

其他的，诸如 accept、read 这种就属于 Inbound 事件。

> 比如客户端在发起请求的时候，需要 1️⃣connect 到服务器，然后 2️⃣write 数据传到服务器，再然后 3️⃣read 服务器返回的数据，前面的 connect 和 write 就是 **out** 事件，后面的 read 就是 **in** 事件。

比如很多初学者看不懂下面的这段代码，这段代码用于服务端的 childHandler 中：

```java
1. pipeline.addLast(new StringDecoder());
2. pipeline.addLast(new StringEncoder());
3. pipeline.addLast(new BizHandler());
```

初学者肯定都纳闷，以为这个顺序写错了，应该是先 decode 客户端过来的数据，然后用 BizHandler 处理业务逻辑，最后再 encode 数据然后返回给客户端，所以添加的顺序应该是 **1 -> 3 -> 2** 才对。

其实这里的三个 handler 是分组的，分为 Inbound（1 和 3） 和 Outbound（2）：

```java
1. pipeline.addLast(new StringDecoder());
2. pipeline.addLast(new StringEncoder());
3. pipeline.addLast(new BizHandler());
```

- 客户端连接进来的时候，读取（read）客户端请求数据的操作是 Inbound 的，所以会先使用 1，然后是 3 对处理进行处理；
- 处理完数据后，返回给客户端数据的 write 操作是 Outbound 的，此时使用的是 2。

所以虽然添加顺序有点怪，但是执行顺序其实是按照 1 -> 3 -> 2 进行的。

> 如果我们在上面的基础上，加上下面的第四行，这是一个 OutboundHandler：
>
> ```java
> 4. pipeline.addLast(new OutboundHandlerA());
> ```
>
> 那么执行顺序是不是就是 1 -> 3 -> 2 -> 4 呢？答案是：不是的。
>
> 对于 Inbound 操作，按照添加顺序执行每个 Inbound 类型的 handler；而对于 Outbound 操作，是反着来的，从后往前，顺次执行 Outbound 类型的 handler。
>
> 所以，上面的顺序应该是先 1 后 3，它们是 Inbound 的，然后是 4，最后才是 2，它们两个是 Outbound 的。说实话，这种组织方式对新手应该很是头疼。
>
> 那我们在开发的时候怎么写呢？其实也很简单，从最外层开始写，一步步写到业务处理层，把 Inbound 和 Outbound 混写在一起。比如 encode 和 decode 是属于最外层的处理逻辑，先写它们。假设 decode 以后是字符串，那再进来一层应该可以写进来和出去的日志。再进来一层可以写 字符串 <=> 对象 的相互转换。然后就应该写业务层了。

到这里，我想大家应该都知道 Inbound 和 Outbound 了吧？下面我们来介绍它们的接口使用。

![9](D:\Java\document\study\images\netty-9.png)

定义处理 Inbound 事件的 handler 需要实现 ChannelInboundHandler，定义处理 Outbound 事件的 handler 需要实现 ChannelOutboundHandler。最下面的三个类，是 Netty 提供的适配器，特别的，如果我们希望定义一个 handler 能同时处理 Inbound 和 Outbound 事件，可以通过继承中间的 **ChannelDuplexHandler** 的方式，比如 **LoggingHandler** 这种既可以用来处理 Inbound 也可以用来处理 Outbound 事件的 handler。

有了 Inbound 和 Outbound 的概念以后，我们来开始介绍 Pipeline 的源码。

我们说过，一个 Channel 关联一个 pipeline，NioSocketChannel 和 NioServerSocketChannel 在执行构造方法的时候，都会走到它们的父类 AbstractChannel 的构造方法中：

```java
protected AbstractChannel(Channel parent) {
    this.parent = parent;
    // 给每个 channel 分配一个唯一 id
    id = newId();
    // 每个 channel 内部需要一个 Unsafe 的实例
    unsafe = newUnsafe();
    // 每个 channel 内部都会创建一个 pipeline
    pipeline = newChannelPipeline();
}
```

上面的三行代码中，id 比较不重要，Netty 中的 Unsafe 实例其实挺重要的，这里简单介绍一下。

在 JDK 的源码中，sun.misc.Unsafe 类提供了一些底层操作的能力，它设计出来是给 JDK 中的源码使用的，比如 AQS、ConcurrentHashMap 等，我们在之前的并发包的源码分析中也看到了很多它们使用 Unsafe 的场景，这个 Unsafe 类不是给我们的代码使用的，是给 JDK 源码使用的（需要的话，我们也是可以获取它的实例的）。

> Unsafe 类的构造方法是 private 的，但是它提供了 getUnsafe() 这个静态方法：
>
> ```java
> Unsafe unsafe = Unsafe.getUnsafe();
> ```
>
> 大家可以试一下，上面这行代码编译没有问题，但是执行的时候会抛 `java.lang.SecurityException` 异常，因为它就不是给我们的代码用的。
>
> 但是如果你就是想获取 Unsafe 的实例，可以通过下面这个代码获取到:
>
> ```java
> Field f = Unsafe.class.getDeclaredField("theUnsafe");
> f.setAccessible(true);
> Unsafe unsafe = (Unsafe) f.get(null);
> ```

Netty 中的 Unsafe 也是同样的意思，它封装了 Netty 中会使用到的 JDK 提供的 NIO 接口，比如将 channel 注册到 selector 上，比如 bind 操作，比如 connect 操作等，**这些操作都是稍微偏底层一些**。Netty 同样也是不希望我们的业务代码使用 Unsafe 的实例，它是提供给 Netty 中的源码使用的。

> 不过，对于我们源码分析来说，我们还是会有很多时候需要分析 Unsafe 中的源码的

关于 Unsafe，我们后面用到了再说，这里只要知道，它封装了大部分需要访问 JDK 的 NIO 接口的操作就好了。这里我们继续将焦点放在实例化 pipeline 上：

```java
protected DefaultChannelPipeline newChannelPipeline() {
    return new DefaultChannelPipeline(this);
}
```

这里开始调用 DefaultChannelPipeline 的构造方法，并把当前 channel 的引用传入：

```java
protected DefaultChannelPipeline(Channel channel) {
    this.channel = ObjectUtil.checkNotNull(channel, "channel");
    succeededFuture = new SucceededChannelFuture(channel, null);
    voidPromise =  new VoidChannelPromise(channel, true);

    tail = new TailContext(this);
    head = new HeadContext(this);

    head.next = tail;
    tail.prev = head;
}
```

这里实例化了 tail 和 head 这两个 handler。tail 实现了 ChannelInboundHandler 接口，而 head 实现了 ChannelOutboundHandler 和 ChannelInboundHandler 两个接口，并且最后两行代码将 tail 和 head 连接起来:

![12](D:\Java\document\study\images\netty-12.png)

> 注意，在不同的版本中，源码也略有差异，head 不一定是 in + out，大家知道这点就好了。
>
> 还有，从上面的 head 和 tail 我们也可以看到，其实 pipeline 中的每个元素是 **ChannelHandlerContext** 的实例，而不是 ChannelHandler 的实例，context 包装了一下 handler，但是，后面我们都会用 handler 来描述一个 pipeline 上的节点，而不是使用 context，希望读者知道这一点。

这里只是构造了 pipeline，并且添加了两个固定的 handler 到其中（head + tail），还不涉及到自定义的 handler 代码执行。我们回过头来看下面这段代码：

![13](D:\Java\document\study\images\netty-13.png)

> 我们说过 childHandler 中指定的 handler 不是给 NioServerSocketChannel 使用的，是给 NioSocketChannel 使用的，所以这里我们不看它。

这里调用 handler(…) 方法指定了一个 LoggingHandler 的实例，然后我们再进去下面的 bind(…) 方法中看看这个 LoggingHandler 实例是怎么进入到我们之前构造的 pipeline 内的。

顺着 bind() 一直往前走，bind() -> doBind() -> initAndRegister()：

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;
    try {
        // 1. 构造 channel 实例，同时会构造 pipeline 实例，
        // 现在 pipeline 中有 head 和 tail 两个 handler 了
        channel = channelFactory.newChannel();
        // 2. 看这里
        init(channel);
    } catch (Throwable t) {
    ......
}
```

上面的两行代码，第一行实现了构造 channel 和 channel 内部的 pipeline，我们来看第二行 init 代码：

// ServerBootstrap：

```java
@Override
void init(Channel channel) throws Exception {
    ......
    // 拿到刚刚创建的 channel 内部的 pipeline 实例
    ChannelPipeline p = channel.pipeline();
    ...
    // 开始往 pipeline 中添加一个 handler，这个 handler 是 ChannelInitializer 的实例
    p.addLast(new ChannelInitializer<Channel>() {

        // 我们以后会看到，下面这个 initChannel 方法何时会被调用
        @Override
        public void initChannel(final Channel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();
            // 这个方法返回我们最开始指定的 LoggingHandler 实例
            ChannelHandler handler = config.handler();
            if (handler != null) {
                // 添加 LoggingHandler
                pipeline.addLast(handler);
            }

            // 先不用管这里的 eventLoop
            ch.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    // 添加一个 handler 到 pipeline 中：ServerBootstrapAcceptor
                    // 从名字可以看到，这个 handler 的目的是用于接收客户端请求
                    pipeline.addLast(new ServerBootstrapAcceptor(
                            ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
                }
            });
        }
    });
}
```

这里涉及到 pipeline 中的辅助类 ChannelInitializer，我们看到，它本身是一个 handler（Inbound 类型），但是它的作用和普通 handler 有点不一样，它纯碎是用来辅助将其他的 handler 加入到 pipeline 中的。

大家可以稍微看一下 ChannelInitializer 的 initChannel 方法，有个简单的认识就好，此时的 pipeline 应该是这样的：

![14](D:\Java\document\study\images\netty-14.png)

ChannelInitializer 的 initChannel(channel) 方法被调用的时候，会往 pipeline 中添加我们最开始指定的 **LoggingHandler** 和添加一个 **ServerBootstrapAcceptor**。但是我们现在还不知道这个 initChannel 方法何时会被调用。

上面我们说的是作为服务端的 NioServerSocketChannel 的 pipeline，NioSocketChannel 也是差不多的，我们可以看一下 Bootstrap 类的 init(channel) 方法：

```java
void init(Channel channel) throws Exception {
    ChannelPipeline p = channel.pipeline();
    p.addLast(config.handler());
    ...
}
```

![23](D:\Java\document\study\images\netty-23.png)

它和服务端 ServerBootstrap 要添加 ServerBootstrapAcceptor 不一样，它只需要将 EchoClient 类中的 ChannelInitializer 实例加进来就可以了，它的 ChannelInitializer 中添加了两个 handler，LoggingHandler 和 EchoClientHandler：

![16](D:\Java\document\study\images\netty-16.png)

很显然，我们需要的是像 LoggingHandler 和 EchoClientHandler 这样的 handler，但是，它们现在还不在 pipeline 中，那么它们什么时候会真正进入到 pipeline 中呢？以后我们再揭晓。

还有，为什么 Server 端我们指定的是一个 handler 实例，而 Client 指定的是一个 ChannelInitializer 实例？其实它们是可以随意搭配使用的，你甚至可以在 ChannelInitializer 实例中添加 ChannelInitializer 的实例。

非常抱歉，这里又要断了，下面要先介绍线程池了，大家要记住 pipeline 现在的样子，**head + channelInitializer + tail**。

本节没有介绍 handler 的向后传播，就是一个 handler 处理完了以后，怎么传递给下一个 handler 来处理？比如我们熟悉的 JavaEE 中的 Filter 是采用在一个 Filter 实例中调用 chain.doFilter(request, response) 来传递给下一个 Filter 这种方式的。

我们用下面这张图结束本节。下图展示了传播的方法，但我其实是更想让大家看一下，哪些事件是 Inbound 类型的，哪些是 Outbound 类型的：

![19](D:\Java\document\study\images\netty-19.png)

Outbound 类型的几个事件大家应该比较好认，注意 bind 也是 Outbound 类型的。

## Netty 中的线程池 EventLoopGroup

接下来，我们来分析 Netty 中的线程池。Netty 中的线程池比较不好理解，因为它的类比较多，而且它们之间的关系错综复杂。看下图，感受下 NioEventLoop 类和 NioEventLoopGroup 类的继承结构：

![2](D:\Java\document\study\images\netty-2.png)

这张图我按照继承关系整理而来，大家仔细看一下就会发现，涉及到的类确实挺多的。本节来给大家理理清楚这部分内容。

首先，我们说的 Netty 的线程池，指的就是 **NioEventLoopGroup** 的实例；线程池中的单个线程，指的是右边 **NioEventLoop** 的实例。

回顾下我们第一节介绍的 Echo 例子，客户端和服务端的启动代码中，最开始我们总是先实例化 NioEventLoopGroup：

```java
// EchoClient 代码最开始：
EventLoopGroup group = new NioEventLoopGroup();

// EchoServer 代码最开始：
EventLoopGroup bossGroup = new NioEventLoopGroup(1);
EventLoopGroup workerGroup = new NioEventLoopGroup();
```

下面，我们就从 NioEventLoopGroup 的源码开始进行分析。

我们打开 NioEventLoopGroup 的源码，可以看到，NioEventLoopGroup 有多个构造方法用于参数设置，最简单地，我们采用无参构造函数，或仅仅设置线程数量就可以了，其他的参数采用默认值。

> 比如上面的代码中，我们只在实例化 bossGroup 的时候指定了参数，代表该线程池需要一个线程。

```java
public NioEventLoopGroup() {
    this(0);
}
public NioEventLoopGroup(int nThreads) {
    this(nThreads, (Executor) null);
}

...

// 参数最全的构造方法
public NioEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory,
                         final SelectorProvider selectorProvider,
                         final SelectStrategyFactory selectStrategyFactory,
                         final RejectedExecutionHandler rejectedExecutionHandler) {
    // 调用父类的构造方法
    super(nThreads, executor, chooserFactory, selectorProvider, selectStrategyFactory, rejectedExecutionHandler);
}
```

我们来稍微看一下构造方法中的各个参数：

- nThreads：这个最简单，就是线程池中的线程数，也就是 NioEventLoop 的实例数量。
- executor：我们知道，我们本身就是要构造一个线程池（Executor），为什么这里传一个 executor 实例呢？它其实不是给线程池用的，而是给 NioEventLoop 用的，以后再说。
- chooserFactory：当我们提交一个任务到线程池的时候，线程池需要选择（choose）其中的一个线程来执行这个任务，这个就是用来实现选择策略的。
- selectorProvider：这个简单，我们需要通过它来实例化 JDK 的 Selector，可以看到每个线程池都持有一个 selectorProvider 实例。
- selectStrategyFactory：这个涉及到的是线程池中线程的工作流程，在介绍 NioEventLoop 的时候会说。
- rejectedExecutionHandler：这个也是线程池的好朋友了，用于处理线程池中没有可用的线程来执行任务的情况。在 Netty 中稍微有一点点不一样，这个是给 NioEventLoop 实例用的，以后我们再详细介绍。

这里介绍这些参数是希望大家有个印象而已，大家发现没有，在构造 NioEventLoopGroup 实例时的好几个参数，都是用来构造 NioEventLoop 用的。

下面，我们从 NioEventLoopGroup 的无参构造方法开始，跟着源码走：

```java
public NioEventLoopGroup() {
    this(0);
}
```

然后一步步走下去，到这个构造方法：

```java
public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory, final SelectorProvider selectorProvider, final SelectStrategyFactory selectStrategyFactory) {

    super(nThreads, threadFactory, selectorProvider, selectStrategyFactory, RejectedExecutionHandlers.reject());
}
```

大家自己要去跟一下源码，这样才知道中间设置了哪些默认值，下面这几个参数都被设置了默认值： 

- selectorProvider = SelectorProvider.provider()

  > 这个没什么好说的，调用了 JDK 提供的方法

- selectStrategyFactory = DefaultSelectStrategyFactory.INSTANCE

  > 这个涉及到的是线程在做 select 操作和执行任务过程中的策略选择问题，在介绍 NioEventLoop 的时候会用到。 

- rejectedExecutionHandler = RejectedExecutionHandlers.reject()

  > 大家进去看一下 reject() 方法，也就是说，Netty 选择的默认拒绝策略是：抛出异常

跟着源码走，我们会来到父类 MultithreadEventLoopGroup 的构造方法中：

```java
protected MultithreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
    super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, threadFactory, args);
}
```

这里我们发现，如果采用无参构造函数，那么到这里的时候，默认地 nThreads 会被设置为 **CPU 核心数 \*2**。大家可以看下 DEFAULT_EVENT_LOOP_THREADS 的默认值，以及 static 代码块的设值逻辑。

我们继续往下走：

```java
protected MultithreadEventExecutorGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
    this(nThreads, threadFactory == null ? null : new ThreadPerTaskExecutor(threadFactory), args);
}
```

到这一步的时候，`new ThreadPerTaskExecutor(threadFactory)` 会构造一个 executor。

> 我们现在还不知道这个 executor 怎么用。这里我们先看下它的源码：
>
> ```java
> public final class ThreadPerTaskExecutor implements Executor {
>  private final ThreadFactory threadFactory;
> 
>  public ThreadPerTaskExecutor(ThreadFactory threadFactory) {
>      if (threadFactory == null) {
>          throw new NullPointerException("threadFactory");
>      }
>      this.threadFactory = threadFactory;
>  }
> 
>  @Override
>  public void execute(Runnable command) {
>      // 为每个任务新建一个线程
>      threadFactory.newThread(command).start();
>  }
> }
> ```
>
> Executor 作为线程池的最顶层接口， 我们知道，它只有一个 execute(runnable) 方法，从上面我们可以看到，实现类 ThreadPerTaskExecutor 的逻辑就是**每来一个任务，新建一个线程**。
>
> 我们先记住这个，前面也说了，它是给 NioEventLoop 用的，不是给 NioEventLoopGroup 用的。

上一步设置完了 executor，我们继续往下看：

```java
protected MultithreadEventExecutorGroup(int nThreads, Executor executor, Object... args) {
    this(nThreads, executor, DefaultEventExecutorChooserFactory.INSTANCE, args);
}
```

这一步设置了 chooserFactory，用来实现从线程池中选择一个线程的选择策略。

> ChooserFactory 的逻辑比较简单，我们看下 DefaultEventExecutorChooserFactory 的实现：
>
> ```java
> @Override
> public EventExecutorChooser newChooser(EventExecutor[] executors) {
>  if (isPowerOfTwo(executors.length)) {
>      return new PowerOfTwoEventExecutorChooser(executors);
>  } else {
>      return new GenericEventExecutorChooser(executors);
>  }
> }
> ```
>
> 这里设置的策略也很简单：
>
> 1、如果线程池的线程数量是 2^n，采用下面的方式会高效一些：
>
> ```java
> @Override
> public EventExecutor next() {
>  return executors[idx.getAndIncrement() & executors.length - 1];
> }
> ```
>
> 2、如果不是，用取模的方式：
>
> ```java
> @Override
> public EventExecutor next() {
>  return executors[Math.abs(idx.getAndIncrement() % executors.length)];
> }
> ```

走了这么久，我们终于到了一个**干实事**的构造方法中了：

```java
protected MultithreadEventExecutorGroup(int nThreads, Executor executor,
                                        EventExecutorChooserFactory chooserFactory, Object... args) {
    if (nThreads <= 0) {
        throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
    }

    // executor 如果是 null，做一次和前面一样的默认设置。
    if (executor == null) {
        executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
    }

    // 这里的 children 数组非常重要，它就是线程池中的线程数组，这么说不太严谨，但是就大概这个意思
    children = new EventExecutor[nThreads];

    // 下面这个 for 循环将实例化 children 数组中的每一个元素
    for (int i = 0; i < nThreads; i ++) {
        boolean success = false;
        try {
            // 实例化！！！！！！
            children[i] = newChild(executor, args);
            success = true;
        } catch (Exception e) {
            // TODO: Think about if this is a good exception type
            throw new IllegalStateException("failed to create a child event loop", e);
        } finally {
            // 如果有一个 child 实例化失败，那么 success 就会为 false，然后进入下面的失败处理逻辑
            if (!success) {
                // 把已经成功实例化的“线程” shutdown，shutdown 是异步操作
                for (int j = 0; j < i; j ++) {
                    children[j].shutdownGracefully();
                }

                // 等待这些线程成功 shutdown
                for (int j = 0; j < i; j ++) {
                    EventExecutor e = children[j];
                    try {
                        while (!e.isTerminated()) {
                            e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                        }
                    } catch (InterruptedException interrupted) {
                        // 把中断状态设置回去，交给关心的线程来处理.
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
    // ================================================
    // === 到这里，就是代表上面的实例化所有线程已经成功结束 ===
    // ================================================

    // 通过之前设置的 chooserFactory 来实例化 Chooser，把线程池数组传进去，
    //     这就不必再说了吧，实现线程选择策略
    chooser = chooserFactory.newChooser(children);

    // 设置一个 Listener 用来监听该线程池的 termination 事件
    // 下面的代码逻辑是：给池中每一个线程都设置这个 listener，当监听到所有线程都 terminate 以后，这个线程池就算真正的 terminate 了。
    final FutureListener<Object> terminationListener = new FutureListener<Object>() {
        @Override
        public void operationComplete(Future<Object> future) throws Exception {
            if (terminatedChildren.incrementAndGet() == children.length) {
                terminationFuture.setSuccess(null);
            }
        }
    };
    for (EventExecutor e: children) {
        e.terminationFuture().addListener(terminationListener);
    }

    // 设置 readonlyChildren，它是只读集合，以后用到再说
    Set<EventExecutor> childrenSet = new LinkedHashSet<EventExecutor>(children.length);
    Collections.addAll(childrenSet, children);
    readonlyChildren = Collections.unmodifiableSet(childrenSet);
}
```

上面的代码非常简单吧，没有什么需要特别说的，接下来，我们来看看 newChild() 这个方法，这个方法非常重要，它将创建线程池中的线程。

> 我上面已经用过很多次"线程"这个词了，它可不是 Thread 的意思，而是指池中的个体，后面我们会看到每个"线程"在什么时候会真正创建 Thread 实例。反正每个 NioEventLoop 实例内部都会有一个自己的 Thread 实例，所以把这两个概念混在一起也无所谓吧。

`newChild(…)` 方法在 NioEventLoopGroup 中覆写了，上面说的"线程"其实就是 NioEventLoop：

```java
@Override
protected EventLoop newChild(Executor executor, Object... args) throws Exception {
    return new NioEventLoop(this, executor, (SelectorProvider) args[0],
        ((SelectStrategyFactory) args[1]).newSelectStrategy(), (RejectedExecutionHandler) args[2]);
}
```

它调用了 NioEventLoop 的构造方法：

```java
NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider,
             SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler) {
    // 调用父类构造器
    super(parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler);
    if (selectorProvider == null) {
        throw new NullPointerException("selectorProvider");
    }
    if (strategy == null) {
        throw new NullPointerException("selectStrategy");
    }
    provider = selectorProvider;
    // 开启 NIO 中最重要的组件：Selector
    final SelectorTuple selectorTuple = openSelector();
    selector = selectorTuple.selector;
    unwrappedSelector = selectorTuple.unwrappedSelector;
    selectStrategy = strategy;
}
```

我们先粗略观察一下，然后再往下看：

- 在 Netty 中，NioEventLoopGroup 代表线程池，NioEventLoop 就是其中的线程。
- 线程池 NioEventLoopGroup 是池中的线程 NioEventLoop 的 **parent**，从上面的代码中的取名可以看出。
- 每个 NioEventLoop 都有自己的 Selector，上面的代码也反应了这一点，这和 Tomcat 中的 NIO 模型有点区别。
- executor、selectStrategy 和 rejectedExecutionHandler 从 NioEventLoopGroup 中一路传到了 NioEventLoop 中。

这个时候，我们来看一下 NioEventLoop 类的属性都有哪些，我们先忽略它继承自父类的属性，单单看它自己的：

```java
private Selector selector;
private Selector unwrappedSelector;
private SelectedSelectionKeySet selectedKeys;

private final SelectorProvider provider;

private final AtomicBoolean wakenUp = new AtomicBoolean();

private final SelectStrategy selectStrategy;

private volatile int ioRatio = 50;
private int cancelledKeys;
private boolean needsToSelectAgain;
```

结合它的构造方法我们来总结一下：

- provider：它由 NioEventLoopGroup 传进来，前面我们说了一个线程池有一个 selectorProvider，用于创建 Selector 实例

- selector：虽然我们还没看创建 selector 的代码，但我们已经知道，在 Netty 中 Selector 是跟着线程池中的线程走的。也就是说，并非一个线程池一个 Selector 实例，而是线程池中每一个线程都有一个 Selector 实例。

  > 在无参构造过程中，我们发现，Netty 设置线程个数是 CPU 核心数的两倍，假设我们的机器 CPU 是 4 核，那么对应的就会有 8 个 Selector 实例。

- selectStrategy：select 操作的策略，这个不急。

- ioRatio：这是 IO 任务的执行时间比例，因为每个线程既有 IO 任务执行，也有非 IO 任务需要执行，所以该参数为了保证有足够时间是给 IO 的。这里也不需要急着去理解什么 IO 任务、什么非 IO 任务。

然后我们继续走它的构造方法，我们看到上面的构造方法调用了父类的构造器，它的父类是 SingleThreadEventLoop。

```java
protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor,
                                boolean addTaskWakesUp, int maxPendingTasks,
                                RejectedExecutionHandler rejectedExecutionHandler) {
    super(parent, executor, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);

    // 我们可以直接忽略这个东西，以后我们也不会再介绍它
    tailTasks = newTaskQueue(maxPendingTasks);
}
```

SingleThreadEventLoop 这个名字很诡异有没有？然后它的构造方法又调用了父类 SingleThreadEventExecutor 的构造方法：

```java
protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor,
                                    boolean addTaskWakesUp, int maxPendingTasks,
                                    RejectedExecutionHandler rejectedHandler) {
    super(parent);
    this.addTaskWakesUp = addTaskWakesUp;
    this.maxPendingTasks = Math.max(16, maxPendingTasks);
    this.executor = ObjectUtil.checkNotNull(executor, "executor");
    // taskQueue，这个东西很重要，提交给 NioEventLoop 的任务都会进入到这个 taskQueue 中等待被执行
    // 这个 queue 的默认容量是 16 ?? 实际传入最大值为Integer.MAX_VALUE
    taskQueue = newTaskQueue(this.maxPendingTasks);
    rejectedExecutionHandler = ObjectUtil.checkNotNull(rejectedHandler, "rejectedHandler");
}
```

到这里就更加诡异了，NioEventLoop 的父类是 SingleThreadEventLoop，而 SingleThreadEventLoop 的父类是 **SingleThreadEventExecutor**，它的名字告诉我们，它是一个 Executor，是一个线程池，而且是 Single Thread 单线程的。

也就是说，线程池 NioEventLoopGroup 中的每一个线程 NioEventLoop 也可以当做一个线程池来用，只不过它只有一个线程。这种设计虽然看上去很巧妙，不过有点反人类的样子。

上面这个构造函数比较简单：

- 设置了 parent，也就是之前创建的线程池 NioEventLoopGroup 实例

- executor：它是我们之前实例化的 ThreadPerTaskExecutor，我们说过，这个东西在线程池中没有用，它是给 NioEventLoop 用的，马上我们就要看到它了。提前透露一下，它用来开启 NioEventLoop 中的线程（Thread 实例）。

- taskQueue：这算是该构造方法中新的东西，它是任务队列。我们前面说过，NioEventLoop 需要负责 IO 事件和非 IO 事件，通常它都在执行 selector 的 select 方法或者正在处理 selectedKeys，如果我们要 submit 一个任务给它，任务就会被放到 taskQueue 中，等它来轮询。该队列是线程安全的 LinkedBlockingQueue，默认容量为 16。==（存疑？是MpscUnboundedArrayQueue 容量默认1024 至Integer.MAX_VALUE）==

- rejectedExecutionHandler：taskQueue 的默认容量是 16，所以，如果 submit 的任务堆积了到了 16，再往里面提交任务会触发 rejectedExecutionHandler 的执行策略。

  > 还记得默认策略吗：抛出RejectedExecutionException 异常。
  >
  > 在 NioEventLoopGroup 的默认构造中，它的实现是这样的：
  >
  > ```java
  > private static final RejectedExecutionHandler REJECT = new RejectedExecutionHandler() {
  >     @Override
  >     public void rejected(Runnable task, SingleThreadEventExecutor executor) {
  >         throw new RejectedExecutionException();
  >     }
  > };
  > ```

然后，我们再回到 NioEventLoop 的构造方法：

```java
NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider,
             SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler) {
    // 我们刚刚说完了这个
    super(parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler);
    if (selectorProvider == null) {
        throw new NullPointerException("selectorProvider");
    }
    if (strategy == null) {
        throw new NullPointerException("selectStrategy");
    }
    provider = selectorProvider;
    // 创建 selector 实例
    final SelectorTuple selectorTuple = openSelector();
    selector = selectorTuple.selector;
    unwrappedSelector = selectorTuple.unwrappedSelector;

    selectStrategy = strategy;
}
```

可以看到，最重要的方法其实就是 openSelector() 方法，它将创建 NIO 中最重要的一个组件 **Selector**。在这个方法中，Netty 也做了一些优化，这部分我们就不去分析它了。

到这里，我们的线程池 NioEventLoopGroup 创建完成了，并且实例化了池中的所有 NioEventLoop 实例。

同时，大家应该已经看到，上面并没有真正创建 NioEventLoop 中的线程（没有创建 Thread 实例）。

提前透露一下，创建线程的时机在第一个任务提交过来的时候，那么第一个任务是什么呢？是我们马上要说的 channel 的 **register** 操作。

## Channel 的 register 操作

经过前面的铺垫，我们已经具备一定的基础了，我们开始来把前面学到的内容揉在一起。这节，我们会介绍 register 操作，这一步其实是非常关键的，对于我们源码分析非常重要。

我们从 EchoClient 中的 connect() 方法出发，或者 EchoServer 的 bind(port) 方法出发，都会走到 initAndRegister() 这个方法：

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;
    try {
        // 1
        channel = channelFactory.newChannel();
        // 2 对于 Bootstrap 和 ServerBootstrap，这里面有些不一样
        init(channel);
    } catch (Throwable t) {
        ...
    }
    // 3 我们这里要说的是这行
    ChannelFuture regFuture = config().group().register(channel);
    if (regFuture.cause() != null) {
        if (channel.isRegistered()) {
            channel.close();
        } else {
            channel.unsafe().closeForcibly();
        }
    }
    return regFuture;
}
```

initAndRegister() 这个方法我们已经接触过两次了，前面介绍了 1️⃣ Channel 的实例化，实例化过程中，会执行 Channel 内部 Unsafe 和 Pipeline 的实例化，以及在上面 2️⃣ init(channel) 方法中，会往 pipeline 中添加 handler（pipeline 此时是 head+channelnitializer+tail）。

> 我们这节终于要揭秘 ChannelInitializer 中的 initChannel 方法了~~~

现在，我们继续往下走，看看 3️⃣ **register** 这一步：

```java
ChannelFuture regFuture = config().group().register(channel);
```

> 我们说了，register 这一步是非常关键的，它发生在 channel 实例化以后，大家回忆一下当前 channel 中的一些情况：
>
> 实例化了 JDK 底层的 Channel，设置了非阻塞，实例化了 Unsafe，实例化了 Pipeline，同时往 pipeline 中添加了 head、tail 以及一个 ChannelInitializer 实例。

上面的 `config().group()` 方法会返回前面实例化的 NioEventLoopGroup 的实例，然后调用其 register(channel) 方法：

// MultithreadEventLoopGroup

```java
@Override
public ChannelFuture register(Channel channel) {
    return next().register(channel);
}
```

next() 方法很简单，就是选择线程池中的一个线程（还记得 chooserFactory 吗），也就是选择一个 NioEventLoop 实例，这个时候我们就进入到 NioEventLoop 了。

NioEventLoop 的 register(channel) 方法实现在它的父类 **SingleThreadEventLoop** 中：

```java
@Override
public ChannelFuture register(Channel channel) {
    return register(new DefaultChannelPromise(channel, this));
}
```

上面的代码实例化了一个 Promise，将当前 channel 带了进去：

```java
@Override
public ChannelFuture register(final ChannelPromise promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    // promise 关联了 channel，channel 持有 Unsafe 实例，register 操作就封装在 Unsafe 中
    promise.channel().unsafe().register(this, promise);
    return promise;
}
```

拿到 channel 中关联的 Unsafe 实例，然后调用它的 register 方法：

> 我们说过，Unsafe 专门用来封装底层实现，当然这里也没那么“底层”

// AbstractChannel#**AbstractUnsafe**

```java
@Override
public final void register(EventLoop eventLoop, final ChannelPromise promise) {
    ...
    // 将这个 eventLoop 实例设置给这个 channel，从此这个 channel 就是有 eventLoop 的了
    // 我觉得这一步其实挺关键的，因为后续该 channel 中的所有异步操作，都要提交给这个 eventLoop 来执行
    AbstractChannel.this.eventLoop = eventLoop;

    // 如果发起 register 动作的线程就是 eventLoop 实例中的线程，那么直接调用 register0(promise)
    // 对于我们来说，它不会进入到这个分支，
    //     之所以有这个分支，是因为我们是可以 unregister，然后再 register 的，后面再仔细看
    if (eventLoop.inEventLoop()) {
        register0(promise);
    } else {
        try {
            // 否则，提交任务给 eventLoop，eventLoop 中的线程会负责调用 register0(promise)
            eventLoop.execute(new Runnable() {
                @Override
                public void run() {
                    register0(promise);
                }
            });
        } catch (Throwable t) {
            ...
        }
    }
}
```

> 到这里，我们要明白，NioEventLoop 中是还没有实例化 Thread 实例的。

这几步涉及到了好几个类：NioEventLoop、Promise、Channel、Unsafe 等，大家要仔细理清楚它们的关系。

对于我们前面过来的 register 操作，其实提交到 eventLoop 以后，就直接返回 promise 实例了，剩下的register0 是异步操作，它由 NioEventLoop 实例来完成。

我们这边先不继续往里分析 register0(promise) 方法，先把前面欠下的 NioEventLoop 中的线程介绍清楚，然后再回来介绍这个 register0 方法。

> Channel 实例一旦 register 到了 NioEventLoopGroup 实例中的某个 NioEventLoop 实例，那么后续该 Channel 的所有操作，都是由该 NioEventLoop 实例来完成的。
>
> 这个也非常简单，因为 Selector 实例是在 NioEventLoop 实例中的，Channel 实例一旦注册到某个 Selector 实例中，当然也只能在这个实例中处理 NIO 事件。

## NioEventLoop 工作流程

前面，我们在分析线程池的实例化的时候说过，NioEventLoop 中并没有启动 Java 线程。这里我们来仔细分析下在 register 过程中调用的 **eventLoop.execute(runnable)** 这个方法，这个代码在父类 SingleThreadEventExecutor 中：

```java
@Override
public void execute(Runnable task) {
    if (task == null) {
        throw new NullPointerException("task");
    }
    // 判断添加任务的线程是否就是当前 EventLoop 中的线程
    boolean inEventLoop = inEventLoop();

    // 添加任务到之前介绍的 taskQueue 中，
    //     如果 taskQueue 满了(默认大小 16)，根据我们之前说的，默认的策略是抛出异常
    addTask(task);

    if (!inEventLoop) {
        // 如果不是 NioEventLoop 内部线程提交的 task，那么判断下线程是否已经启动，没有的话，就启动线程
        startThread();
        if (isShutdown() && removeTask(task)) {
            reject();
        }
    }

    if (!addTaskWakesUp && wakesUpForTask(task)) {
        wakeup(inEventLoop);
    }
}
```

> 原来启动 NioEventLoop 中的线程的方法在这里。
>
> 另外，上节我们说的 register 操作进到了 taskQueue 中，所以它其实是被归类到了非 IO 操作的范畴。

下面是 startThread 的源码，判断线程是否已经启动来决定是否要进行启动操作：

```java
private void startThread() {
    if (state == ST_NOT_STARTED) {
        if (STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_STARTED)) {
            try {
                doStartThread();
            } catch (Throwable cause) {
                STATE_UPDATER.set(this, ST_NOT_STARTED);
                PlatformDependent.throwException(cause);
            }
        }
    }
}
```

我们按照前面的思路，根据线程没有启动的情况，来看看 doStartThread() 方法：

```java
private void doStartThread() {
    assert thread == null;
    // 这里的 executor 大家是不是有点熟悉的感觉，它就是一开始我们实例化 NioEventLoop 的时候传进来的 ThreadPerTaskExecutor 的实例。它是每次来一个任务，创建一个线程的那种 executor。
    // 一旦我们调用它的 execute 方法，它就会创建一个新的线程，所以这里终于会创建 Thread 实例
    executor.execute(new Runnable() {
        @Override
        public void run() {
            // 看这里，将 “executor” 中创建的这个线程设置为 NioEventLoop 的线程！！！
            thread = Thread.currentThread();

            if (interrupted) {
                thread.interrupt();
            }

            boolean success = false;
            updateLastExecutionTime();
            try {
                // 执行 SingleThreadEventExecutor 的 run() 方法，它在 NioEventLoop 中实现了
                SingleThreadEventExecutor.this.run();
                success = true;
            } catch (Throwable t) {
                logger.warn("Unexpected exception from an event executor: ", t);
            } finally {
                // ... 我们直接忽略掉这里的代码
            }
        }
    });
}
```

上面线程启动以后，会执行 NioEventLoop 中的 run() 方法，这是一个**非常重要**的方法，这个方法肯定是没那么容易结束的，必然是像 JDK 线程池的 Worker 那样，不断地循环获取新的任务的。它需要不断地做 select 操作和轮询 taskQueue 这个队列。

我们先来简单地看一下它的源码，这里先不做深入地介绍：

```java
@Override
protected void run() {
    // 代码嵌套在 for 循环中
    for (;;) {
        try {
            // selectStrategy 终于要派上用场了
            // 它有两个值，一个是 CONTINUE 一个是 SELECT
            // 针对这块代码，我们分析一下。
            // 1. 如果 taskQueue 不为空，也就是 hasTasks() 返回 true，
            //         那么执行一次 selectNow()，该方法不会阻塞
            // 2. 如果 hasTasks() 返回 false，那么执行 SelectStrategy.SELECT 分支，
            //    进行 select(...)，这块是带阻塞的
            // 这个很好理解，就是按照是否有任务在排队来决定是否可以进行阻塞
            switch (selectStrategy.calculateStrategy(selectNowSupplier, hasTasks())) {
                case SelectStrategy.CONTINUE:
                    continue;
                case SelectStrategy.SELECT:
                    // 如果 !hasTasks()，那么进到这个 select 分支，这里 select 带阻塞的
                    select(wakenUp.getAndSet(false));
                    if (wakenUp.get()) {
                        selector.wakeup();
                    }
                default:
            }


            cancelledKeys = 0;
            needsToSelectAgain = false;
            // 默认地，ioRatio 的值是 50
            final int ioRatio = this.ioRatio;

            if (ioRatio == 100) {
                // 如果 ioRatio 设置为 100，那么先执行 IO 操作，然后在 finally 块中执行 taskQueue 中的任务
                try {
                    // 1. 执行 IO 操作。因为前面 select 以后，可能有些 channel 是需要处理的。
                    processSelectedKeys();
                } finally {
                    // 2. 执行非 IO 任务，也就是 taskQueue 中的任务
                    runAllTasks();
                }
            } else {
                // 如果 ioRatio 不是 100，那么根据 IO 操作耗时，限制非 IO 操作耗时
                final long ioStartTime = System.nanoTime();
                try {
                    // 执行 IO 操作
                    processSelectedKeys();
                } finally {
                    // 根据 IO 操作消耗的时间，计算执行非 IO 操作（runAllTasks）可以用多少时间.
                    final long ioTime = System.nanoTime() - ioStartTime;
                    runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                }
            }
        } catch (Throwable t) {
            handleLoopException(t);
        }
        // Always handle shutdown even if the loop processing threw an exception.
        try {
            if (isShuttingDown()) {
                closeAll();
                if (confirmShutdown()) {
                    return;
                }
            }
        } catch (Throwable t) {
            handleLoopException(t);
        }
    }
}
```

上面这段代码是 NioEventLoop 的核心，这里介绍两点：

1. 首先，会根据 hasTasks() 的结果来决定是执行 selectNow() 还是 select(oldWakenUp)，这个应该好理解。如果有任务正在等待，那么应该使用无阻塞的 selectNow()，如果没有任务在等待，那么就可以使用带阻塞的 select 操作。
2. ioRatio 控制 IO 操作所占的时间比重：
   - 如果设置为 100%，那么先执行 IO 操作，然后再执行任务队列中的任务。
   - 如果不是 100%，那么先执行 IO 操作，然后执行 taskQueue 中的任务，但是需要控制执行任务的总时间。也就是说，非 IO 操作可以占用的时间，通过 ioRatio 以及这次 IO 操作耗时计算得出。

我们这里先不要去关心 select(oldWakenUp)、processSelectedKeys() 方法和 runAllTasks(…) 方法的细节，只要先理解它们分别做什么事情就可以了。

回过神来，我们前面在 register 的时候提交了 register 任务给 NioEventLoop，这是 NioEventLoop 接收到的第一个任务，所以这里会实例化 Thread 并且启动，然后进入到 NioEventLoop 中的 run 方法。

> 当然了，实际情况可能是，Channel 实例被 register 到一个已经启动线程的 NioEventLoop 实例中。

## 回到 Channel 的 register 操作

我们回到前面的 register0(promise) 方法，我们知道，这个 register 任务进入到了 NioEventLoop 的 taskQueue 中，然后会启动 NioEventLoop 中的线程，该线程会轮询这个 taskQueue，然后执行这个 register 任务。

注意，此时执行该方法的是 eventLoop 中的线程：

// AbstractChannel

```java
private void register0(ChannelPromise promise) {
    try {
        ...
        boolean firstRegistration = neverRegistered;
        // *** 进行 JDK 底层的操作：Channel 注册到 Selector 上 ***
        doRegister();

        neverRegistered = false;
        registered = true;
        // 到这里，就算是 registered 了

        // 这一步也很关键，因为这涉及到了 ChannelInitializer 的 init(channel)
        // 我们之前说过，init 方法会将 ChannelInitializer 内部添加的 handlers 添加到 pipeline 中
        pipeline.invokeHandlerAddedIfNeeded();

        // 设置当前 promise 的状态为 success
        //   因为当前 register 方法是在 eventLoop 中的线程中执行的，需要通知提交 register 操作的线程
        safeSetSuccess(promise);

        // 当前的 register 操作已经成功，该事件应该被 pipeline 上
        //   所有关心 register 事件的 handler 感知到，往 pipeline 中扔一个事件
        pipeline.fireChannelRegistered();

        // 这里 active 指的是 channel 已经打开
        if (isActive()) {
            // 如果该 channel 是第一次执行 register，那么 fire ChannelActive 事件
            if (firstRegistration) {
                pipeline.fireChannelActive();
            } else if (config().isAutoRead()) {
                // 该 channel 之前已经 register 过了，
                // 这里让该 channel 立马去监听通道中的 OP_READ 事件
                beginRead();
            }
        }
    } catch (Throwable t) {
        ...
    }
}
```

我们先说掉上面的 doRegister() 方法，然后再说 pipeline。

```java
@Override
protected void doRegister() throws Exception {
    boolean selected = false;
    for (;;) {
        try {
            // 附 JDK 中 Channel 的 register 方法：
            // public final SelectionKey register(Selector sel, int ops, Object att) {...}
            selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
            return;
        } catch (CancelledKeyException e) {
            ...
        }
    }
}
```

我们可以看到，这里做了 JDK 底层的 register 操作，将 SocketChannel(或 ServerSocketChannel) 注册到 Selector 中，并且可以看到，这里的监听集合设置为了 **0**，也就是什么都不监听。

> 当然，也就意味着，后续一定有某个地方会需要修改这个 selectionKey 的监听集合，不然啥都干不了

我们重点来说说 **pipeline** 操作，我们之前在介绍 NioSocketChannel 的 pipeline 的时候介绍到，我们的 pipeline 现在长这个样子：

![20](D:\Java\document\study\images\netty-20.png)

> 现在，我们将看到这里会把 LoggingHandler 和 EchoClientHandler 添加到 pipeline。

我们继续看代码，register 成功以后，执行了以下操作：

```java
pipeline.invokeHandlerAddedIfNeeded();
```

大家可以跟踪一下，这一步会执行到 pipeline 中 ChannelInitializer 实例的 handlerAdded 方法，在这里会执行它的 init(context) 方法：

```java
@Override
public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    if (ctx.channel().isRegistered()) {
        initChannel(ctx);
    }
}
```

然后我们看下 initChannel(ctx)，这里终于来了我们之前介绍过的 init(channel) 方法：

```java
private boolean initChannel(ChannelHandlerContext ctx) throws Exception {
    if (initMap.putIfAbsent(ctx, Boolean.TRUE) == null) { // Guard against re-entrance.
        try {
            // 1. 将把我们自定义的 handlers 添加到 pipeline 中
            initChannel((C) ctx.channel());
        } catch (Throwable cause) {
            ...
        } finally {
            // 2. 将 ChannelInitializer 实例从 pipeline 中删除
            remove(ctx);
        }
        return true;
    }
    return false;
}
```

我们前面也说过，ChannelInitializer 的 init(channel) 被执行以后，那么其内部添加的 handlers 会进入到 pipeline 中，然后上面的 finally 块中将 ChannelInitializer 的实例从 pipeline 中删除，那么此时 pipeline 就算建立起来了，如下图：

![21](D:\Java\document\study\images\netty-21.png)

> 其实这里还有个问题，如果我们在 ChannelInitializer 中添加的是一个 ChannelInitializer 实例呢？大家可以考虑下这个情况。

pipeline 建立了以后，然后我们继续往下走，会执行到这一句：

```java
pipeline.fireChannelRegistered();
```

我们只要摸清楚了 fireChannelRegistered() 方法，以后碰到其他像 fireChannelActive()、fireXxx() 等就知道怎么回事了，它们都是类似的。我们来看看这句代码会发生什么：

// DefaultChannelPipeline

```java
@Override
public final ChannelPipeline fireChannelRegistered() {
    // 注意这里的传参是 head
    AbstractChannelHandlerContext.invokeChannelRegistered(head);
    return this;
}
```

也就是说，我们往 pipeline 中扔了一个 **channelRegistered** 事件，这里的 register 属于 Inbound 事件，pipeline 接下来要做的就是执行 pipeline 中的 Inbound 类型的 handlers 中的 channelRegistered() 方法。

从上面的代码，我们可以看出，往 pipeline 中扔出 channelRegistered 事件以后，第一个处理的 handler 是 **head**。

接下来，我们还是跟着代码走，此时我们来到了 pipeline 的第一个节点 **head** 的处理中：

// AbstractChannelHandlerContext

```java
// next 此时是 head
static void invokeChannelRegistered(final AbstractChannelHandlerContext next) {

    EventExecutor executor = next.executor();
    // 执行 head 的 invokeChannelRegistered()
    if (executor.inEventLoop()) {
        next.invokeChannelRegistered();
    } else {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                next.invokeChannelRegistered();
            }
        });
    }
}
```

也就是说，这里会先执行 head.invokeChannelRegistered() 方法，而且是放到 NioEventLoop 中的 taskQueue 中执行的：

// AbstractChannelHandlerContext

```java
private void invokeChannelRegistered() {
    if (invokeHandler()) {
        try {
            // handler() 方法此时会返回 head
            ((ChannelInboundHandler) handler()).channelRegistered(this);
        } catch (Throwable t) {
            notifyHandlerException(t);
        }
    } else {
        fireChannelRegistered();
    }
}
```

我们去看 head 的 channelRegistered 方法：

// HeadContext

```java
@Override
public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    // 1. 这一步是 head 对于 channelRegistered 事件的处理。没有我们要关心的
    invokeHandlerAddedIfNeeded();
    // 2. 向后传播 Inbound 事件
    ctx.fireChannelRegistered();
}
```

然后 head 会执行 fireChannelRegister() 方法：

// AbstractChannelHandlerContext

```java
@Override
public ChannelHandlerContext fireChannelRegistered() {
    // 这里很关键
    // findContextInbound() 方法会沿着 pipeline 找到下一个 Inbound 类型的 handler
    invokeChannelRegistered(findContextInbound());
    return this;
}
```

> 注意：pipeline.fireChannelRegistered() 是将 channelRegistered 事件抛到 pipeline 中，pipeline 中的 handlers 准备处理该事件。而 context.fireChannelRegistered() 是一个 handler 处理完了以后，向后传播给下一个 handler。
>
> 它们两个的方法名字是一样的，但是来自于不同的类。

findContextInbound() 将找到下一个 Inbound 类型的 handler，然后又是重复上面的几个方法。

> 我觉得上面这块代码没必要太纠结，总之就是从 head 中开始，依次往下寻找所有 Inbound handler，执行其 channelRegistered(ctx) 操作。

说了这么多，我们的 register 操作算是真正完成了。

下面，我们回到 initAndRegister 这个方法：

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;
    try {
        channel = channelFactory.newChannel();
        init(channel);
    } catch (Throwable t) {
        ...
    }

    // 我们上面说完了这行
    ChannelFuture regFuture = config().group().register(channel);

    // 如果在 register 的过程中，发生了错误
    if (regFuture.cause() != null) {
        if (channel.isRegistered()) {
            channel.close();
        } else {
            channel.unsafe().closeForcibly();
        }
    }

    // 源码中说得很清楚，如果到这里，说明后续可以进行 connect() 或 bind() 了，因为两种情况：
    // 1. 如果 register 动作是在 eventLoop 中发起的，那么到这里的时候，register 一定已经完成
    // 2. 如果 register 任务已经提交到 eventLoop 中，也就是进到了 eventLoop 中的 taskQueue 中，
    //    由于后续的 connect 或 bind 也会进入到同一个 eventLoop 的 queue 中，所以一定是会先 register 成功，才会执行 connect 或 bind
    return regFuture;
}
```

我们要知道，不管是服务端的 NioServerSocketChannel 还是客户端的 NioSocketChannel，在 bind 或 connect 时，都会先进入 initAndRegister 这个方法，所以我们上面说的那些，对于两者都是通用的。

大家要记住，register 操作是非常重要的，要知道这一步大概做了哪些事情，register 操作以后，将进入到 bind 或 connect 操作中。

## connect 过程和 bind 过程分析

上面我们介绍的 register 操作非常关键，它建立起来了很多的东西，它是 Netty 中 NioSocketChannel 和 NioServerSocketChannel 开始工作的起点。

这一节，我们来说说 register 之后的 connect 操作和 bind 操作。这节非常简单。



### connect 过程分析

对于客户端 NioSocketChannel 来说，前面 register 完成以后，就要开始 connect 了，这一步将连接到服务端。

```java
private ChannelFuture doResolveAndConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
    // 这里完成了 register 操作
    final ChannelFuture regFuture = initAndRegister();
    final Channel channel = regFuture.channel();

    // 这里我们不去纠结 register 操作是否 isDone()
    if (regFuture.isDone()) {
        if (!regFuture.isSuccess()) {
            return regFuture;
        }
        // 看这里
        return doResolveAndConnect0(channel, remoteAddress, localAddress, channel.newPromise());
    } else {
        ....
    }
}
```

这里大家自己一路点进去，我就不浪费篇幅了。最后，我们会来到 AbstractChannel 的 connect 方法：

```java
@Override
public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
    return pipeline.connect(remoteAddress, promise);
}
```

我们看到，connect 操作是交给 pipeline 来执行的。进入 pipeline 中，我们会发现，connect 这种 Outbound 类型的操作，是从 pipeline 的 tail 开始的：

> 前面我们介绍的 register 操作是 Inbound 的，是从 head 开始的

```java
@Override
public final ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
    return tail.connect(remoteAddress, promise);
}
```

接下来就是 pipeline 的操作了，从 tail 开始，执行 pipeline 上的 Outbound 类型的 handlers 的 connect(...) 方法，那么真正的底层的 connect 的操作发生在哪里呢？还记得我们的 pipeline 的图吗？

![22](D:\Java\document\study\images\netty-22.png)

从 tail 开始往前找 out 类型的 handlers，每经过一个 handler，都执行里面的 connect() 方法，最后会到 head 中，因为 head 也是 Outbound 类型的，我们需要的 connect 操作就在 head 中，它会负责调用 unsafe 中提供的 connect 方法：

```java
// HeadContext
public void connect(
        ChannelHandlerContext ctx,
        SocketAddress remoteAddress, SocketAddress localAddress,
        ChannelPromise promise) throws Exception {
    unsafe.connect(remoteAddress, localAddress, promise);
}
```

接下来，我们来看一看 connect 在 unsafe 类中所谓的底层操作：

```java
// AbstractNioChannel.AbstractNioUnsafe
@Override
public final void connect(
        final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
        ......

        boolean wasActive = isActive();
        // 大家自己点进去看 doConnect 方法
        // 这一步会做 JDK 底层的 SocketChannel connect，然后设置 interestOps 为 SelectionKey.OP_CONNECT
        // 返回值代表是否已经连接成功
        if (doConnect(remoteAddress, localAddress)) {
            // 处理连接成功的情况
            fulfillConnectPromise(promise, wasActive);
        } else {
            connectPromise = promise;
            requestedRemoteAddress = remoteAddress;

            // 下面这块代码，在处理连接超时的情况，代码很简单
            // 这里用到了 NioEventLoop 的定时任务的功能，这个我们之前一直都没有介绍过，因为我觉得也不太重要
            int connectTimeoutMillis = config().getConnectTimeoutMillis();
            if (connectTimeoutMillis > 0) {
                connectTimeoutFuture = eventLoop().schedule(new Runnable() {
                    @Override
                    public void run() {
                        ChannelPromise connectPromise = AbstractNioChannel.this.connectPromise;
                        ConnectTimeoutException cause =
                                new ConnectTimeoutException("connection timed out: " + remoteAddress);
                        if (connectPromise != null && connectPromise.tryFailure(cause)) {
                            close(voidPromise());
                        }
                    }
                }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
            }

            promise.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isCancelled()) {
                        if (connectTimeoutFuture != null) {
                            connectTimeoutFuture.cancel(false);
                        }
                        connectPromise = null;
                        close(voidPromise());
                    }
                }
            });
        }
    } catch (Throwable t) {
        promise.tryFailure(annotateConnectException(t, remoteAddress));
        closeIfClosed();
    }
}
```

如果上面的 doConnect 方法返回 false，那么后续是怎么处理的呢？

在上一节介绍的 register 操作中，channel 已经 register 到了 selector 上，只不过将 interestOps 设置为了 0，也就是什么都不监听。

而在上面的 doConnect 方法中，我们看到它在调用底层的 connect 方法后，会设置 interestOps 为 `SelectionKey.OP_CONNECT`。

剩下的就是 NioEventLoop 的事情了，还记得 NioEventLoop 的 run() 方法吗？也就是说这里的 connect 成功以后，这个 TCP 连接就建立起来了，后续的操作会在 `NioEventLoop.run()` 方法中被 `processSelectedKeys()` 方法处理掉。

### bind 过程分析

说完 connect 过程，我们再来简单看下 bind 过程：

```java
private ChannelFuture doBind(final SocketAddress localAddress) {
    // **前面说的 initAndRegister**
    final ChannelFuture regFuture = initAndRegister();

    final Channel channel = regFuture.channel();
    if (regFuture.cause() != null) {
        return regFuture;
    }

    if (regFuture.isDone()) {
        // register 动作已经完成，那么执行 bind 操作
        ChannelPromise promise = channel.newPromise();
        doBind0(regFuture, channel, localAddress, promise);
        return promise;
    } else {
        ......
    }
}
```

然后一直往里看，会看到，bind 操作也是要由 pipeline 来完成的：

// AbstractChannel

```java
@Override
public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
    return pipeline.bind(localAddress, promise);
}
```

bind 操作和 connect 一样，都是 Outbound 类型的，所以都是 tail 开始：

```java
@Override
public final ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
    return tail.bind(localAddress, promise);
}
```

最后的 bind 操作又到了 head 中，由 head 来调用 unsafe 提供的 bind 方法：

```java
@Override
public void bind(
        ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
        throws Exception {
    unsafe.bind(localAddress, promise);
}
```

感兴趣的读者自己去看一下 unsafe 中的 bind 方法，非常简单，bind 操作也不是什么异步方法，我们就介绍到这里了。

本节非常简单，就是想和大家介绍下 Netty 中各种操作的套路。

