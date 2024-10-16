package com.r.chat.context;

public class AdminContext {

    public static ThreadLocal<Boolean> threadLocal = new ThreadLocal<>();

    static {
        threadLocal.set(false);  // 默认不是管理员
    }

    public static void setAdmin(Boolean isAdmin) {
        threadLocal.set(isAdmin);
    }

    public static Boolean isAdmin() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

}
