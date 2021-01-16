package com.ly.hashmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Description
 * @Created by Administrator
 * @Date 2020/10/1 23:30
 */
public class Test {

    public static void main(String[] args) {

//        float f = 1.3f, d = 1.8f;
//        System.out.println((int)f); //1 截断后面的小数
//        System.out.println((int)d); //1 截断后面的小数
//
//        Map m = new HashMap();



        /**
         *Iterator 主要是用来遍历集合用的，它的特点是更加安全，
         * 因为它可以确保，在当前遍历的集合元素被更改的时候，
         * 就会抛出 ConcurrentModificationException 异常。
         */
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        //fail-fast并发修改异常
//        for (String s : list) {
//            if ("2".equals(s)){
//                list.remove(s);
//            }
//        }
        //lambda表达式
        list.removeIf("2"::equals);

        Iterator<String> str = list.iterator();
        while (str.hasNext()){
            if ("2".equals(str.next())){
                str.remove();
            }
        }
















    }

}
