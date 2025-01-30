package com.lhd.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: liuhd
 * @Date: 2025/1/28 15:41
 * @Description:
 */
public class DateUtils {

    public static final String yyyy_MM_dd  = "yyyy-MM-dd";
    public static final String yyyyMMdd  = "yyyy/MM/dd";
    /**
     * @param pattern 目标格式
     * @param date    输入日期
     * @return
     * @description: 日期转字符串
     * @author liuhd
     * 2025/1/28 15:42
     */
    public static String format(String pattern, Date date) {
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * @param str     输入字符串
     * @param pattern 目标格式
     * @return java.util.Date
     * @description: 字符串转日期
     * @author liuhd
     * 2025/1/28 15:47
     */
    public static Date parse(String str, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
