package com.jn.common.util;

import android.util.Log;

import com.jn.common.BuildConfig;


/**
 * Log统一管理类
 */
public class LogUtils {

    private static boolean LOGENABLE = BuildConfig.DEBUG;// 是否需要打印bug，可以在application的onCreate函数里面初始化
    private static String TAG = "LogUtils";
    private static final String sysOutMessageFormat1 = "%s: %s:>>> %s";
    private static final String sysOutMessageFormat2 = "%s:>>> %s";

    private LogUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 初始化Log
     *
     * @param tagName   Tag名称
     */
    public static void init(String tagName) {
        if (tagName != null && !"".equals(tagName))
            TAG = tagName;
    }

    public static void i(String msg) {
        if (LOGENABLE)
            Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (LOGENABLE)
            Log.d(TAG, msg);
    }

    public static void w(String msg) {
        if (LOGENABLE)
            Log.w(TAG, msg);
    }

    public static void e(String msg) {
        if (LOGENABLE)
            Log.e(TAG, msg);
    }

    public static void v(String msg) {
        if (LOGENABLE)
            Log.v(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (LOGENABLE)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (LOGENABLE)
            Log.d(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (LOGENABLE)
            Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (LOGENABLE)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (LOGENABLE)
            Log.v(tag, msg);
    }

    /**
     * system 输出
     */
    public static void sysOut(String s) {
        if (LOGENABLE) {
            System.out.println(TAG + s);
        }
    }

    /**
     * system 输出
     */
    public static void sysOut(String tag, Object s) {
        if (LOGENABLE) {
            System.out.println(String.format(sysOutMessageFormat1, TAG, tag, s));
        }
    }

    /**
     * system 输出
     */
    public static void sysOut1(String tag, Object s) {
        if (LOGENABLE) {
            System.out.println(String.format(sysOutMessageFormat2, tag, s));
        }
    }
}