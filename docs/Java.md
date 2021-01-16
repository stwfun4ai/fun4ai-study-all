# 知识体系

- JVM	
- 多线程	
- 设计模式	
- Redis	
- ZK	
- Mysql调优

- 算法 

- 网络Netty  



**==简历==**





# English





# HashMap

## JDK7 HashMap



![1](images\jdk7_hashmap.png)

HashMap 里面是一个==**数组**==，然后数组中每个元素是一个==**单向链表**==。

每个绿色的实体是嵌套类 Entry 的实例，**Entry** 包含四个属性：key, value, hash 值和用于单向链表的 next。

**capacity**：当前数组容量，始终保持 2^n，（初始默认`16`）可以扩容，扩容后数组大小为当前的 `2 倍`。

**loadFactor**：负载因子，默认为 `0.75`。

**threshold**：扩容的阈值，等于 capacity * loadFactor（即初始为16*0.75=12）



### put 过程分析

```java
public V put(K key, V value) {
    // 默认为空数组，插入第一个元素时，先初始化数组大小
    if (table == EMPTY_TABLE) {
        inflateTable(threshold);
    }
    // 处理null key
    if (key == null)
        return putForNullKey(value);
    // 1. 取得key的hash
    int hash = hash(key);
	// 2. 取得hash对应数组下标 （解释数组为什么必须为2的幂）
    int i = indexFor(hash, table.length);
    // 3. 遍历对应下标处的链表，查看是否有该key存在，存在则新值覆盖并返回旧值
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {
        Object k;
        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }

    modCount++;
    // 4. 不存在该key,添加entry到链表中
    addEntry(hash, key, value, i);
    return null;
}
```

```java
private void inflateTable(int toSize) {
    // Find a power of 2 >= toSize
    int capacity = roundUpToPowerOf2(toSize);

    threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
    table = new Entry[capacity];
    initHashSeedAsNeeded(capacity);
}
private static int roundUpToPowerOf2(int number) {
    // assert number >= 0 : "number must be non-negative";
    return number >= MAXIMUM_CAPACITY
        ? MAXIMUM_CAPACITY
        : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
}
```

>  **处理null key**

```java
// 处理null key，数组【0】下有null key则直接覆盖并返回原v值，无null key则添加对应(null,value)
// 数组【0】下可能有其他元素存储
private V putForNullKey(V value) {
    for (Entry<K,V> e = table[0]; e != null; e = e.next) {
        if (e.key == null) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }
    modCount++;
    addEntry(0, null, value, 0);
    return null;
}
```



```java
// 取得key的hash
final int hash(Object k){
    int h = hashSeed;
    if (0!=h && k instanceof String){
        return sun.misc.Hashing.stringHash32((String) k);
    }
    
    h ^= k.hashCode();
    // 防止哈希冲突，让高位参与进来
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^  (h >>> 7) ^  (h >>> 4); 
}

// 取得hash对应数组下标
// 解释数组为什么必须为2的幂，取hash值的低n位作为数组下标，如16-1(0000 1111)取低4位为数组下标
static int indexFor(int h, int length){
	return h & (length - 1);
}

```

> **扩容**

```java
void addEntry(int hash, K key, V value, int bucketIndex) {
    // 判断存储元素大于阈值,并且将要插入到的数组位置不为空时
    // 扩容 *2
    if ((size >= threshold) && (null != table[bucketIndex])) {
        resize(2 * table.length);
        // 扩容后重新计算hash值
        hash = (null != key) ? hash(key) : 0;
        // 扩容后重新计算新下标
        bucketIndex = indexFor(hash, table.length);
    }

    createEntry(hash, key, value, bucketIndex);
}

// 将新值放到链表的表头，然后 size++
void createEntry(int hash, K key, V value, int bucketIndex) {
    Entry<K,V> e = table[bucketIndex];
    table[bucketIndex] = new Entry<>(hash, key, value, e);
    size++;
}

// 具体扩容方法
void resize(int newCapacity) {
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
        threshold = Integer.MAX_VALUE;
        return;
    }

    Entry[] newTable = new Entry[newCapacity];
    transfer(newTable, initHashSeedAsNeeded(newCapacity));
    table = newTable;
    threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
}
/**
 * Transfers all entries from current table to newTable.
 */
void transfer(Entry[] newTable, boolean rehash) {
    int newCapacity = newTable.length;
    for (Entry<K,V> e : table) {
        while(null != e) {
            Entry<K,V> next = e.next;
            if (rehash) {
                e.hash = null == e.key ? 0 : hash(e.key);
            }
            // 新容量为2倍即（length-1=...0000 1111变为...0001 1111），与hash &运算后
            // 原值要么存储在新数组原下标处，要么原下标+16(原数组长度)
            // ===链表使用头插===，所以扩容后原链表顺序反转
            int i = indexFor(e.hash, newCapacity);
            e.next = newTable[i];
            newTable[i] = e;
            e = next;
        }
    }
}

```

### get 过程分析

```java
public V get(Object key) {
    // key 为 null会被放到 table[0]，所以只要遍历下table[0]处的链表就可以了
    if (key == null)
        return getForNullKey();
    Entry<K,V> entry = getEntry(key);

    return null == entry ? null : entry.getValue();
}
private V getForNullKey() {
    if (size == 0) {
        return null;
    }
    for (Entry<K,V> e = table[0]; e != null; e = e.next) {
        if (e.key == null)
            return e.value;
    }
    return null;
}
```

```java
final Entry<K,V> getEntry(Object key) {
    if (size == 0) {
        return null;
    }

    int hash = (key == null) ? 0 : hash(key);
    // 确定数组下标，然后从头开始遍历链表，直到找到为止
    for (Entry<K,V> e = table[indexFor(hash, table.length)];
         e != null;
         e = e.next) {
        Object k;
        if (e.hash == hash &&
            ((k = e.key) == key || (key != null && key.equals(k))))
            return e;
    }
    return null;
}
```



## JDK7 ConcurrentHahMap

![3](images\jdk7_ConcurrentHashMap.png)

**concurrencyLevel**：并行级别、并发数、Segment 数，怎么翻译不重要，理解它。默认是 `16`，也就是说 ConcurrentHashMap 有 16 个 Segments，所以理论上，这个时候，最多可以同时支持 16 个线程并发写，只要它们的操作分别分布在不同的 Segment 上。这个值可以在初始化的时候设置为其他值，但是一旦初始化以后，它是不可以扩容的。

再具体到每个 Segment 内部，其实每个 Segment 很像之前介绍的 HashMap，不过它要保证线程安全，所以处理起来要麻烦些。



### 初始化

initialCapacity：初始容量，这个值指的是整个 ConcurrentHashMap 的初始容量，实际操作的时候需要平均分给每个 Segment。

loadFactor：负载因子，之前我们说了，Segment 数组不可以扩容，所以这个负载因子是给每个 Segment 内部使用的。

```java
public ConcurrentHashMap(int initialCapacity,
                         float loadFactor, int concurrencyLevel) {
    if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
        throw new IllegalArgumentException();
    if (concurrencyLevel > MAX_SEGMENTS)
        concurrencyLevel = MAX_SEGMENTS;
    // Find power-of-two sizes best matching arguments
    int sshift = 0;
    int ssize = 1;
    // 计算并行级别ssize，保证是2的n次方
    while (ssize < concurrencyLevel) {
        ++sshift;
        ssize <<= 1;
    }
    // 我们这里先不要那么烧脑，用默认值，concurrencyLevel 为 16，sshift 为 4
    // 那么计算出 segmentShift 为 28，segmentMask 为 15，后面会用到这两个值
    this.segmentShift = 32 - sshift;
    this.segmentMask = ssize - 1;
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    // initialCapacity 是设置整个 map 初始的大小，
    // 这里根据 initialCapacity 计算 Segment 数组中每个位置可以分到的大小
    // 如 initialCapacity 为 64，那么每个 Segment 或称之为"槽"可以分到 4 个
    int c = initialCapacity / ssize;
    if (c * ssize < initialCapacity)
        ++c;
    // 默认 MIN_SEGMENT_TABLE_CAPACITY 是 2，这个值也是有讲究的，因为这样的话，对于具体的槽上，
    // 插入一个元素不至于扩容，插入第二个的时候才会扩容    
    int cap = MIN_SEGMENT_TABLE_CAPACITY;
    while (cap < c)
        cap <<= 1;
    // create segments and segments[0]
    Segment<K,V> s0 =
        new Segment<K,V>(loadFactor, (int)(cap * loadFactor),
                         (HashEntry<K,V>[])new HashEntry[cap]);
    Segment<K,V>[] ss = (Segment<K,V>[])new Segment[ssize];
    // ordered write of segments[0]
    UNSAFE.putOrderedObject(ss, SBASE, s0); 
    this.segments = ss;
}
```

初始化完成，我们得到了一个 Segment 数组。

我们就当是用 new ConcurrentHashMap() 无参构造函数进行初始化的，那么初始化完成后：

- Segment 数组长度为 16，不可以扩容
- Segment[i] 的默认大小为 2，负载因子是 0.75，得出初始阈值为 1.5，也就是以后插入第一个元素不会触发扩容，插入第二个会进行第一次扩容
- 这里初始化了 segment[0]，其他位置还是 null，至于为什么要初始化 segment[0]，后面的代码会介绍
- 当前 segmentShift 的值为 32 - 4 = 28，segmentMask 为 16 - 1 = 15，姑且把它们简单翻译为**移位数**和**掩码**，这两个值马上就会用到

### put 过程分析

```java
public V put(K key, V value) {
    Segment<K,V> s;
    if (value == null)
        throw new NullPointerException();
    // 1. 计算key的hash值
    int hash = hash(key);
    // 2. 根据hash值找到Segment数组中的位置j
    //  hash 是 32 位，无符号右移 segmentShift(28) 位，剩下高 4 位，
    //  然后和 segmentMask(15) 做一次与操作，也就是说 j 是 hash 值的高 4 位，也就是槽的数组下标
    int j = (hash >>> segmentShift) & segmentMask;
    // 刚刚说了，初始化的时候初始化了 segment[0]，但是其他位置还是 null，
    // ensureSegment(j) 对 segment[j] 进行初始化
    if ((s = (Segment<K,V>)UNSAFE.getObject          // nonvolatile; recheck
         (segments, (j << SSHIFT) + SBASE)) == null) //  in ensureSegment
        s = ensureSegment(j);
    // 3. 插入新值到槽 s 中
    return s.put(key, hash, value, false);
}
```

第一层皮很简单，根据 hash 值很快就能找到相应的 Segment，之后就是 Segment 内部的 put 操作了。

Segment 内部是由 **==数组+链表==** 组成的。

```java
final V put(K key, int hash, V value, boolean onlyIfAbsent) {
    // 在往该 segment 写入前，需要先获取该 segment 的独占锁
    HashEntry<K,V> node = tryLock() ? null :
    scanAndLockForPut(key, hash, value);
    V oldValue;
    try {
        // 这个是 segment 内部的数组
        HashEntry<K,V>[] tab = table;
        // 再利用 hash 值，求应该放置的数组下标
        int index = (tab.length - 1) & hash;
        // first 是数组该位置处的链表的表头
        HashEntry<K,V> first = entryAt(tab, index);
        // 下面这串for循环虽然很长，不过也很好理解，想想该位置没有任何元素和已经存在一个链表这两种情况
        for (HashEntry<K,V> e = first;;) {
            if (e != null) {
                K k;
                if ((k = e.key) == key ||
                    (e.hash == hash && key.equals(k))) {
                    oldValue = e.value;
                    if (!onlyIfAbsent) {
                        // 覆盖旧值
                        e.value = value;
                        ++modCount;
                    }
                    break;
                }
                // 继续顺着链表走
                e = e.next;
            }
            else {
                // node 到底是不是 null，这个要看获取锁的过程，不过和这里都没有关系。
                // 如果不为 null，那就直接将它设置为链表表头；如果是null，初始化并设置为链表表头。
                if (node != null)
                    node.setNext(first);
                else
                    node = new HashEntry<K,V>(hash, key, value, first);
                int c = count + 1;
                // 如果超过了该 segment 的阈值，这个 segment 需要扩容
                if (c > threshold && tab.length < MAXIMUM_CAPACITY)
                    rehash(node);
                else
                    // 没有达到阈值，将 node 放到数组 tab 的 index 位置，
                    // 其实就是将新的节点设置成原链表的表头
                    setEntryAt(tab, index, node);
                ++modCount;
                count = c;
                oldValue = null;
                break;
            }
        }
    } finally {
        unlock();
    }
    return oldValue;
}
```

