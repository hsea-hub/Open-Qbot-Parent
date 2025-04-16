package com.qbot.cq.business.common.contexts;


public class UserRequestContext {
    private static final ThreadLocal<String> USER_INFO = new ThreadLocal<>();

    public static void set(String wxId) {
        USER_INFO.set(wxId);
    }
    public static void remove() {
        USER_INFO.remove();
    }
    public static String get() {
        return USER_INFO.get();
    }

}
