package com.ly.reflect;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/10 0010 23:52
 */
public class UserController {
    @AutoWired
    private UserService userService;

    public UserService getUserService() {
        return userService;
    }
}