**初始化槽: ensureSegment**

ConcurrentHashMap 初始化的时候会初始化第一个槽 segment[0]，对于其他槽来说，在插入第一个值的时候进行初始化。

这里需要考虑并发，因为很可能会有多个线程同时进来初始化同一个槽 segment[k]，不过只要有一个成功了就可以。

```java
private Segment<K,V> ensureSegment(int k) {
    final Segment<K,V>[] ss = this.segments;
    long u = (k << SSHIFT) + SBASE; // raw offset
    Segment<K,V> seg;
    if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u)) == null) {
        // 这里看到为什么之前要初始化 segment[0] 了，
        // 使用当前 segment[0] 处的数组长度和负载因子来初始化 segment[k]
        // 为什么要用“当前”，因为 segment[0] 可能早就扩容过了
        Segment<K,V> proto = ss[0]; // use segment 0 as prototype
        int cap = proto.table.length;
        float lf = proto.loadFactor;
        int threshold = (int)(cap * lf);
        // 初始化 segment[k] 内部的数组
        HashEntry<K,V>[] tab = (HashEntry<K,V>[])new HashEntry[cap];
        if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u))
            == null) { // recheck该槽是否被其他线程初始化了。
            Segment<K,V> s = new Segment<K,V>(lf, threshold, tab);
            // 使用 while 循环，内部用 CAS，当前线程成功设值或其他线程成功设值后，退出
            while ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u))
                   == null) {
                if (UNSAFE.compareAndSwapObject(ss, u, null, seg = s))
                    break;
            }
        }
    }
    return seg;
}
```

总的来说，ensureSegment(int k) 比较简单，对于并发操作使用 CAS 进行控制。

如果当前线程 CAS 失败，这里的 while 循环是为了将 seg 赋值返回。

**获取写入锁: scanAndLockForPut**

​		前面我们看到，在往某个 segment 中 put 的时候，首先会调用  node = tryLock() ? null : scanAndLockForPut(key, hash, value)，也就是说先进行一次 tryLock() 快速获取该 segment 的独占锁，如果失败，那么进入到 scanAndLockForPut 这个方法来获取锁。

```java
private HashEntry<K,V> scanAndLockForPut(K key, int hash, V value) {
    HashEntry<K,V> first = entryForHash(this, hash);
    HashEntry<K,V> e = first;
    HashEntry<K,V> node = null;
    int retries = -1; // negative while locating node
    
    // 循环获取锁
    while (!tryLock()) {
        HashEntry<K,V> f; // to recheck first below
        if (retries < 0) {
            if (e == null) {
                if (node == null) // speculatively create node 推测性地创建节点
                    // 进到这里说明数组该位置的链表是空的，没有任何元素
                    // 当然，进到这里的另一个原因是 tryLock() 失败，所以该槽存在并发，不一定是该位置
                    node = new HashEntry<K,V>(hash, key, value, null);
                retries = 0;
            }
            else if (key.equals(e.key))
                retries = 0;
            else
                // 顺着链表往下走
                e = e.next;
        }
        // 重试次数如果超过 MAX_SCAN_RETRIES（单核1多核64），那么不抢了，进入到阻塞队列等待锁
        // lock() 是阻塞方法，直到获取锁后返回
        else if (++retries > MAX_SCAN_RETRIES) {
            lock();
            break;
        }
        // 这个时候是有大问题了，那就是有新的元素进到了链表，成为了新的表头
  		// 所以这边的策略是，相当于重新走一遍这个 scanAndLockForPut 方法
        else if ((retries & 1) == 0 &&
                 (f = entryForHash(this, hash)) != first) {
            e = first = f; // re-traverse if entry changed
            retries = -1;
        }
    }
    return node;
}
```

这个方法有两个出口，一个是 tryLock() 成功了，循环终止，另一个就是重试次数超过了 MAX_SCAN_RETRIES，进到 lock() 方法，此方法会阻塞等待，直到成功拿到独占锁。

这个方法就是看似复杂，但是其实就是做了一件事，那就是**获取该 segment 的独占锁**，如果需要的话顺便实例化了一下 node。

**扩容：rehash**

​		重复一下，segment 数组不能扩容，扩容是 segment 数组某个位置内部的数组 HashEntry\<K,V>[] 进行扩容，扩容后，容量为原来的 2 倍。

​		首先，我们要回顾一下触发扩容的地方，put 的时候，如果判断该值的插入会导致该 segment 的元素个数超过阈值，那么先进行扩容，再插值，读者这个时候可以回去 put 方法看一眼。

​		该方法不需要考虑并发，因为到这里的时候，是持有该 segment 的独占锁的。

```java
// 方法参数上的 node 是这次扩容后，需要添加到新的数组中的数据。
private void rehash(HashEntry<K,V> node) {
    HashEntry<K,V>[] oldTable = table;
    int oldCapacity = oldTable.length;
    // 2 倍
    int newCapacity = oldCapacity << 1;
    threshold = (int)(newCapacity * loadFactor);
    // 创建新数组
    HashEntry<K,V>[] newTable =
        (HashEntry<K,V>[]) new HashEntry[newCapacity];
    // 新的掩码，如从16扩容到32，那么sizeMask为 31，对应二进制 ‘000...0001 1111’
    int sizeMask = newCapacity - 1;
    // 遍历原数组，老套路，将原数组位置 i处的链表拆分到 新数组位置 i 和 i+oldCap 两个位置
    for (int i = 0; i < oldCapacity ; i++) {
        // e 是链表的第一个元素
        HashEntry<K,V> e = oldTable[i];
        if (e != null) {
            HashEntry<K,V> next = e.next;
            // 计算应该放置在新数组中的位置，
            // 假设原数组长度为 16，e 在 oldTable[3] 处，那么 idx 只可能是3或者是3 + 16 = 19
            int idx = e.hash & sizeMask;
            if (next == null)   //  Single node on list
                newTable[idx] = e;
            else { // Reuse consecutive sequence at same slot
                // e 是链表表头
                HashEntry<K,V> lastRun = e;
                // idx 是当前链表的头结点 e 的新位置
                int lastIdx = idx;
                // 下面这个 for 循环会找到一个 lastRun 节点，这个节点之后的所有元素是将要放到一起的
                for (HashEntry<K,V> last = next;
                     last != null;
                     last = last.next) {
                    int k = last.hash & sizeMask;
                    if (k != lastIdx) {
                        lastIdx = k;
                        lastRun = last;
                    }
                }
                // 将 lastRun 及其之后的所有节点组成的这个链表放到 lastIdx 这个位置
                newTable[lastIdx] = lastRun;
                // Clone remaining nodes
                // 下面的操作是处理 lastRun 之前的节点，
                // 这些节点可能分配在另一个链表中，也可能分配到上面的那个链表中
                for (HashEntry<K,V> p = e; p != lastRun; p = p.next) {
                    V v = p.value;
                    int h = p.hash;
                    int k = h & sizeMask;
                    HashEntry<K,V> n = newTable[k];
                    newTable[k] = new HashEntry<K,V>(h, p.key, v, n);
                }
            }
        }
    }
    // 将新来的 node 放到新数组中刚刚的 两个链表之一 的 头部
    int nodeIndex = node.hash & sizeMask; // add the new node
    node.setNext(newTable[nodeIndex]);
    newTable[nodeIndex] = node;
    table = newTable;
}
```

​		这里的扩容比之前的 HashMap 要复杂一些，代码难懂一点。上面有两个挨着的 for 循环，第一个 for 有什么用呢？

​		仔细一看发现，如果没有第一个 for 循环，也是可以工作的，但是，这个 for 循环下来，如果 lastRun 的后面还有比较多的节点，那么这次就是值得的。因为我们只需要克隆 lastRun 前面的节点，后面的一串节点跟着 lastRun 走就是了，不需要做任何操作。

​		我觉得 Doug Lea 的这个想法也是挺有意思的，不过比较坏的情况就是每次 lastRun 都是链表的最后一个元素或者很靠后的元素，那么这次遍历就有点浪费了。**不过 Doug Lea 也说了，根据统计，如果使用默认的阈值，大约只有 1/6 的节点需要克隆**。



### get 过程分析

相对于 put 来说，get 真的不要太简单。

1. 计算 hash 值，找到 segment 数组中的具体位置，或我们前面用的“槽”
2. 槽中也是一个数组，根据 hash 找到数组中具体的位置
3. 到这里是链表了，顺着链表进行查找即可

```java
public V get(Object key) {
    Segment<K,V> s; // manually integrate access methods to reduce overhead
    HashEntry<K,V>[] tab;
    // 1. hash 值
    int h = hash(key);
    long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
    // 2. 根据 hash 找到对应的 segment
    if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null &&
        (tab = s.table) != null) {
        // 3. 找到segment 内部数组相应位置的链表，遍历
        for (HashEntry<K,V> e = (HashEntry<K,V>) UNSAFE.getObjectVolatile
                 (tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE);
             e != null; e = e.next) {
            K k;
            if ((k = e.key) == key || (e.hash == h && key.equals(k)))
                return e.value;
        }
    }
    return null;
}
```



### 并发问题分析

现在我们已经说完了 put 过程和 get 过程，我们可以看到 get 过程中是没有加锁的，那自然我们就需要去考虑并发问题。

添加节点的操作 put 和删除节点的操作 remove 都是要加 segment 上的独占锁的，所以它们之间自然不会有问题，我们需要考虑的问题就是 get 的时候在同一个 segment 中发生了 put 或 remove 操作。

- put 操作的线程安全性。
  - 初始化槽，这个我们之前就说过了，使用了 CAS 来初始化 Segment 中的数组。
  - 添加节点到链表的操作是插入到表头的，所以，如果这个时候 get 操作在链表遍历的过程已经到了中间，是不会影响的。当然，另一个并发问题就是 get 操作在 put 之后，需要保证刚刚插入表头的节点被读取，这个依赖于 setEntryAt 方法中使用的 UNSAFE.putOrderedObject。
  - 扩容。扩容是新创建了数组，然后进行迁移数据，最后面将 newTable 设置给属性 table。所以，如果 get 操作此时也在进行，那么也没关系，如果 get 先行，那么就是在旧的 table 上做查询操作；而 put 先行，那么 put 操作的可见性保证就是 table 使用了 volatile 关键字。

- remove 操作的线程安全性。

  remove 操作我们没有分析源码，所以这里说的读者感兴趣的话还是需要到源码中去求实一下的。

  get 操作需要遍历链表，但是 remove 操作会"破坏"链表。

  如果 remove 破坏的节点 get 操作已经过去了，那么这里不存在任何问题。

  如果 remove 先破坏了一个节点，分两种情况考虑。  1、如果此节点是头结点，那么需要将头结点的 next 设置为数组该位置的元素，table 虽然使用了 volatile 修饰，但是 volatile 并不能提供数组内部操作的可见性保证，所以源码中使用了 UNSAFE 来操作数组，请看方法 setEntryAt。2、如果要删除的节点不是头结点，它会将要删除节点的后继节点接到前驱节点中，这里的并发保证就是 next 属性是 volatile 的。



## JDK8 HashMap

![img](images\jdk8_HashMap.png)

​		Java8 对 HashMap 进行了一些修改，最大的不同就是利用了红黑树，所以其由 ==**数组+链表+红黑树**== 组成。

​		根据 Java7 HashMap 的介绍，我们知道，查找的时候，根据 hash 值我们能够快速定位到数组的具体下标，但是之后的话，需要顺着链表一个个比较下去才能找到我们需要的，时间复杂度取决于链表的长度，为 **O(n)**。

​		为了降低这部分的开销，在 Java8 中，当链表中的元素达到了 8 个时，会将链表转换为红黑树，在这些位置进行查找的时候可以降低时间复杂度为 **O(logN)**。

​		Java7 中使用 Entry 来代表每个 HashMap 中的数据节点，Java8 中使用 **Node**，基本没有区别，都是 key，value，hash 和 next 这四个属性，不过，Node 只能用于链表的情况，红黑树的情况需要使用 **TreeNode**。

​		我们根据数组元素中，第一个节点数据类型是 Node 还是 TreeNode 来判断该位置下是链表还是红黑树的。

### put 过程分析

put操作主要如下：

1. 哈希桶数组 table 为空时，通过 resize() 方法进行初始化
2. 待插入的 key 已存在，直接覆盖 value
3. 若不存在，将键值对插入到对应的链表或红黑树中
4. 插入链表时判断是否转红黑树
5. 判断是否需要扩容

