package com.ly.hashmap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/9 21:47
 */
public class SafetyDelete {

    public static void main(String[] args) {
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(1, "Java");
        map.put(2, "JDK");
        map.put(3, "Spring Framework");
        map.put(4, "MyBatis Framework");
        map.put(5, "Mysql");


        //迭代器方式删除==安全
        System.out.println("迭代器方式删除==安全");
        Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, String> entry = iterator.next();
            if (entry.getKey() == 1) {
                // 删除
                System.out.println("del:" + entry.getKey());
                iterator.remove();
            } else {
                System.out.println("show:" + entry.getKey());
            }
        }


        //For 循环中删除数据非安全==报错
        System.out.println("For 循环中删除数据非安全==报错");
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getKey() == 1) {
                // 删除
                System.out.println("del:" + entry.getKey());
                map.remove(entry.getKey());
            } else {
                System.out.println("show:" + entry.getKey());
            }
        }

        //Lambda 循环中删除数据非安全==报错
        System.out.println("Lambda 循环中删除数据非安全==报错");
        map.forEach((key, value) -> {
            if (key == 1) {
                System.out.println("del:" + key);
                map.remove(key);
            } else {
                System.out.println("show:" + key);
            }
        });

        //Lambda 正确删除
        // 根据 map 中的 key 去判断删除
        System.out.println("Lambda 正确删除");
        map.keySet().removeIf(key -> key == 1);
        map.forEach((key, value) -> {
            System.out.println("show:" + key);
        });

        //Stream 循环中删除数据非安全
        System.out.println("Stream 循环中删除数据非安全==报错");
        map.entrySet().stream().forEach((entry) -> {
            if (entry.getKey() == 1) {
                System.out.println("del:" + entry.getKey());
                map.remove(entry.getKey());
            } else {
                System.out.println("show:" + entry.getKey());
            }
        });

        //Stream 正确删除
        map.entrySet().stream().filter(m -> 1 != m.getKey()).forEach((entry) -> {
            if (entry.getKey() == 1) {
                System.out.println("del:" + entry.getKey());
            } else {
                System.out.println("show:" + entry.getKey());
            }
        });


        //我们不能在遍历中使用集合 map.remove() 来删除数据，这是非安全的操作方式，


        //但我们可以使用迭代器的 iterator.remove() 的方法来删除数据，这是安全的删除集合的方式。
        //同样的我们也可以使用 Lambda 中的 removeIf 来提前删除数据，或者是使用 Stream 中的 filter 过滤掉要删除的数据进行循环，这样都是安全的，
        // 当然我们也可以在 for 循环前删除数据在遍历也是线程安全的。



    }
}
