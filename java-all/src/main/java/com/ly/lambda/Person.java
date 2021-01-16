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