```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

// 第四个参数 onlyIfAbsent 如果是 true，那么只有在不存在该 key 时才会进行 put 操作
// 第五个参数 evict 我们这里不关心
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // 第一次 put 值的时候，会触发下面的 resize()，类似 java7 的第一次 put 也要初始化数组长度
    // 第一次 resize 和后续的扩容有些不一样，因为这次是数组从 null 初始化到默认的 16 或自定义的初始容量
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    // 找到具体的数组下标，如果此位置没有值，那么直接初始化一下 Node 并放置在这个位置就可以了
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);

    else {// 数组该位置有数据
        Node<K,V> e; K k;
        // 首先，判断该位置的第一个数据和我们要插入的数据，key 是不是"相等"，如果是，取出这个节点
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        // 如果该节点是代表红黑树的节点，调用红黑树的插值方法，本文不展开说红黑树
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            // 到这里，说明数组该位置上是一个链表
            for (int binCount = 0; ; ++binCount) {
                // 插入到链表的最后面(Java7 是插入到链表的最前面)
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    // TREEIFY_THRESHOLD 为 8，所以，如果新插入的值是链表中的第 8 个
                    // 会触发下面的 treeifyBin，也就是将链表转换为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                // 如果在该链表中找到了"相等"的 key(== 或 equals)
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    // 此时 break，那么 e 为链表中[与要插入的新值的 key "相等"]的 node
                    break;
                p = e;
            }
        }
        // e!=null 说明存在旧值的key与要插入的key"相等"
        // 对于我们分析的put操作，下面这个 if 其实就是进行 "值覆盖"，然后返回旧值
        if (e != null) {	// existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    // 如果 HashMap 由于新插入这个值导致 size 已经超过了阈值，需要进行扩容
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

```java
static final int hash(Object key) {
    int h;
    // null key存放在tab[0]处
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}

// Returns a power of two size for the given target capacity.
// 指定容量，返回2的幂
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}

