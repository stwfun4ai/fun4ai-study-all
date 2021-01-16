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
