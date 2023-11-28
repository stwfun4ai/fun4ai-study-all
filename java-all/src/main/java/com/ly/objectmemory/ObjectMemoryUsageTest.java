package com.ly.objectmemory;

import org.openjdk.jol.info.ClassLayout;

/**
 * @Description 对象在内存中使用情况测试分析
 * 需添加jol依赖
 * @Created by fun4ai
 * @Date 1/13 0013 0:46
 */
public class ObjectMemoryUsageTest {
    static class Dog {
        //依次放开查看数据内存具体占用
        //基本数据类型
        //private int age;
        //private char date;
        //String引用
        //private String name;
    }

    public static void main(String[] args) {
        Dog d = new Dog();
        //jol
        //VM options关闭指针压缩测试：类指针和普通对象指针ordinary object pointer
        // -XX:-UseCompressedClassPointers -XX:-UseCompressedOops
        System.out.println(ClassLayout.parseInstance(d).toPrintable());
        //dog数组int[]数组测试

    }

}