final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; Node<K,V> e;
    // 数组为空或数组大小小于64时会先扩容；否则才会转红黑树
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
        resize();
    else if ((e = tab[index = (n - 1) & hash]) != null) {
        TreeNode<K,V> hd = null, tl = null;
        do {
            TreeNode<K,V> p = replacementTreeNode(e, null);
            if (tl == null)
                hd = p;
            else {
                p.prev = tl;
                tl.next = p;
            }
            tl = p;
        } while ((e = e.next) != null);
        if ((tab[index] = hd) != null)
            hd.treeify(tab);
    }
}
```

​		和 Java7 稍微有点不一样的地方就是，Java7 是先扩容后插入新值的，Java8 先插值再扩容，不过这个不重要。

> **数组扩容**

resize() 方法用于**初始化数组**或**数组扩容**，每次扩容后，容量为原来的` 2 `倍，并进行数据迁移。

```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) { // 对应数组扩容
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        // 将数组大小扩大一倍
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            // 将阈值扩大一倍
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // 对应使用 new HashMap(int initialCapacity) 初始化后，第一次 put 的时候
        newCap = oldThr;
    else {// 对应使用 new HashMap() 初始化后，第一次 put 的时候
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }

    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;

    // 用新的数组大小初始化新的数组
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab; // 如果是初始化数组，到这里就结束了，返回 newTab 即可

    if (oldTab != null) {
        // 开始遍历原数组，进行数据迁移。
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                // 如果该数组位置上只有单个元素，那就简单了，简单迁移这个元素就可以了
                if (e.next == null)
                    newTab[e.hash & (newCap - 1)] = e;
                // 如果是红黑树，具体我们就不展开了
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { 
                    // 这块是处理链表的情况，preserve order
                    // 需要将此链表拆成两个链表，放到新的数组中，并且保留原来的先后顺序
                    // loHead、loTail 对应一条链表，hiHead、hiTail 对应另一条链表
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        // 原位置不变的子链表
                        if ((e.hash & oldCap) == 0) {                           
                            if (loTail == null)
                                loHead = e;	// 第一次进来时给链头赋值
                            else
                                loTail.next = e;	 // 给链尾赋值
                            loTail = e;	 // 重置该变量
                        }
                        // 原位置偏移 oldCap 的子链表
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        // 第一条链表
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        // 第二条链表的新的位置是 j + oldCap，这个很好理解
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

> 红黑树分析

```java
//TODO: 红黑树研究
```

==**自平衡二叉查找树**==

​		时间复杂度 **O(log n)**

![img](images\red black tree.jpg)

==性质：==

1. **每个节点要么是黑色，要么是红色。**
2. **根节点是黑色。**
3. **每个叶子节点（NIL）是黑色。 [注意：这里叶子节点，是指为空(NIL或NULL)的叶子节点！]**
4. **每个红色节点的两个子节点都是黑色。（从每个叶子到根的所有路径上不能有两个连续的红色节点）**
5. **从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点。**
   1. 如果一个结点存在黑子结点，那么该结点肯定有两个子结点



==三种操作：左旋、右旋和变色。==

- **左旋**：以某个结点作为支点(旋转结点)，其右子结点变为旋转结点的父结点，右子结点的左子结点变为旋转结点的右子结点，左子结点保持不变。如图3。
- **右旋**：以某个结点作为支点(旋转结点)，其左子结点变为旋转结点的父结点，左子结点的右子结点变为旋转结点的左子结点，右子结点保持不变。如图4。
- **变色**：结点的颜色由红变黑或由黑变红。





### get 过程分析

相对于 put 来说，get 真的太简单了。

1. 计算 key 的 hash 值，根据 hash 值找到对应数组下标: hash & (length-1)
2. 判断数组该位置处的元素是否刚好就是我们要找的，如果不是，走第三步
3. 判断该元素类型是否是 TreeNode，如果是，用红黑树的方法取数据，如果不是，走第四步
4. 遍历链表，直到找到相等(==或equals)的 key

```java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}
```

```java
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        // 判断第一个节点是不是就是需要的
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        if ((e = first.next) != null) {
            // 判断是否是红黑树
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);

            // 链表遍历
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```





## JDK8 ConcurrentHahMap

![image-20201122173120097](images\jdk8_ConcurrentHashMap.png)



**Java8 ConcurrentHashMap 源码真心不简单，最难的在于扩容，数据迁移操作不容易看懂。**

### 初始化

```java
// 这构造函数里，什么都不干
public ConcurrentHashMap() {
}
```

```java
public ConcurrentHashMap(int initialCapacity) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException();
    int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
               MAXIMUM_CAPACITY :
               tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
    this.sizeCtl = cap;
}
```

​		这个初始化方法有点意思，通过提供初始容量，计算了 sizeCtl，sizeCtl = 【 (1.5 * initialCapacity + 1)，然后向上取最近的 2 的 n 次方】。如 initialCapacity 为 10，那么得到 sizeCtl 为 16，如果 initialCapacity 为 11，得到 sizeCtl 为 32。

### put 过程分析

```java
public V put(K key, V value) {
    return putVal(key, value, false);
}
```

```java
final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key == null || value == null) throw new NullPointerException();
    // 得到 hash 值
    int hash = spread(key.hashCode());
    // 用于记录相应链表的长度
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        // 如果数组"空"，进行数组初始化
        if (tab == null || (n = tab.length) == 0)
            // 初始化数组，后面会详细介绍
            tab = initTable();

        // 找该 hash 值对应的数组下标，得到第一个节点 f
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            // 如果数组该位置为空，
            //    用一次 CAS 操作将这个新值放入其中即可，这个 put 操作差不多就结束了，可以拉到最后面了
            //          如果 CAS 失败，那就是有并发操作，进到下一个循环就好了
            if (casTabAt(tab, i, null,
                         new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        // hash 居然可以等于 MOVED，这个需要到后面才能看明白，不过从名字上也能猜到，肯定是因为在扩容
        else if ((fh = f.hash) == MOVED)
            // 帮助数据迁移，这个等到看完数据迁移部分的介绍后，再理解这个就很简单了
            tab = helpTransfer(tab, f);

        else { // 到这里就是说，f 是该位置的头结点，而且不为空

            V oldVal = null;
            // 获取数组该位置的头结点的监视器锁
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    if (fh >= 0) { // 头结点的 hash 值大于 0，说明是链表
                        // 用于累加，记录链表的长度
                        binCount = 1;
                        // 遍历链表
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            // 如果发现了"相等"的 key，判断是否要进行值覆盖，然后也就可以 break 了
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            // 到了链表的最末端，将这个新值放到链表的最后面
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    else if (f instanceof TreeBin) { // 红黑树
                        Node<K,V> p;
                        binCount = 2;
                        // 调用红黑树的插值方法插入新节点
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }

            if (binCount != 0) {
                // 判断是否要将链表转换为红黑树，临界值和 HashMap 一样，也是 8
                if (binCount >= TREEIFY_THRESHOLD)
                    // 这个方法和 HashMap 中稍微有一点点不同，那就是它不是一定会进行红黑树转换，
                    // 如果当前数组的长度小于 64，那么会选择进行数组扩容，而不是转换为红黑树
                    //    具体源码我们就不看了，扩容部分后面说
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    // 
    addCount(1L, binCount);
    return null;
}
```

put 的主流程看完了，但是至少留下了几个问题，第一个是初始化，第二个是扩容，第三个是帮助数据迁移

> 初始化数组：initTable

​		这个比较简单，主要就是初始化一个**合适大小**的数组，然后会设置 sizeCtl。

​		初始化方法中的并发问题是通过对 sizeCtl 进行一个 CAS 操作来控制的。

```java
private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    while ((tab = table) == null || tab.length == 0) {
        // 初始化的"功劳"被其他线程"抢去"了
        if ((sc = sizeCtl) < 0)
            Thread.yield(); // lost initialization race; just spin
        // CAS 一下，将 sizeCtl 设置为 -1，代表抢到了锁
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            try {
                if ((tab = table) == null || tab.length == 0) {
                    // DEFAULT_CAPACITY 默认初始容量是 16
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    // 初始化数组，长度为 16 或初始化时提供的长度
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    // 将这个数组赋值给 table，table 是 volatile 的
                    table = tab = nt;
                    // 如果 n 为 16 的话，那么这里 sc = 12
                    // 其实就是 0.75 * n
                    sc = n - (n >>> 2);
                }
            } finally {
                // 设置 sizeCtl 为 sc，我们就当是 12 吧
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}
```

> 链表转红黑树: treeifyBin

前面我们在 put 源码分析也说过，treeifyBin 不一定就会进行红黑树转换，也可能是仅仅做数组扩容。

```java
private final void treeifyBin(Node<K,V>[] tab, int index) {
    Node<K,V> b; int n, sc;
    if (tab != null) {
        // MIN_TREEIFY_CAPACITY 为 64
        // 所以，如果数组长度小于 64 的时候，其实也就是 32 或者 16 或者更小的时候，会进行数组扩容
        if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
            // 后面我们再详细分析这个方法
            tryPresize(n << 1);
        // b 是头结点
        else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
            // 加锁
            synchronized (b) {

                if (tabAt(tab, index) == b) {
                    // 下面就是遍历链表，建立一颗红黑树
                    TreeNode<K,V> hd = null, tl = null;
                    for (Node<K,V> e = b; e != null; e = e.next) {
                        TreeNode<K,V> p =
                            new TreeNode<K,V>(e.hash, e.key, e.val,
                                              null, null);
                        if ((p.prev = tl) == null)
                            hd = p;
                        else
                            tl.next = p;
                        tl = p;
                    }
                    // 将红黑树设置到数组相应位置中
                    setTabAt(tab, index, new TreeBin<K,V>(hd));
                }
            }
        }
    }
}
```

> 扩容：tryPresize

​		如果说 Java8 ConcurrentHashMap 的源码不简单，那么说的就是扩容操作和迁移操作。

​		这个方法要完完全全看懂还需要看之后的 transfer 方法，读者应该提前知道这点。

​		这里的扩容也是做翻倍扩容的，扩容后数组容量为原来的 2 倍。

```java
// 首先要说明的是，方法参数 size 传进来的时候就已经翻了倍了
private final void tryPresize(int size) {
    // c：size 的 1.5 倍，再加 1，再往上取最近的 2 的 n 次方。
    int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY :
        tableSizeFor(size + (size >>> 1) + 1);
    int sc;
    while ((sc = sizeCtl) >= 0) {
        Node<K,V>[] tab = table; int n;

        // 这个 if 分支和之前说的初始化数组的代码基本上是一样的，在这里，我们可以不用管这块代码
        if (tab == null || (n = tab.length) == 0) {
            n = (sc > c) ? sc : c;
            if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if (table == tab) {
                        @SuppressWarnings("unchecked")
                        Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                        table = nt;
                        sc = n - (n >>> 2); // 0.75 * n
                    }
                } finally {
                    sizeCtl = sc;
                }
            }
        }
        else if (c <= sc || n >= MAXIMUM_CAPACITY)
            break;
        else if (tab == table) {
            // 我没看懂 rs 的真正含义是什么，不过也关系不大
            int rs = resizeStamp(n);

            if (sc < 0) {
                Node<K,V>[] nt;
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                    transferIndex <= 0)
                    break;
                // 2. 用 CAS 将 sizeCtl 加 1，然后执行 transfer 方法
                //    此时 nextTab 不为 null
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }
            // 1. 将 sizeCtl 设置为 (rs << RESIZE_STAMP_SHIFT) + 2)
            //     我是没看懂这个值真正的意义是什么？不过可以计算出来的是，结果是一个比较大的负数
            //  调用 transfer 方法，此时 nextTab 参数为 null
            else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                         (rs << RESIZE_STAMP_SHIFT) + 2))
                transfer(tab, null);
        }
    }
}
```

​		这个方法的核心在于 sizeCtl 值的操作，首先将其设置为一个负数，然后执行 transfer(tab, null)，再下一个循环将 sizeCtl 加 1，并执行 transfer(tab, nt)，之后可能是继续 sizeCtl 加 1，并执行 transfer(tab, nt)。

​		所以，可能的操作就是执行 **1 次 transfer(tab, null) + 多次 transfer(tab, nt)**，这里怎么结束循环的需要看完 transfer 源码才清楚。

> 数据迁移：transfer

下面这个方法有点长，将原来的 tab 数组的元素迁移到新的 nextTab 数组中。

虽然我们之前说的 tryPresize 方法中多次调用 transfer 不涉及多线程，但是这个 transfer 方法可以在其他地方被调用，典型地，我们之前在说 put 方法的时候就说过了，请往上看 put 方法，是不是有个地方调用了 helpTransfer 方法，helpTransfer 方法会调用 transfer 方法的。

此方法支持多线程执行，外围调用此方法的时候，会保证第一个发起数据迁移的线程，nextTab 参数为 null，之后再调用此方法的时候，nextTab 不会为 null。

阅读源码之前，先要理解并发操作的机制。原数组长度为 n，所以我们有 n 个迁移任务，让每个线程每次负责一个小任务是最简单的，每做完一个任务再检测是否有其他没做完的任务，帮助迁移就可以了，而 Doug Lea 使用了一个 stride，简单理解就是**步长**，每个线程每次负责迁移其中的一部分，如每次迁移 16 个小任务。所以，我们就需要一个全局的调度者来安排哪个线程执行哪几个任务，这个就是属性 transferIndex 的作用。

第一个发起数据迁移的线程会将 transferIndex 指向原数组最后的位置，然后**从后往前**的 stride 个任务属于第一个线程，然后将 transferIndex 指向新的位置，再往前的 stride 个任务属于第二个线程，依此类推。当然，这里说的第二个线程不是真的一定指代了第二个线程，也可以是同一个线程，这个读者应该能理解吧。其实就是将一个大的迁移任务分为了一个个任务包。

```java
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
    int n = tab.length, stride;

    // stride 在单核下直接等于 n，多核模式下为 (n>>>3)/NCPU，最小值是 16
    // stride 可以理解为”步长“，有 n 个位置是需要进行迁移的，
    //   将这 n 个任务分为多个任务包，每个任务包有 stride 个任务
    if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
        stride = MIN_TRANSFER_STRIDE; // subdivide range

    // 如果 nextTab 为 null，先进行一次初始化
    //    前面我们说了，外围会保证第一个发起迁移的线程调用此方法时，参数 nextTab 为 null
    //       之后参与迁移的线程调用此方法时，nextTab 不会为 null
    if (nextTab == null) {
        try {
            // 容量翻倍
            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
            nextTab = nt;
        } catch (Throwable ex) {      // try to cope with OOME
            sizeCtl = Integer.MAX_VALUE;
            return;
        }
        // nextTable 是 ConcurrentHashMap 中的属性
        nextTable = nextTab;
        // transferIndex 也是 ConcurrentHashMap 的属性，用于控制迁移的位置
        transferIndex = n;
    }

    int nextn = nextTab.length;

    // ForwardingNode 翻译过来就是正在被迁移的 Node
    // 这个构造方法会生成一个Node，key、value 和 next 都为 null，关键是 hash 为 MOVED
    // 后面我们会看到，原数组中位置 i 处的节点完成迁移工作后，
    //    就会将位置 i 处设置为这个 ForwardingNode，用来告诉其他线程该位置已经处理过了
    //    所以它其实相当于是一个标志。
    ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);


    // advance 指的是做完了一个位置的迁移工作，可以准备做下一个位置的了
    boolean advance = true;
    boolean finishing = false; // to ensure sweep before committing nextTab

    /*
     * 下面这个 for 循环，最难理解的在前面，而要看懂它们，应该先看懂后面的，然后再倒回来看
     * 
     */

    // i 是位置索引，bound 是边界，注意是从后往前
    for (int i = 0, bound = 0;;) {
        Node<K,V> f; int fh;

        // 下面这个 while 真的是不好理解
        // advance 为 true 表示可以进行下一个位置的迁移了
        //   简单理解结局：i 指向了 transferIndex，bound 指向了 transferIndex-stride
        while (advance) {
            int nextIndex, nextBound;
            if (--i >= bound || finishing)
                advance = false;

            // 将 transferIndex 值赋给 nextIndex
            // 这里 transferIndex 一旦小于等于 0，说明原数组的所有位置都有相应的线程去处理了
            else if ((nextIndex = transferIndex) <= 0) {
                i = -1;
                advance = false;
            }
            else if (U.compareAndSwapInt
                     (this, TRANSFERINDEX, nextIndex,
                      nextBound = (nextIndex > stride ?
                                   nextIndex - stride : 0))) {
                // 看括号中的代码，nextBound 是这次迁移任务的边界，注意，是从后往前
                bound = nextBound;
                i = nextIndex - 1;
                advance = false;
            }
        }
        if (i < 0 || i >= n || i + n >= nextn) {
            int sc;
            if (finishing) {
                // 所有的迁移操作已经完成
                nextTable = null;
                // 将新的 nextTab 赋值给 table 属性，完成迁移
                table = nextTab;
                // 重新计算 sizeCtl：n 是原数组长度，所以 sizeCtl 得出的值将是新数组长度的 0.75 倍
                sizeCtl = (n << 1) - (n >>> 1);
                return;
            }

            // 之前我们说过，sizeCtl 在迁移前会设置为 (rs << RESIZE_STAMP_SHIFT) + 2
            // 然后，每有一个线程参与迁移就会将 sizeCtl 加 1，
            // 这里使用 CAS 操作对 sizeCtl 进行减 1，代表做完了属于自己的任务
            if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                // 任务结束，方法退出
                if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                    return;

                // 到这里，说明 (sc - 2) == resizeStamp(n) << RESIZE_STAMP_SHIFT，
                // 也就是说，所有的迁移任务都做完了，也就会进入到上面的 if(finishing){} 分支了
                finishing = advance = true;
                i = n; // recheck before commit
            }
        }
        // 如果位置 i 处是空的，没有任何节点，那么放入刚刚初始化的 ForwardingNode ”空节点“
        else if ((f = tabAt(tab, i)) == null)
            advance = casTabAt(tab, i, null, fwd);
        // 该位置处是一个 ForwardingNode，代表该位置已经迁移过了
        else if ((fh = f.hash) == MOVED)
            advance = true; // already processed
        else {
            // 对数组该位置处的结点加锁，开始处理数组该位置处的迁移工作
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    Node<K,V> ln, hn;
                    // 头结点的 hash 大于 0，说明是链表的 Node 节点
                    if (fh >= 0) {
                        // 下面这一块和 Java7 中的 ConcurrentHashMap 迁移是差不多的，
                        // 需要将链表一分为二，
                        //   找到原链表中的 lastRun，然后 lastRun 及其之后的节点是一起进行迁移的
                        //   lastRun 之前的节点需要进行克隆，然后分到两个链表中
                        int runBit = fh & n;
                        Node<K,V> lastRun = f;
                        for (Node<K,V> p = f.next; p != null; p = p.next) {
                            int b = p.hash & n;
                            if (b != runBit) {
                                runBit = b;
                                lastRun = p;
                            }
                        }
                        if (runBit == 0) {
                            ln = lastRun;
                            hn = null;
                        }
                        else {
                            hn = lastRun;
                            ln = null;
                        }
                        for (Node<K,V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash; K pk = p.key; V pv = p.val;
                            if ((ph & n) == 0)
                                ln = new Node<K,V>(ph, pk, pv, ln);
                            else
                                hn = new Node<K,V>(ph, pk, pv, hn);
                        }
                        // 其中的一个链表放在新数组的位置 i
                        setTabAt(nextTab, i, ln);
                        // 另一个链表放在新数组的位置 i+n
                        setTabAt(nextTab, i + n, hn);
                        // 将原数组该位置处设置为 fwd，代表该位置已经处理完毕，
                        //    其他线程一旦看到该位置的 hash 值为 MOVED，就不会进行迁移了
                        setTabAt(tab, i, fwd);
                        // advance 设置为 true，代表该位置已经迁移完毕
                        advance = true;
                    }
                    else if (f instanceof TreeBin) {
                        // 红黑树的迁移
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> lo = null, loTail = null;
                        TreeNode<K,V> hi = null, hiTail = null;
                        int lc = 0, hc = 0;
                        for (Node<K,V> e = t.first; e != null; e = e.next) {
                            int h = e.hash;
                            TreeNode<K,V> p = new TreeNode<K,V>
                                (h, e.key, e.val, null, null);
                            if ((h & n) == 0) {
                                if ((p.prev = loTail) == null)
                                    lo = p;
                                else
                                    loTail.next = p;
                                loTail = p;
                                ++lc;
                            }
                            else {
                                if ((p.prev = hiTail) == null)
                                    hi = p;
                                else
                                    hiTail.next = p;
                                hiTail = p;
                                ++hc;
                            }
                        }
                        // 如果一分为二后，节点数少于 8，那么将红黑树转换回链表
                        ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                            (hc != 0) ? new TreeBin<K,V>(lo) : t;
                        hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                            (lc != 0) ? new TreeBin<K,V>(hi) : t;

                        // 将 ln 放置在新数组的位置 i
                        setTabAt(nextTab, i, ln);
                        // 将 hn 放置在新数组的位置 i+n
                        setTabAt(nextTab, i + n, hn);
                        // 将原数组该位置处设置为 fwd，代表该位置已经处理完毕，
                        //    其他线程一旦看到该位置的 hash 值为 MOVED，就不会进行迁移了
                        setTabAt(tab, i, fwd);
                        // advance 设置为 true，代表该位置已经迁移完毕
                        advance = true;
                    }
                }
            }
        }
    }
}
```

说到底，transfer 这个方法并没有实现所有的迁移任务，每次调用这个方法只实现了 transferIndex 往前 stride 个位置的迁移工作，其他的需要由外围来控制。

这个时候，再回去仔细看 tryPresize 方法可能就会更加清晰一些了。



### get 过程分析

get 方法从来都是最简单的，这里也不例外：

1. 计算 hash 值
2. 根据 hash 值找到数组对应位置: (n - 1) & h
3. 根据该位置处结点性质进行相应查找
   - 如果该位置为 null，那么直接返回 null 就可以了
   - 如果该位置处的节点刚好就是我们需要的，返回该节点的值即可
   - 如果该位置节点的 hash 值小于 0，说明正在扩容，或者是红黑树，后面我们再介绍 find 方法
   - 如果以上 3 条都不满足，那就是链表，进行遍历比对即可

```java
public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
    int h = spread(key.hashCode());
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (e = tabAt(tab, (n - 1) & h)) != null) {
        // 判断头结点是否就是我们需要的节点
        if ((eh = e.hash) == h) {
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                return e.val;
        }
        // 如果头结点的 hash 小于 0，说明 正在扩容，或者该位置是红黑树
        else if (eh < 0)
            // 参考 ForwardingNode.find(int h, Object k) 和 TreeBin.find(int h, Object k)
            return (p = e.find(h, key)) != null ? p.val : null;

        // 遍历链表
        while ((e = e.next) != null) {
            if (e.hash == h &&
                ((ek = e.key) == key || (ek != null && key.equals(ek))))
                return e.val;
        }
    }
    return null;
}
```

​		简单说一句，此方法的大部分内容都很简单，只有正好碰到扩容的情况，ForwardingNode.find(int h, Object k) 稍微复杂一些，不过在了解了数据迁移的过程后，这个也就不难了，所以限于篇幅这里也不展开说了。











# Concurrent



## 基础

**进程是操作系统进行资源分配的基本单位，**

**线程是操作系统进行调度的基本单位**。



**强引用(FinalReference)**	

**软引用(SoftReference)**	堆空间不足，gc		用作缓存

**弱引用(WeakReference)**	gc发现就回收	threadlocal

**虚引用(PhantomReference)**	跟踪垃圾回收过程



## Java对象头

- **对象头（ Header ）**
  - **MarkWord**
  - **指向类的指针**
  - **数组长度（只有数组对象才有）**
- **实例数据（ Instance Data ）**
- **对齐填充（ Padding ）**



Mark Word在32位JVM中的长度是32bit，在64位JVM（**未开启压缩指针**）中长度是64bit。在32位JVM中是这么存的：

![image-20201209212714411](images\32bit JVM mark word.png)

指向类的指针在32位JVM中的长度是32bit，在64位JVM中长度是64bit。

数组长度只有数组对象保存了这部分数据。

==由于HotSpot VM 的自动内存管理系统要求对象起始地址必须是8 字节的整数倍，换句话说，就是**对象的大小必须是8 字节的整数倍**。==



```bash
#类指针压缩
-XX:+UseCompressedClassPointers	 

#普通对象指针压缩 oops: ordinary object pointer
-XX:+UseCompressedOops	
```

**==堆内存超过32G，压缩失效==**

32位系统最大寻址空间2^32=4G，每个字节代表一个地址，中间不被打断，所以4G×8位=32G

64位系统 现在最大48位寻址=256T内存	无良硬件厂商就造了48根地址总线？



### 64位系统测试

pom.xml添加jol依赖

```xml
<dependency>
  <groupId>org.openjdk.jol</groupId>
  <artifactId>jol-core</artifactId>
  <version>0.9</version>
</dependency>
```

**64位系统默认开启了指针压缩**

- 空对象

```java
public class Test {
    static class Dog {    
    }
    public static void main( String[] args ){
        Dog dog =new Dog();
        System.out.println(ClassLayout.parseInstance(dog).toPrintable());
    }
}
```

```java
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
```

- 有成员变量

```java
public class Test {
    static class Dog {
        //依次放开查看数据内存具体占用
        //基本数据类型
        private int age;
        private char date;
        //String引用类型
        private String name;
    }

    public static void main(String[] args) {
        Dog d = new Dog();
        System.out.println(ClassLayout.parseInstance(d).toPrintable());
    }
}
```

```java
 OFFSET  SIZE               TYPE DESCRIPTION                   VALUE
      0     4                    (object header)               01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4                    (object header)               00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4                    (object header)               43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
     12     4                int Dog.age                       0
     16     2               char Dog.date                                 
     18     2                    (alignment/padding gap)                  
     20     4   java.lang.String Dog.name                      null
Instance size: 24 bytes
```

**VM options关闭指针压缩**

```bash
-XX:-UseCompressedClassPointers -XX:-UseCompressedOops
```

```java
OFFSET  SIZE               TYPE DESCRIPTION                     VALUE
      0     4                    (object header)                01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4                    (object header)                00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4                    (object header)                50 35 3c 17 (01010000 00110101 00111100 00010111) (389821776)
     12     4                    (object header)                dd 01 00 00 (11011101 00000001 00000000 00000000) (477)
     16     4                int Dog.age                        0
     20     2               char Dog.date                                   
     22     2                    (alignment/padding gap)                  
     24     8   java.lang.String Dog.name                       null
Instance size: 32 bytes
```

==**对象为数组类型**==

- **基本数据类型**数组计算方式为 (数组个数x单个元素大小),比如int数组，每个int是4字节，如果有5个元素，则数组占20字节。
- **引用类型**就不一样了，
  - 开启指针压缩情况下为(数组长度x4字节)，每个引用4字节；
  - 关闭指针压缩情况下为(数组长度x8字节)，每个引用8字节。如，数组长度为8，每个引用8字节，最终占64字节(关闭指针压缩)。







## volatile

- **线程可见性** 
- **禁止指令重排**

```java
//反证法证明cpu存在乱序执行
//若不存在乱序 则不可能出现x=0 y=0,结果出现了所以得证存在乱序
public class Disorder{
    private static int x =0, y = 0;
    private static int a =0, b = 0;
    
    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        for(;;){
            i++;
            x = 0; y = 0;
            a = 0; b = 0;
            Thread one = new Thread(()->{
                //由于线程one先启动，下面这句话让它等一等线程two,可适当调整等待时间
                //shortWait(100000);
                a = 1;
                x = b;
            });
            
            Thread other = new Thread(()->{
                b = 1;
                y = a;
            })
            one.start();other.start();
            one.join();other.join();
            String result = "第" + i + "次（" + x + "," + y +"）";
            if(x == 0 && y == 0){
                System.out.println(result);
                break;
            } else {
                //System.out.println(result);
            }          
        }              
    } 
    public static void shortWait(long interval){
        long start = System.nanoTime();
        long end;
        do{
            end = System.nanoTime();
        }while(start + interval >= end);        
    }
}
```

### 对象创建过程

```java
//  1.在堆内存开辟内存空间。默认参数基本类型（0，0.0，空）	m=0
//  2.在堆内存中初始化t里面的各个参数。 m=8
//  3.把对象指向堆内存空间。
class T {
    int m = 8;
}
T t = new T();
```

​		跑一下后idea->View->Show Bytecode with Jclasslib

```assembly
#汇编码	0 4 8 对应创建3步
0 new #2 <T>
3 dup
4 invokespecial #3 <T.<init>>
7 astore_1
8 return
```





### 内存屏障

#### X86 CPU内存屏障

- sfence	指令前的写操作必须在指令后的写操作前完成
- lfence   读...
- mfence  读写...



#### JSR内存屏障

- LoadLoad屏障
- StoreStore屏障
- LoadStore屏障
- StoreLoad屏障



#### JVM层面volatile实现细节

```java
---StoreStoreBarrier---
	Volatile写
---StoreLoadBarrier---
```

```java
	Volatile读	
---LoadLoadBarrier---
---LoadStoreBarrier---
```

happens-before原则

as-if-serial 不管如何重排序，单线程执行结果不会改变



#### hotspot实现

` lock;addl $0,0(%%rsp)`	锁总线









## ==AQS ???==

### 1. 介绍

全路径：java.util.concurrent.locks.**AbstractQueuedSynchronizer** 

​		AQS是一个用来构建锁和同步器的框架，使用AQS能简单且高效地构造出应用广泛的大量的同步器，比如我们提到的ReentrantLock，Semaphore，其他的诸如ReentrantReadWriteLock，SynchronousQueue，FutureTask等等皆是基于AQS的。当然，我也能利用AQS非常轻松地构造出符合我们自己需求的同步器。

### 2. 数据结构

​		==**AQS核⼼思想是，如果被请求的共享资源空闲，则将当前请求资源的线程设置为有效的⼯作线程，并且**
**将共享资源设置为锁定状态。如果被请求的共享资源被占⽤，那么就需要⼀套线程阻塞等待以及被唤醒**
**时锁分配的机制，这个机制AQS是⽤CLH队列锁实现的，即将暂时获取不到锁的线程加⼊到队列中。**==

​		CLH(Craig, Landin, and Hagersten)队列是一个虚拟的双向队列（即不存在队列实例，仅存在节点之间的关联关系）。AQS是将每条请求共享资源的线程封装成一个CLH锁队列的一个节点（Node）来实现锁的分配。

数据结构图：

![img](images\AQS数据结构.png)

```java
private volatile int state;// 共享变量，使用volatile修饰保证线程可见性
```





```java
protected final int getState() {
	return state;
}

protected final void setState(int newState) {
	state = newState;
}

// 原子地将同步状态值设置为给定update，如果当前同步状态的值等于期望值expect
protected final boolean compareAndSetState(int expect, int update) {
	return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```



### 3. 资源共享模式

资源有两种共享模式，或者说两种同步方式：

- ==**独占模式（Exclusive）**==：资源是独占的，一次只能一个线程获取。如ReentrantLock。
- ==**共享模式（Share）**==：同时可以被多个线程获取，具体的资源个数可以通过参数指定。如Semaphore/CountDownLatch。



​		一般情况下，子类只需要根据需求实现其中一种模式，当然也有同时实现两种模式的同步类，如`ReentrantReadWriteLock`。

```java
static final class Node {
    // 标记一个结点（对应的线程）在共享模式下等待
    static final Node SHARED = new Node();
    // 标记一个结点（对应的线程）在独占模式下等待
    static final Node EXCLUSIVE = null; 

    // waitStatus的值，表示该结点（对应的线程）已被取消
    static final int CANCELLED = 1; 
    // waitStatus的值，表示后继结点（对应的线程）需要被唤醒
    static final int SIGNAL = -1;
    // waitStatus的值，表示该结点（对应的线程）在等待某一条件
    static final int CONDITION = -2;
    /*waitStatus的值，表示有资源可用，新head结点需要继续唤醒后继结点（共享模式下，多线程并发释放资源，而head唤醒其后继结点后，需要把多出来的资源留给后面的结点；设置新的head结点时，会继续唤醒其后继结点）*/
    static final int PROPAGATE = -3;

    // 等待状态，取值范围，-3，-2，-1，0，1
    volatile int waitStatus;
    volatile Node prev; // 前驱结点
    volatile Node next; // 后继结点
    volatile Thread thread; // 结点对应的线程
    Node nextWaiter; // 等待队列里下一个等待条件的结点


    // 判断共享模式的方法
    final boolean isShared() {
        return nextWaiter == SHARED;
    }

    Node(Thread thread, Node mode) {     // Used by addWaiter
        this.nextWaiter = mode;
        this.thread = thread;
    }
    
    Node(Thread thread, int waitStatus) { // Used by Condition
        this.waitStatus = waitStatus;
        this.thread = thread;
    }

    // 其它方法忽略，可以参考具体的源码
}

// AQS里面的addWaiter私有方法
private Node addWaiter(Node mode) {
    // 使用了Node的这个构造函数
    Node node = new Node(Thread.currentThread(), mode);
    // 其它代码省略
}
```



### 4. 主要方法源码解析

​		AQS的设计是基于**==模板方法模式==**的，它有一些方法必须要子类去实现的，它们主要有：

```java
isHeldExclusively() //该线程是否正在独占资源。只有用到condition才需要去实现它。
tryAcquire(int) //独占方式。尝试获取资源，成功则返回true，失败则返回false。
tryRelease(int) //独占方式。尝试释放资源，成功则返回true，失败则返回false。
tryAcquireShared(int) //共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
tryReleaseShared(int) //共享方式。尝试释放资源，如果释放后允许唤醒后续等待结点返回true，否则返回false。
```

​		默认抛出UnsupportedOperationException`。

![image-20201125215144340](images\test_need_del.png)



## 工具类

### CountDownLatch

```java
package com.ly.concurrent.util;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @Description CountDownLatch类的原理挺简单的，内部同样是一个继承了AQS的实现类Sync
 *  构造器中的计数值（count）实际上就是闭锁需要等待的线程数量。这个值只能被设置一次，而且CountDownLatch没有提供任何机制去重新设置这个计数值。
 * @Created by Administrator
 * @Date 2020/10/12 15:16
 */
public class CountDownLatchDemo {
    // 定义前置任务线程
    static class PreTaskThread implements Runnable {

        private String task;
        private CountDownLatch countDownLatch;

        public PreTaskThread(String task, CountDownLatch countDownLatch) {
            this.task = task;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                Random random = new Random();
                Thread.sleep(random.nextInt(1000));
                System.out.println(task + " - 任务完成");
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // 假设有三个模块需要加载
        CountDownLatch countDownLatch = new CountDownLatch(3);

        // 主任务
        new Thread(() -> {
            try {
                System.out.println("等待数据加载...");
                System.out.println(String.format("还有%d个前置任务", countDownLatch.getCount()));
                countDownLatch.await();
                System.out.println("数据加载完成，正式开始游戏！");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // 前置任务
        new Thread(new PreTaskThread("加载地图数据", countDownLatch)).start();
        new Thread(new PreTaskThread("加载人物模型", countDownLatch)).start();
        new Thread(new PreTaskThread("加载背景音乐", countDownLatch)).start();
    }

}
```

### CyclicBarrier

```java
package com.ly.concurrent.util;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @Description CyclicBarrier内部使用的是ReentrantLock + Condition实现的等待/通知模式
 * @Created by Administrator
 * @Date 2020/10/12 15:19
 */
public class CyclicBarrierDemo {

    static class PreTaskThread implements Runnable {

        private String task;
        private CyclicBarrier cyclicBarrier;

        public PreTaskThread(String task, CyclicBarrier cyclicBarrier) {
            this.task = task;
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            // 假设总共五个关卡
            for (int i = 1; i < 6; i++) {
                try {
                    Random random = new Random();
                    Thread.sleep(random.nextInt(1000));
                    System.out.println(String.format("关卡%d的任务%s完成", i, task));
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                cyclicBarrier.reset(); // 重置栅栏
            }
        }
    }

    public static void main(String[] args) {
        //parties 栅栏barrier打破前必须调用的线程数
        //barrierAction 打破栅栏后执行的任务,最后一个进入栅栏的线程执行。
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, () -> {
            System.out.println("本关卡所有前置任务完成，开始游戏...");
        });

        new Thread(new PreTaskThread("加载地图数据", cyclicBarrier)).start();
        new Thread(new PreTaskThread("加载人物模型", cyclicBarrier)).start();
        new Thread(new PreTaskThread("加载背景音乐", cyclicBarrier)).start();
    }
}
```

### Exchanger

```java
package com.ly.concurrent.util;

import java.util.concurrent.Exchanger;

/**
 * @Description Exchanger类用于两个线程交换数据。它支持泛型，也就是说你可以在两个线程之间传送任何数据
 *      此类提供对外的操作是同步的；
 *      用于成对出现的线程之间交换数据；
 *      可以视作双向的同步队列；
 *      可应用于基因算法、流水线设计等场景。
 * @Created by Administrator
 * @Date 2020/10/12 15:13
 */
public class ExchangerDemo {
    public static void main(String[] args) throws InterruptedException {
        Exchanger<String> exchanger = new Exchanger<>();

        new Thread(() -> {
            try {
                System.out.println("这是线程A，得到了另一个线程的数据："
                        + exchanger.exchange("这是来自线程A的数据"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        System.out.println("这个时候线程A是阻塞的，在等待线程B的数据");
        Thread.sleep(1000);

        new Thread(() -> {
            try {
                System.out.println("这是线程B，得到了另一个线程的数据："
                        + exchanger.exchange("这是来自线程B的数据"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
```

### Phaser

```java
package com.ly.concurrent.util;

import java.util.Random;
import java.util.concurrent.Phaser;

/**
 * @Description Phaser类用来控制某个阶段的线程数量很有用,增强的CyclicBarrier
 *      内部使用了两个基于Fork-Join框架的原子类辅助
 * @Created by Administrator
 * @Date 2020/10/12 15:25
 */
public class PhaserDemo {

    static class PreTaskThread implements Runnable {

        private String task;
        private Phaser phaser;

        public PreTaskThread(String task, Phaser phaser) {
            this.task = task;
            this.phaser = phaser;
        }

        @Override
        public void run() {
            for (int i = 1; i < 4; i++) {
                try {
                    // 第二次关卡起不加载NPC，跳过
                    if (i >= 2 && "加载新手教程".equals(task)) {
                        continue;
                    }
                    Random random = new Random();
                    Thread.sleep(random.nextInt(1000));
                    System.out.println(String.format("关卡%d，需要加载%d个模块，当前模块【%s】",
                            i, phaser.getRegisteredParties(), task));

                    // 从第二个关卡起，不加载NPC
                    if (i == 1 && "加载新手教程".equals(task)) {
                        System.out.println("下次关卡移除加载【新手教程】模块");
                        phaser.arriveAndDeregister(); // 移除一个模块
                    } else {
                        phaser.arriveAndAwaitAdvance();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Phaser phaser = new Phaser(4) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println(String.format("第%d次关卡准备完成", phase + 1));
                return phase == 3 || registeredParties == 0;
            }
        };

        new Thread(new PreTaskThread("加载地图数据", phaser)).start();
        new Thread(new PreTaskThread("加载人物模型", phaser)).start();
        new Thread(new PreTaskThread("加载背景音乐", phaser)).start();
        new Thread(new PreTaskThread("加载新手教程", phaser)).start();
    }
}
```

### Semaphore

```java
package com.ly.concurrent.util;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * @Description Semaphore往往用于资源有限的场景中，去限制线程的数量。
 * @Created by Administrator
 * @Date 2020/10/12 15:10
 */
public class SemaphoreDemo {
    static class MyThread implements Runnable {

        private int value;
        private Semaphore semaphore;

        public MyThread(int value, Semaphore semaphore) {
            this.value = value;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire(); // 获取permit
                System.out.println(String.format("当前线程是%d, 还剩%d个资源，还有%d个线程在等待",
                        value, semaphore.availablePermits(), semaphore.getQueueLength()));
                // 睡眠随机时间，打乱释放顺序
                Random random = new Random();
                Thread.sleep(random.nextInt(1000));
                System.out.println(String.format("线程%d释放了资源", value));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release(); // 释放permit
            }
        }
    }

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < 10; i++) {
            new Thread(new MyThread(i, semaphore)).start();
        }
    }
}
```



## Synchronized  ReentrantLock

==相似点：==

​		这两种同步方式有很多相似之处，它们都是加锁方式同步，而且都是阻塞式的同步，也就是说当如果一个线程获得了对象锁，进入了同步块，其他访问该同步块的线程都必须阻塞在同步块外面等待，而进行线程阻塞和唤醒的代价是比较高的（操作系统需要在用户态与内核态之间来回切换，代价很高，不过可以通过对锁优化进行改善）。

==功能区别：==

​		这两种方式最大区别就是对于**Synchronized**来说，它是java语言的关键字，是原生语法层面的互斥，需要**jvm**实现。而**ReentrantLock**它是JDK 1.5之后提供的**API层面**的互斥锁，需要lock()和unlock()方法配合try/finally语句块来完成

- 便利性：很明显Synchronized的使用比较方便简洁，并且由编译器去保证锁的加锁和释放，而ReenTrantLock需要手工声明来加锁和释放锁，为了避免忘记手工释放锁造成死锁，所以最好在finally中声明释放锁。

- 锁的细粒度和灵活度：很明显ReenTrantLock优于Synchronized

==性能的区别：==

​		在Synchronized优化以前，synchronized的性能是比ReenTrantLock差很多的，但是自从Synchronized引入了偏向锁，轻量级锁（自旋锁）后，两者的性能就差不多了，在两种方法都可用的情况下，官方甚至建议使用synchronized，其实synchronized的优化我感觉就借鉴了ReenTrantLock中的CAS技术。都是试图在用户态就把加锁问题解决，避免进入内核态的线程阻塞。

### Synchronized

```java
public class SynDemo{ 
	public static void main(String[] arg){
		Runnable t1=new MyThread();
		new Thread(t1,"t1").start();
		new Thread(t1,"t2").start();
	}
}
class MyThread implements Runnable {
	@Override
	public void run() {
		synchronized (this) {
			for(int i=0;i<10;i++)
				System.out.println(Thread.currentThread().getName()+":"+i);
		}		
	} 
}
```



### ReentrantLock

​		由于ReentrantLock是java.util.concurrent包下提供的一套互斥锁，相比Synchronized，ReentrantLock类提供了一些高级功能，主要有以下3项：

1. ==**等待可中断**==，持有锁的线程长期不释放的时候，正在等待的线程可以选择放弃等待，这相当于Synchronized来说可以避免出现死锁的情况。通过lock.lockInterruptibly()来实现这个机制。
2. **==公平锁==**，多个线程等待同一个锁时，必须按照申请锁的时间顺序获得锁，Synchronized锁非公平锁，ReentrantLock默认的构造函数是创建的非公平锁，可以通过参数true设为公平锁，但公平锁表现的性能不是很好。

```java
//创建一个非公平锁，默认是非公平锁
Lock lock = new ReentrantLock();
Lock lock = new ReentrantLock(false);
 
//创建一个公平锁，构造传参true
Lock lock = new ReentrantLock(true);
```

3. ==**锁绑定多个条件**==，一个ReentrantLock对象可以同时绑定对个对象。ReenTrantLock提供了一个Condition（条件）类，用来实现**分组唤醒需要唤醒的线程们**，而不是像synchronized要么随机唤醒一个线程要么唤醒全部线程。

> ReenTrantLock实现的原理

​		ReentrantLock基于AQS实现，而AQS底层使用的是改进的CLH队列，CAS+阻塞+唤醒，对于暂时获取不到资源以及尚未被父节点唤醒的线程在队列中阻塞休眠，被唤醒后CAS获取资源。并不是单纯的阻塞或者CAS，兼顾了性能和效率。		

​		ReenTrantLock的实现是一种自旋锁，通过循环调用CAS操作来实现加锁。它的性能比较好也是因为避免了使线程进入内核态的阻塞状态。想尽办法避免线程进入内核的阻塞状态是我们去分析和理解锁设计的关键钥匙。

```java
public class SynDemo{ 
	public static void main(String[] arg){
		Runnable t1=new MyThread();
		new Thread(t1,"t1").start();
		new Thread(t1,"t2").start();
	} 
}
class MyThread implements Runnable {
	private Lock lock=new ReentrantLock();
	public void run() {
        lock.lock();
        try{
            for(int i=0;i<5;i++)
                System.out.println(Thread.currentThread().getName()+":"+i);
        }finally{
            lock.unlock();
        }
	}
}
```



## Thread

```java
package com.ly.concurrent.thread;

import java.util.concurrent.*;

/**
 * @Description	创建线程方式
 * @Created by Administrator
 * @Date 2020/10/11 23:09
 */
public class CreateThreadDemo {

    public static void main(String[] args) throws Exception {
        //1、继承Thread
        Thread myThread = new MyThread();
        myThread.start();

        //2、实现Runnable接口
        new Thread(new MyThread1()).start();
        // Java 8 函数式编程，可以省略MyThread1类
        new Thread(() -> System.out.println("Java 8 匿名内部类")).start();

        //建个线程池
        ExecutorService executor = Executors.newCachedThreadPool();
        //3、实现Callable接口    有返回值，而且支持泛型。
        Task task = new Task();
        Future<Integer> future = executor.submit(task);
        // 注意调用get方法会阻塞当前线程，直到得到结果。
        // 所以实际编码中建议使用可以设置超时时间的重载get方法。
        System.out.println(future.get());

        //4、使用FutureTask类
        FutureTask<Integer> futureTask = new FutureTask<>(new Task());
        executor.submit(futureTask);    //无返回值
        System.out.println(futureTask.get());

        //关闭线程池
        executor.shutdown();

    }

    public static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("MyThread extends Thread.");
        }
    }

    public static class MyThread1 implements Runnable {

        @Override
        public void run() {
            System.out.println("MyThread implements Runnable.");
        }
    }

    public static class Task implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            // 模拟计算需要一秒
            Thread.sleep(1000);
            return 2;
        }
    }

}
```

## ThreadPool

```java
package com.ly.concurrent.thread;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/8 21:16
 */
public class MyThreadPool {
    public static void main(String[] args) {
        /**
         *  ===前5个参数必需===
         *  corePoolSize：线程池中核心线程数的最大值
         *  maximumPoolSize：该线程池中线程总数最大值
         *  keepAliveTime：表示非核心线程的存活时间。
         *  timeUnit：表示keepAliveTime的单位。
         *  workQueue：用于缓存任务的阻塞队列 常用：
         *             LinkedBlockingQueue 链式阻塞队列，底层数据结构是链表，默认大小是Integer.MAX_VALUE，也可以指定大小。
         *             ArrayBlockingQueue 数组阻塞队列，底层数据结构是数组，需要指定队列的大小。
         *             SynchronousQueue  同步队列，内部容量为0，每个put操作必须等待一个take操作，反之亦然。
         *             DelayQueue 延迟队列，该队列中的元素只有当其指定的延迟时间到了，才能够从队列中获取到该元素 。
         *  threadFactory：指定创建线程的工厂
         *  handler：表示当workQueue已满，且池中的线程数达到maximumPoolSize时，线程池拒绝添加新任务时采取的策略。
         *            ThreadPoolExecutor.AbortPolicy：默认拒绝处理策略，丢弃任务并抛出RejectedExecutionException异常。
         *            ThreadPoolExecutor.DiscardPolicy：丢弃新来的任务，但是不抛出异常。
         *            ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列头部（最旧的）的任务，然后重新尝试执行程序（如果再次失败，重复此过程）。
         *            ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务。
         */
        ExecutorService executor = new ThreadPoolExecutor(
                3,
                5,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        for (int i = 0; i < 9; i++) {
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + "====>执行任务");
                try {
                    TimeUnit.SECONDS.sleep(5L);
                    //Thread.currentThread().sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();
    }



    //四种常见线程池

    /**
     *  当需要执行很多短时间的任务时，CacheThreadPool的线程复用率比较高， 会显著的提高性能。而且线程60s后会回收，意味着即使没有任务进来，CacheThreadPool并不会占用很多资源。
     *  运行流程：
     * 1、提交任务进线程池。
     * 2、因为corePoolSize为0的关系，不创建核心线程，线程池最大为Integer.MAX_VALUE。
     * 3、尝试将任务添加到SynchronousQueue队列。
     * 4、如果SynchronousQueue入列成功，等待被当前运行的线程空闲后拉取执行。如果当前没有空闲线程，那么就创建一个非核心线程，然后从SynchronousQueue拉取任务并在当前线程执行。
     * 5、如果SynchronousQueue已有任务在等待，入列操作将会阻塞。
     * @return
     */
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    /**
     *  只能创建核心线程。无界队列LinkedBlockingQueue的默认大小是Integer.MAX_VALUE
     *  没有任务的情况下， FixedThreadPool占用资源更多。
     * @param nThreads
     * @return
     */
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(
                nThreads,
                nThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    /**
     *  有且仅有一个核心线程（ corePoolSize == maximumPoolSize=1），使用了LinkedBlockingQueue（容量很大），
     *  所以，不会创建非核心线程。所有任务按照先来先执行的顺序执行。如果这个唯一的线程不空闲，那么新来的任务会存储在任务队列里等待执行。
     * @return
     */
    public static ExecutorService newSingleThreadExecutor() {
        return
                //new FinalizableDelegatedExecutorService
                (new ThreadPoolExecutor(
                        1,
                        1,
                        0L,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>()));
    }

    /**
     * 创建一个定长线程池，支持定时及周期性任务执行。
     * @param corePoolSize
     * @return
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        //return new ScheduledThreadPoolExecutor(corePoolSize);
        //return new ThreadPoolExecutor(
        //        corePoolSize,
        //        Integer.MAX_VALUE,
        //        0,
        //        NANOSECONDS,
        //        new DelayedWorkQueue());
        return null;

    }
}
```







# Reflect

## Class反射 

获取Class类三种方式：

- ==**通过类的路径获取**==

  `Class class = Class.forName("com.ly.entry.User");`

- ==**通过类获取**==

  `Class class = User.class;`

- ==**通过对象获取**==

  `User user = new User();`

  `Class class = user.getClass();`



编写AutoWired注解

```java
package com.ly.reflect;

import java.lang.annotation.*;

/**
 * @Description 使用反射自己实现注解自动注入
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Documented
public @interface AutoWired {
}
```

UserService和UserController

```java
public class UserService {

}

public class UserController {
    @AutoWired
    private UserService userService;

    public UserService getUserService() {
        return userService;
    }
}
```

Test

```java
package com.ly.reflect;

import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) {
        UserController userController = new UserController();
        Class<? extends UserController> clazz = userController.getClass();

        // 获取所有属性
        Stream.of(clazz.getDeclaredFields()).forEach(field -> {
            // 判断每个属性是否有注解AutoWired
            AutoWired annotation = field.getAnnotation(AutoWired.class);
            if (annotation != null) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                // 实例化对象
                Object o =null;

                try {
                    o = type.newInstance();
                    field.set(userController, o);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println(userController.getUserService());
    }
}
```











## 代理模式 

- 静态代理 
- 动态代理
  - JDK  (必须实现接口)
  - CGLIB （类和方法不能声明为final）
    	  		

jdk动态代理必须实现接口，否则只能用cglibProxy  enhancer字节码增强。

jdk生成的代理类父类是Proxy类,通过jdk代理生成的类都继承Proxy类:

因为Java是单继承的,而代理类又必须继承自Proxy类,所以通过jdk代理的类必须实现接口。



短消息发送接口及实现

```java
public interface SmsService {
    String send(String msg);
}
```

````java
public class SmsServiceImpl implements SmsService {
    @Override
    public String send(String msg) {
        System.out.println("send message:" + msg);
        return msg;
    }
}
````

### 静态代理

````java
/**
 * @Description 静态代理，创建代理类并同样实现发送短信的接口
 * @Created by Administrator
 * @Date 2020/10/8 22:51
 */
public class StaticProxy implements SmsService {
    private SmsService smsService;

    public StaticProxy(SmsService smsService){
        this.smsService = smsService;
    }

    @Override
    public String send(String msg) {
        System.out.println("staticProxy调用send()方法前，添加自己的操作");
        smsService.send(msg);
        System.out.println("staticProxy调用send()方法后，添加自己的操作");
        return null;
    }
}
````

### JDK动态代理

````java
public class JdkDynamicProxy implements InvocationHandler {
    //被代理对象
    private Object target;

    public JdkDynamicProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("jdkProxy调用【" + method.getName() + "】方法前，添加自己的操作");
        Object result = method.invoke(target, args);
        System.out.println("jdkProxy调用【" + method.getName() + "】方法后，添加自己的操作");
        return result;
    }
}
````

### Cglib动态代理

pom文件添加

```xml
<dependency>
    <groupId>cglib</groupId>
    <artifactId>cglib</artifactId>
    <version>3.3.0</version>
</dependency>
```

```java
/**
 * @Description Cglib动态代理 需要导入jar包cglib asm   类和方法不能声明为final
 * @Created by Administrator
 * @Date 2020/10/8 23:07
 */
public class CglibDynamicProxy implements MethodInterceptor {

    /**
     * @param object 被代理的对象（需要增强的对象）
     * @param method    被拦截的方法（需要增强的方法）
     * @param args   方法参数
     * @param methodProxy   用于调用原始方法
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        System.out.println("cglibProxy调用【"+method.getName()+"】方法前，添加自己的操作");
        Object result = methodProxy.invokeSuper(object, args);
        System.out.println("cglibProxy调用【"+method.getName()+"】方法后，添加自己的操作");
        return result;
    }
}
```

```java
public class CglibFilter implements CallbackFilter {

    @Override
    public int accept(Method method) {
        //对应callback[]数组顺序
        //即send方法使用CglibDynamicProxy拦截，非send方法使用NoOp.INSTANCE拦截
        // 可以过滤动态代理的方法
        if ("send".equalsIgnoreCase(method.getName())) {
            return 0;
        }
        return 1;
    }
}
```

### 测试类

```java
public class ProxyTest {
    public static void main(String[] args) {
        SmsService smsService = new SmsServiceImpl();

        /**
         *  静态代理测试
         */
        StaticProxy staticProxy = new StaticProxy(smsService);
        staticProxy.send("静态代理。。。");


        /**
         *  JDK动态代理 被代理对象必须实现接口
         */
        SmsService jdkProxy = (SmsService) Proxy.newProxyInstance(
                smsService.getClass().getClassLoader(), //被代理类的类加载器
                smsService.getClass().getInterfaces(),  //被代理类需要实现的接口(可多个)
                new JdkDynamicProxy(smsService));//代理类具体实现

        jdkProxy.send("JDK动态代理。。。");


        /**
         *  Cglib动态代理 不需要实现接口  需要导入jar包cglib和asm,类和方法不能声明为final
         */

        SmsServiceImpl cglibProxy = (SmsServiceImpl) Enhancer.create(
                SmsServiceImpl.class,
                null,
                new CglibFilter(),
                new Callback[]{new CglibDynamicProxy(), NoOp.INSTANCE});    //NoOp.INSTANCE是CGlib所提供的实际是一个没有任何操作的拦截器
        cglibProxy.send("Cglib动态代理。。。");
    }
}
```





# [IO](Java-IO.md)





# JVM

JVM 堆 栈 方法栈分配 GC基本原理  GC Roots
线程	多线程	线程池 参数含义		threadLocals源代码	sleep方法是不会释放当前的锁的，而wait方法会



<img src="images\jvm1.7.png" style="zoom: 200%;" />



![img](images\jvm1.7_1.png)

![img](images\jvm1.7_2.png)

![img](images\classFile.png)



## GC

### 判断对象已死

#### 引用计数算法

​	简单效率高，但无法解决对象循环引用的问题

#### 可达性分析算法

​	**==GC Roots==**

- 虚拟机栈（栈帧中的本地变量表）中引用的对象
- 方法区中静态属性引用的对象
- 方法区中常量引用的对象
- 本地方法栈中JNI(Native方法)引用的对象

### 垃圾收集算法

1. **标记清除算法 Mark-Sweep**

   

2. **复制算法（新生代） Copying**

   **==Eden:Survivor:Survivor = 8:1:1==**

   

3. **标记整理算法（老年代） Mark-Compact**



### 垃圾回收器

![image-20201211002824201](images\garbage collectors.png)

![image-20201211003919122](images\heap logic part.png)

![image-20201211004729078](images\gc-some.png)



![image-20201211005637055](images\new object summary.png)

TLAB:Thread Local Allocation Buffer



#### CMS

- 初始标记。只是标记一下GC Roots能直接关联到的老年代对象，速度很快。这一阶段会STW
- 并发标记。就是进行GC Roots的Tracing，处理器可以与用户线程一起工作。
- 重新标记。为了修正并发标记期间因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录，这个阶段也会触发STW，停顿时间会比初始标记阶段稍长，远比并发时间短。
- 并发清除，处理器可以与用户线程一起工作。



三色标记:黑灰白

CMS方案: Incrumental Update隐蔽问题

​	并发标记，产生漏标

所以，CMS的Remark阶段必须从头扫描一遍。

## JVM调优

- 预分配
- 卡顿
- OOM

```bash
#显示所有可调参数
java -XX:+PrintFlagsFinal | more
java -XX:+PrintFlagsFinal | wc -l

#显示默认jvm参数
java -XX:+PrintCommandLineFlags -version
```

### **==OutOfMemoryError排查与解决==**

#### **出现问题：**

- java.lang.OutOfMemoryError: heap space
- java.lang.OutOfMemoryError: metaspace
- java.lang.OutOfMemoryError: Perm Gen
- ...

#### **碰到OOM之后想办法拿到Heap Dump分析：**

- ==**-XX:+HeapDumpOnOutOfMemoryError				（启动添加）**==

-XX:HeapDumpPath=/usr/local/logs/java/

- **jmap -dump:live,format=b,file=<filepath> <pid>		（线程运行中添加）**

先使用**jps**查看jvm运行中的线程pid如94532，然后添加：

jmap -dump:live,format=b,file=/94532.hprof 94532

#### **OOM的可能原因：**

- **50%是代码问题**
- **40%是配置问题**
- **10%是内存真的不够**
- **这意味着，绝大多数OOM不能通过加内存解决**

#### **Heap Dump 分析**	

- **Metaspace/PermGen**
- **瞄准Class对象**
- **Heap space**
  - **瞄准占用空间最大的对象**
- **Path to GCRoots**
- **顺藤摸瓜去看代码**

#### **工具**

- **MAT	MemoryAnalyzerTool**
- **VisualVM**
- **JProfiler**
- ==**arthas**==



```bash
jps
jstack
jmap -histo 进程号 | head -20	#生产环境不一定能用
top -Hp

#arthas
thread
```



### GC常用参数

| -Xmn -Xms -Xmx -Xss                                          | 年轻代 最小值 最大值 栈空间        |
| ------------------------------------------------------------ | ---------------------------------- |
| -XX:+UseTLAB                                                 | 使用TLAB，默认打开                 |
| -XX:+PrintTLAB                                               | 打印TLAB的使用情况                 |
| -XX:+TLABSize                                                | 设置TLAB大小                       |
| -XX:+DisableExplictGC                                        | System.gc()不管用，FGC             |
| -XX:+PrintGC                                                 |                                    |
| -XX:+PrintGCDetails                                          |                                    |
| -XX:+PrintHeapAtGC                                           |                                    |
| -XX:+PrintGCTimeStamps                                       |                                    |
| -XX:+PrintGCApplicationConcurrentTime(低)                    | 打印应用程序时间                   |
| -XX:+PrintGcApplicationStoppedTime(低)                       | 打印暂停时间                       |
| -XX:+PrintReferenceGC(重要性低)                              | 记录回收了多少种不同引用类型的引用 |
| -verbose:class                                               | 类加载详细过程                     |
| -XX:+PrintVMOptions                                          |                                    |
| `-XX:+PrintFlagsFinal` `-XX:+PrintFlagsInitial`              | 必须会用                           |
| -Xloggc:opt/log/gc.log                                       |                                    |
| -XX:+MaxTenuringThreshold                                    | 升代年龄，最大值15                 |
| 锁自旋次数 -XX:PreBlockSpin <br>热点代码检测参数 -XX:CompileThreshold <br> 逃逸分析 标量替换... | 不建议设置                         |



>  生产环境中**设置-Xms和-Xmx相同大小**，以让JVM在启动时就直接向OS申请xmx的commited内存，好处：

- 避免JVM在运行过程中向OS申请内存
- 延后启动后首次GC的发生时机
- 减少启动初期的GC次数
- 尽可能避免使用swap space



### Parallel常用参数

| -XX:SurvivorRatio          |                                                          |
| -------------------------- | -------------------------------------------------------- |
| -XX:PreTenureSizeThreshold | 大对象到底多大                                           |
| -XX:MaxTenuringThreshold   |                                                          |
| -XX:+ParallelGCThreads     | 并行收集器的线程数，同样适用于CMS，一般设为和CPU核数相同 |
| -XX:+UseAdaptiveSizePolicy | 自动选择各区大小比例                                     |



### CMS常用参数

| -XX:+UseConcMarkSweepGC                |                                                              |
| -------------------------------------- | ------------------------------------------------------------ |
| -XX:ParallelCMSThreads                 | CMS线程数量                                                  |
| -XX:CMSInitiatingOccupancyFraction     | 使用多少比例的老年代后开始CMS收集，默认是68%(近似值)，如果频繁发生SerialOld卡顿，应该调小，（频繁CMS回收） |
| -XX:+UseCMSCompactAtFullCollection     | 在FGC时进行压缩                                              |
| -XX:CMSFullGCsBeforeCompaction         | 多少次FGC之后进行压缩                                        |
| -XX:+CMSClassUnloadingEnabled          |                                                              |
| -XX:CMSInitiatingPermOccupancyFraction | 达到什么比例时进行Perm回收                                   |
| GCTimeRatio                            | 设置GC时间占用程序运行时间的百分比                           |
| -XX:MaxGCPauseMillis                   | 停顿时间，是一个建议时间，GC会尝试用各种手段达到这个时间，比如减小年轻代 |



### G1常用参数

| -XX:+UseG1GC                   |                                                              |
| ------------------------------ | ------------------------------------------------------------ |
| -XX:MaxGCPauseMillis           | 建议值，G1会尝试调整Young区的块数来达到这个值                |
| -XX:GCPauseIntervalMillis      | ？GC的间隔时间                                               |
| -XX:+G1HeapRegionSize          | 分区大小，建议逐渐增大该值，1 2 4 8 16 32。  随着size增加，垃圾的存活时间更长，GC间隔更长，但每次GC的时间也会更长  ZGC做了改进（动态区块大小） |
| G1NewSizePercent               | 新生代最小比例，默认为5%                                     |
| G1MaxNewSizePercent            | 新生代最大比例，默认为60%                                    |
| GCTimeRatio                    | GC时间建议比例，G1会根据这个值调整堆空间                     |
| ConcGCThreads                  | 线程数量                                                     |
| InitiatingHeapOccupancyPercent | 启动G1的堆空间占用比例                                       |





# JAVA8

## Lambda

```java
package com.ly.lambda;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/9 17:37
 */
public class Person {
    public static final int INT = 46;
    int age;
    String name;
    String gender;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public Person(int age, String name, String gender) {
        this.age = age;
        this.name = name;
        this.gender = gender;
    }

    public Person(String name, String gender) {
        this.name = name;
        this.gender = gender;
    }


    public String getName() {
        return this.name;
    }

    public Person toOpposite() {
        if (gender.charAt(0) == 'M')
            gender = "F";
        else
            gender = "M";
        return this;
    }

    public static boolean isTest() {
        return true;
    }

    public boolean isUnder(Person person) {
        return person.age > this.age;
    }

    public boolean isMale() {
        return gender.equals("M");
    }
}
```



```java
package com.ly.lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @Description 双冒号使用测试
 * 类名::实例方法
 * 对象::实例方法
 * @Created by Administrator
 * @Date 2020/10/9 17:30
 */
public class DoubleColonsTest {


    // 实例对象引用实例方法
    Supplier<String> supplier1 = "lowerCase"::toUpperCase;
    Supplier<String> supplier1_1 = () -> "lowerCase".toUpperCase();

    // 类引用(无参)构造函数
    Supplier<String> supplier2 = String::new;
    Supplier<String> supplier2_1 = () -> new String();

    // 类引用(有参）构造函数
    Function<String, String> function1 = String::new;
    Function<String, String> function1_1 = (String str) -> new String(str);

    // 类引用实例方法，入参为传入实例对象，入参、出参同类型
    Function<String, String> function2 = String::toUpperCase;
    Function<String, String> function2_1 = (String str) -> str.toUpperCase();

    // Predicate<T>可理解为特殊的Function<T, Boolean>

    Person person = new Person();
    // 须为无参静态方法
    Supplier<Boolean> supplierBln = Person::isTest;
    Supplier<Boolean> supplierBln_1 = () -> Person.isTest();

    // 实例对象调用实例方法
    Supplier<String> supplierStr = person::getName;
    Supplier<String> supplierStr_1 = () -> person.getName();

    // 无参构造函数
    Supplier<Person> supplierPerson = Person::new;
    Supplier<Person> supplierPerson_1 = () -> new Person();
    // 有参构造函数
    BiFunction<String, String, Person> biFunction = Person::new;
    BiFunction<String, String, Person> biFunction_1 = (name, gender) -> new Person(name, gender);

    // 类名调用实例方法，入参为传入实例对象
    Function<Person, Person> functionP = Person::toOpposite;
    Function<Person, Person> functionP_1 = Person -> person.toOpposite();

    Consumer<String> consumer = System.out::println;
    Consumer<String> consumer_1 = (String str) -> System.out.println(str);
    ;

    public static void main(String[] args) {
        List<String> list = Arrays.asList("1", "2", "3");
        boolean bl = list.stream().anyMatch("1"::equals);
        List<String> retval = list.stream().collect(Collectors.toCollection(LinkedList::new));

        List<Person> persons = Arrays.asList(new Person(10, "Jack", "M"));
        Person person = new Person(20, "Lily", "F");
        persons.stream().filter(Person::isMale).filter(person::isUnder).collect(Collectors.toCollection(ArrayList::new));
    }

}
```



















## Stream

```java
public static void main(String[] args) {
    Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
        .reduce((a, b) -> {
            System.out.println(String.format("%s: %d + %d = %d", Thread.currentThread().getName(), a, b, a + b));
            return a + b;
        })
        .ifPresent(System.out::println);


    System.out.println("多线程并行计算...");
    Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
        .parallel()
        .reduce((a, b) -> {
            System.out.println(String.format("%s: %d + %d = %d", Thread.currentThread().getName(), a, b, a + b));
            return a + b;
        })
        .ifPresent(System.out::println);
}
```

```java
package com.ly.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/12 16:28
 */
public class StreamParallelDemo {
    public static void main(String[] args) {
        System.out.println(String.format("本计算机的核数：%d", Runtime.getRuntime().availableProcessors()));

        // 产生100w个随机数(1 ~ 100)，组成列表
        Random random = new Random();
        List<Integer> list = new ArrayList<>(1000_0000);

        for (int i = 0; i < 1000_0000; i++) {
            list.add(random.nextInt(100));
        }

        long prevTime = getCurrentTime();
        list.stream().reduce((a, b) -> a + b).ifPresent(System.out::println);
        System.out.println(String.format("单线程计算耗时：%d", getCurrentTime() - prevTime));

        prevTime = getCurrentTime();
        list.stream().parallel().reduce((a, b) -> a + b).ifPresent(System.out::println);
        System.out.println(String.format("多线程计算耗时：%d", getCurrentTime() - prevTime));

    }

    private static long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
```













# Design patterns

[详见Design patterns.md](Design patterns.md)



## singleton

### 饿汉模式

```java
package com.ly.designpattern.singleton;

/**
 * @Description 单例模式-饿汉模式
 * @Created by Administrator
 * @Date 2020/10/15 22:25
 */
public class SingletonHungry {
    private static SingletonHungry instance = new SingletonHungry();

    private SingletonHungry() {
    }

    public static SingletonHungry getInstance() {
        return instance;
    }
}
```

### 懒汉模式

```java
package com.ly.designpattern.singleton;

/**
 * @Description 单例模式-懒汉模式
 *  双重校验锁实现对象单例（线程安全）
 * @Created by Administrator
 * @Date 2020/10/12 23:29
 */
public class SingletonLazy {
    //volatile 1、线程可见性 2、禁止指令重排
    //必须加volatile，否则并发thread1发生指令重排（2 3步）半初始化singleton,此时thread2判断singleton不为空直接使用半初始化状态的对象，默认参数为（0，0.0，空）
    private volatile static SingletonLazy singleton;

    private SingletonLazy() {
    }

    public static SingletonLazy getInstance() {
        //多个线程到这直接判断不用进行锁竞争提升效率性能
        if (singleton == null) {    //DCL 双重检查锁double check lock
            synchronized (SingletonLazy.class) {
                if (singleton == null) {
                    //  1.在堆内存开辟内存空间。默认参数基本类型（0，0.0，空）
                    //  2.在堆内存中初始化SingleTon里面的各个参数。
                    //  3.把对象指向堆内存空间。
                    singleton = new SingletonLazy();
                }
            }
        }
        return singleton;
    }
}
```



### 静态内部类

```java
package com.ly.designpattern.singleton;

import sun.security.jca.GetInstance;

/**
 * @Description 单例模式-静态内部类
 * 静态内部类就属于被动引用的行列，可保证线程安全，也能保证单例的唯一性，同时也延迟了单例的实例化。
 * 外部类加载时并不需要立即加载内部类，内部类不被加载则不去初始化INSTANCE，故而不占内存。
 * 即当SingleTon第一次被加载时，并不需要去加载SingleTonHolder，只有当getInstance()方法第一次被调用时，
 * 才会去初始化INSTANCE,第一次调用getInstance()方法会导致虚拟机加载SingleTonHolder类，
 *
 *
 * 类加载时机：JAVA虚拟机在有且仅有的5种场景下会对类进行初始化。
 * 1.遇到new、getstatic、putstatic或者invokestatic这4个字节码指令时，对应的java代码场景为：
 *      new一个关键字或者一个实例化对象时、读取或设置一个静态字段时(final修饰、已在编译期把结果放入常量池的除外)、调用一个类的静态方法时。
 * 2.使用java.lang.reflect包的方法对类进行反射调用的时候，如果类没进行初始化，需要先调用其初始化方法进行初始化。
 * 3.当初始化一个类时，如果其父类还未进行初始化，会首先触发其父类的初始化。
 * 4.当虚拟机启动时，用户需要指定一个要执行的主类(包含main()方法的类)，虚拟机会先初始化这个类。
 * 5.当使用JDK 1.7等动态语言支持时，如果一个java.lang.invoke.MethodHandle实例最后的解析结果
 *      REF_getStatic、REF_putStatic、REF_invokeStatic的方法句柄，并且这个方法句柄所对应的类没有进行过初始化，则需要先触发其初始化。
 * @Created by Administrator
 * @Date 2020/10/15 22:33
 */
public class SingletonStaticInnerClass {

    private SingletonStaticInnerClass() {
    }

    private static class SingletonHolder {
        private static SingletonStaticInnerClass INSTANCE = new SingletonStaticInnerClass();
    }

    public static SingletonStaticInnerClass GetInstance() {
        return SingletonHolder.INSTANCE;
    }
}
```

### 枚举类型

```java
package com.ly.designpattern.singleton;

/**
 * @Description 单例模式-枚举类型
 * 线程安全的
 * @Created by Administrator
 * @Date 2020/10/15 23:34
 */
public enum SingletonEnum {
    INSTANCE;

    public void method(){
        //todo
    }

    public static void main(String[] args) {
        System.out.println(SingletonEnum.INSTANCE);
    }

}
```



## proxy

[代理模式](###代理模式)



# others

## idea



| 光标移到下一新增行   | shift + Enter                                                |
| -------------------- | ------------------------------------------------------------ |
| 补全声明if while try | ctrl + shift + Enter                                         |
| 转换大小写           | ctrl + shift + U                                             |
| 预览                 | ctrl + shift + I                                             |
| 层级结构             | F4                                                           |
| 扩展选择块           | Alt+Shift+向上箭头                                           |
| 选择重写或实现方法   | 菜单项Code->Override Methods 或 Alt+Insert->Override Methods |



插件：

google-java-format

lombok

jclasslib

protocol buffer editor

SequenceDiagram











## windows 10



myeclipse---

maven nexus？？？

gradle

git		ssh .gitconfig

svn visualSVN

mysql	navicat

mat

xshell/putty


adobe pr2018









































