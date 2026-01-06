package org.clf.springboot.utils;

import org.clf.springboot.entity.User;

public class UserContextHolder {

    private static ThreadLocal<User> USER = new ThreadLocal<>();

    public static void setUser(User user) {
        USER.set(user);
    }

    public static Long getUserId() {
        return USER.get().getId();
    }

    public static void removeUser() {
        USER.remove();
    }

    public static User getUser() {
        return USER.get();
    }
}
