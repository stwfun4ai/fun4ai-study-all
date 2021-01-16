package com.ly.hashmap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Description hashMap遍历
 * 1、迭代器（Iterator）方式遍历；EntrySet/KeySet
 * 2、For Each 方式遍历； EntrySet/KeySet
 * 3、Lambda 表达式遍历（JDK 1.8+）;
 * 4、Streams API 遍历（JDK 1.8+）。  单线程/多线程
 * @Created by Administrator
 * @Date 2020/10/9 20:33
 */
public class Traversal {
    public static void main(String[] args) {

        Map<Integer, String> map = new HashMap();
        map.put(1, "Java");
        map.put(2, "JDK");
        map.put(3, "Spring Framework");
        map.put(4, "MyBatis Framework");
        map.put(5, "Mysql");

        //迭代器遍历EntrySet
        System.out.println("===迭代器遍历EntrySet===");
        Iterator<Map.Entry<Integer, String>> entrySetIterator = map.entrySet().iterator();
        while (entrySetIterator.hasNext()) {
            Map.Entry<Integer, String> entry = entrySetIterator.next();
            System.out.print(entry.getKey());
            System.out.println(entry.getValue());
        }

        //迭代器遍历KeySet
        System.out.println("===迭代器遍历KeySet===");
        Iterator<Integer> keySetIterator = map.keySet().iterator();
        while (keySetIterator.hasNext()) {
            Integer key = keySetIterator.next();
            System.out.print(key);
            System.out.println(map.get(key));
        }

        //ForEach遍历EntrySet
        System.out.println("===ForEach遍历EntrySet===");
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.print(entry.getKey());
            System.out.println(entry.getValue());
        }
        //ForEach遍历KeySet
        System.out.println("===ForEach遍历KeySet===");
        for (Integer key : map.keySet()) {
            System.out.print(key);
            System.out.println(map.get(key));
        }

        //Lambda
        System.out.println("===Lambda遍历===");
        map.forEach((key, value) -> {
            System.out.print(key);
            System.out.println(value);
        });

        //Streams API 单线程
        System.out.println("===Streams API 单线程===");
        map.entrySet().stream().forEach((entry) -> {
            System.out.print(entry.getKey());
            System.out.println(entry.getValue());
        });


        //Streams API 多线程
        System.out.println("===Streams API 多线程===");
        map.entrySet().parallelStream().forEach(entry -> {
            System.out.print(entry.getKey());
            System.out.println(entry.getValue());
        });


    }

}
