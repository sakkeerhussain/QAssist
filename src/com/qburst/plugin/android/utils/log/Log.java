package com.qburst.plugin.android.utils.log;

/**
 * Created by sakkeer on 18/01/17.
 */
public class Log {

    public static void d(String tag, String s) {
        System.out.println("\n===========Debug===============");
        System.out.println(tag+": "+s);
        System.out.println("===============================\n");
    }

    public static void t(String tag, String s) {
        System.out.println("\n===========Test===============");
        System.out.println(tag+": "+s);
        System.out.println("===============================\n");
    }

    public static void e(String tag, String s) {
        System.out.println("\n===========Error===============");
        System.out.println(tag+": "+s);
        System.out.println("===============================\n");
    }
}
