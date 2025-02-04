package com.lhd.bean;

import com.lhd.utils.PropertiesUtils;

/**
 * @Author: liuhd
 * @Date: 2025/1/27 16:24
 * @Description:
 */
public class Constants {
    // 是否忽略表前缀
    public static Boolean IGNORE_TABLE_PREFIX;
    // 查询参数bean的后缀
    public static String QUERY_BEAN_SUFFIX;

    // Mapper的后缀
    public static String MAPPER_BEAN_SUFFIX;
    // Service类的后缀
    public static String SERVICE_BEAN_SUFFIX;
    // ServiceImpl的后缀
    public static String SERVICEIMPL_BEAN_SUFFIX;

    // 文件输出地址
    public static String PATH_BASE;
    // 基础包名
    public static String PACKAGE_BASE;
    // java目录
    public static final String PATH_JAVA = "java/";
    // resources目录
    public static final String PATH_RESOURCES = "resources/";
    // po类输出路径
    public static String PATH_PO;
    // po类所在包
    public static String PACKAGE_PO;
    // 枚举类输出路径
    public static String PATH_ENUM;
    // 枚举所在包
    public static String PACKAGE_ENUM;
    // 查询参数类输出路径
    public static String PATH_QUERY;
    // query类所在包
    public static String PACKAGE_QUERY;
    // controller类路径
    public static String PATH_CONTROLLER;
    // controller类所在包
    public static String PACKAGE_CONTROLLER;
    // vo类所在路径
    public static String PATH_VO;
    // vo类所在包
    public static String PACKAGE_VO;
    // exception类所在路径
    public static String PATH_EXCEPTION;
    // exception类所在包
    public static String PACKAGE_EXCEPTION;

    // utils类所在路径
    public static String PATH_UTILS;
    // utils类所在包
    public static String PACKAGE_UTILS;

    // mappers类所在路径
    public static String PATH_MAPPERS;
    // mappers类所在包
    public static String PACKAGE_MAPPERS;
    // service接口所在路径
    public static String PATH_SERVICE;
    // service接口所在包
    public static String PACKAGE_SERVICE;
    // serviceImpl实现类所在路径
    public static String PATH_SERVICEIMPL;
    // serviceImpl所在包
    public static String PACKAGE_SERVICEIMPL;
    // XML所在路径
    public static String PATH_XML;
    // 作者名
    public static String AUTHOR;
    // 日期参数格式
    public static String DATE_PATTERN;
    public static String DATETIME_PATTERN;

    static {
        IGNORE_TABLE_PREFIX = Boolean.valueOf(PropertiesUtils.getString("ignore.table.prefix"));

        QUERY_BEAN_SUFFIX = PropertiesUtils.getString("query.bean.suffix");

        MAPPER_BEAN_SUFFIX = PropertiesUtils.getString("mapper.bean.suffix");

        SERVICE_BEAN_SUFFIX = PropertiesUtils.getString("service.bean.suffix");

        SERVICEIMPL_BEAN_SUFFIX = PropertiesUtils.getString("serviceImpl.bean.suffix");

        PATH_BASE = PropertiesUtils.getString("path.base");

        PACKAGE_BASE = PropertiesUtils.getString("package.base");

        PACKAGE_PO = PACKAGE_BASE + "." +  PropertiesUtils.getString("package.po");

        PATH_PO = PATH_BASE + PATH_JAVA + PACKAGE_PO.replace(".","/");

        PACKAGE_ENUM = PACKAGE_BASE + "." + PropertiesUtils.getString("package.enum");

        PATH_ENUM = PATH_BASE + PATH_JAVA + PACKAGE_ENUM.replace(".","/");

        PACKAGE_QUERY = PACKAGE_BASE + "." +  PropertiesUtils.getString("package.query");

        PATH_QUERY = PATH_BASE + PATH_JAVA + PACKAGE_QUERY.replace(".","/");

        PACKAGE_CONTROLLER = PACKAGE_BASE + "." + PropertiesUtils.getString("package.controller");

        PATH_CONTROLLER = PATH_BASE + PATH_JAVA + PACKAGE_CONTROLLER.replace(".","/");

        PACKAGE_VO = PACKAGE_BASE + "." + PropertiesUtils.getString("package.vo");

        PATH_VO = PATH_BASE + PATH_JAVA + PACKAGE_VO.replace(".","/");

        PACKAGE_EXCEPTION = PACKAGE_BASE + "." + PropertiesUtils.getString("package.exception");

        PATH_EXCEPTION = PATH_BASE + PATH_JAVA + PACKAGE_EXCEPTION.replace(".","/");

        PACKAGE_UTILS = PACKAGE_BASE + "." + PropertiesUtils.getString("package.utils");

        PATH_UTILS = PATH_BASE + PATH_JAVA + PACKAGE_UTILS.replace(".","/");

        PACKAGE_MAPPERS = PACKAGE_BASE + "." + PropertiesUtils.getString("package.mappers");

        PATH_MAPPERS = PATH_BASE + PATH_JAVA + PACKAGE_MAPPERS.replace(".","/");

        PACKAGE_SERVICE = PACKAGE_BASE + "." + PropertiesUtils.getString("package.service");

        PATH_SERVICE = PATH_BASE + PATH_JAVA + PACKAGE_SERVICE.replace(".","/");

        PACKAGE_SERVICEIMPL = PACKAGE_BASE + "." + PropertiesUtils.getString("package.service.impl");

        PATH_SERVICEIMPL = PATH_BASE + PATH_JAVA + PACKAGE_SERVICEIMPL.replace(".","/");

        PATH_XML = PATH_BASE + PATH_RESOURCES + PACKAGE_MAPPERS.replace(".","/");

        AUTHOR = PropertiesUtils.getString("author");

        DATE_PATTERN = PropertiesUtils.getString("date.pattern");

        DATETIME_PATTERN = PropertiesUtils.getString("datetime.pattern");
    }

    public final static String[] SQL_DATE_TIME_TYPES = new String[]{"datetime", "timestamp"};

    public final static String[] SQL_DATE_TYPES = new String[]{"date"};

    public static final String[] SQL_DECIMAL_TYPE = new String[]{"decimal", "double", "float"};

    public static final String[] SQL_STRING_TYPE = new String[]{"char", "varchar", "text", "mediumtext", "longtext"};

    //Integer
    public static final String[] SQL_INTEGER_TYPE = new String[]{"int", "tinyint"};

    //Long
    public static final String[] SQL_LONG_TYPE = new String[]{"bigint"};

    public static void main(String[] args) {
        System.out.println(MAPPER_BEAN_SUFFIX);
        System.out.println(PACKAGE_PO);
        System.out.println(PATH_QUERY);
        System.out.println(PATH_ENUM);
        System.out.println(PATH_PO);
        System.out.println(PATH_VO);
        System.out.println(PATH_EXCEPTION);
        System.out.println(PATH_UTILS);
        System.out.println(PATH_MAPPERS);
        System.out.println(PATH_XML);
        System.out.println(PATH_SERVICE);
        System.out.println(PATH_SERVICEIMPL);
    }
}
