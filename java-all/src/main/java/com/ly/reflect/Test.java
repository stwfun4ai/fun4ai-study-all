package com.ly.reflect;

import java.util.stream.Stream;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/10 0010 23:59
 */
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
