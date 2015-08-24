package com.tuisongbao.engine.demo.utils;

/**
 * Created by user on 15-8-14.
 */
public class LogUtil {

    public static String makeLogTag(Class clazz) {
        return "TSB" + clazz.getSimpleName();
    }

}
