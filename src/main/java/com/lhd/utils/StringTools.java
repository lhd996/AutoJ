package com.lhd.utils;

/**
 * @Author: liuhd
 * @Date: 2025/1/30 16:10
 * @Description:
 */
public class StringTools {

    /**
     * @description: 将前几个字母大写
     * @param str
     * @param len 前几个字母
     * @return java.lang.String
     * @author liuhd
     * 2025/1/30 16:13
     */
    public static String UpperHead(String str,int len){
        if (len < 0 || len > str.length()) return "长度异常！！！！！";
        return str.substring(0, len).toUpperCase() + str.substring(len);
    }

    /**
     * @description: 将前几个字母小写
     * @param str
     * @param len
     * @return java.lang.String
     * @author liuhd
     * 2025/1/31 12:52
     */
    public static String lowerHead(String str,int len){
        if (len < 0 || len > str.length()) return "长度异常！！！！！";
        return str.substring(0, len).toLowerCase() + str.substring(len);
    }

    public static void main(String[] args) {
        System.out.println(UpperHead("acdscsvAFcevw", 1));
        System.out.println(UpperHead("acdscsvAFcevw", 2));
        System.out.println(UpperHead("acdscsvAFcevw", 3));

        System.out.println(lowerHead("ABCecev", 2));
        System.out.println(lowerHead("aBc", 3));
        System.out.println(lowerHead("abCC", 3));
    }
}
