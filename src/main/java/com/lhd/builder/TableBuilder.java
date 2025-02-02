package com.lhd.builder;

import com.lhd.bean.Constants;
import com.lhd.bean.ExtendField;
import com.lhd.bean.FieldInfo;
import com.lhd.bean.TableInfo;
import com.lhd.utils.PropertiesUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;


/**
 * @Author: liuhd
 * @Date: 2025/1/27 15:12
 * @Description: 建立表结构 将mysql中的表建立成Java对象
 */
public class TableBuilder {
    private static Connection conn = null;

    private static final Logger logger = LoggerFactory.getLogger(TableBuilder.class);

    // 该SQL可以拿到所有的表以及表注释
    private static final String SQL_SHOW_TABLE_STATUS = "SHOW TABLE STATUS";
    // 该SQL可以拿到表的所有字段以及字段的信息
    private static final String SQL_SHOW_FULL_COLUMNS = "SHOW FULL COLUMNS FROM %s";
    // 该SQL可以设置表的索引
    private static final String SQL_SHOW_INDEX = "SHOW INDEX FROM %s";


    // 连接数据库
    static {
        String driverName = PropertiesUtils.getString("db.driver.name");
        String url = PropertiesUtils.getString("db.url");
        String username = PropertiesUtils.getString("db.username");
        String password = PropertiesUtils.getString("db.password");
        try {
            Class.forName(driverName);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            logger.error("数据库连接失败", e);
        }
    }

    // 获取数据库中所有表
    public static List<TableInfo> getTables() {
        PreparedStatement ps = null;
        ResultSet tableResult = null;
        List<TableInfo> tableInfoList = new ArrayList<>();
        try {
            // 准备小车
            ps = conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            // 执行sql
            tableResult = ps.executeQuery();
            while (tableResult.next()) {
                // 获取表名
                String tableName = tableResult.getString("Name");
                // 获取对应注释
                String comment = tableResult.getString("Comment");

                // 设置表信息
                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName);
                tableInfo.setComment(comment);
                // 设置beanName
                String beanName = tableName;
                // 如果需要忽略前缀
                if (Constants.IGNORE_TABLE_PREFIX) {
                    beanName = tableName.substring(beanName.indexOf("_") + 1);
                }
                // 表名转驼峰
                beanName = transferCamel(beanName, true);
                tableInfo.setBeanName(beanName);
                tableInfo.setBeanParamName(beanName + Constants.QUERY_BEAN_SUFFIX);

                // 设置表的字段
                setFieldInfo(tableInfo);
                // 设置索引
                setIndexInfo(tableInfo);
                // 设置扩展字段
                setExtendsField(tableInfo);

                tableInfoList.add(tableInfo);
            }
            // logger.info("{}",JsonUtils.transferObj2Json(tableInfoList));
        } catch (Exception e) {
            logger.error("getTables方法异常", e);
        } finally {
            // 关流
            if (tableResult != null) {
                try {
                    tableResult.close();
                } catch (SQLException e) {
                    logger.error("ResultSet关流失败", e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    logger.error("PreparedStatement关流失败", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Connection关流失败", e);
                }
            }
        }
        return tableInfoList;
    }

    /**
     * @description: 设置扩展字段
     * @param tableInfo
     * @return
     * @author liuhd
     * 2025/1/31 14:57
     */
    private static void setExtendsField(TableInfo tableInfo) {
        Map<FieldInfo, List<ExtendField>> map = new HashMap<>();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            ArrayList<ExtendField> list = new ArrayList<>();
            // String类型的扩展是Fuzzy
            if ("String".equals(fieldInfo.getJavaType())){
                ExtendField extendField = new ExtendField();
                extendField.setFieldName(fieldInfo.getPropertyName() + "Fuzzy");
                extendField.setFieldType("String");
                list.add(extendField);

            }
            // Date类型的扩展是Start与End
            if ("Date".equals(fieldInfo.getJavaType())){
                ExtendField extendField1 = new ExtendField();
                extendField1.setFieldName(fieldInfo.getPropertyName() + "Start");
                extendField1.setFieldType("String");

                ExtendField extendField2 = new ExtendField();
                extendField2.setFieldName(fieldInfo.getPropertyName() + "End");
                extendField2.setFieldType("String");

                list.add(extendField1);
                list.add(extendField2);
            }
            map.put(fieldInfo,list);
            // 其他类型的扩展 。。。。
        }

        tableInfo.setExtendFieldMap(map);
    }
    /**
     * @param fieldName   字段名
     * @param isTableName 是否是表名
     * @return java.lang.String
     * @description: 转驼峰
     * @author liuhd
     * 2025/1/27 16:56
     */
    private static String transferCamel(String fieldName, boolean isTableName) {
        StringBuilder sb = new StringBuilder();
        // 如果是表名 第一个字母大写
        sb.append(isTableName ? Character.toUpperCase(fieldName.charAt(0)) : fieldName.charAt(0));

        for (int i = 1; i < fieldName.length(); i++) {
            // 如果是_ 就把跳到下一个字符 并转成大写拼接
            if (fieldName.charAt(i) == '_') {
                sb.append(Character.toUpperCase(fieldName.charAt(++i)));
            } else {
                // 否则直接拼接
                sb.append(fieldName.charAt(i));
            }
        }
        return sb.toString();
    }

    /**
     * @param tableInfo
     * @return java.util.List<com.lhd.bean.FieldInfo>
     * @description: 设置表中的所有字段
     * @author liuhd
     * 2025/1/27 17:31
     */
    private static void setFieldInfo(TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_FULL_COLUMNS, tableInfo.getTableName()));
            rs = ps.executeQuery();
            List<FieldInfo> fieldInfoList = new ArrayList<>();

            boolean haveDate = false;
            boolean haveDateTime = false;
            boolean haveBigDecimal = false;

            while (rs.next()) {
                String field = rs.getString("Field");
                String type = rs.getString("Type");
                if (type.contains("(")) {
                    type = type.substring(0, type.indexOf("("));
                }
                String comment = rs.getString("Comment");
                String extra = rs.getString("Extra");
                // 判断各个have
                // 是否是date
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)){
                    haveDate = true;
                }
                // 是否是date_time
                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)){
                    haveDateTime = true;
                }
                // 是否是bigDecimal
                if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE,type)){
                    haveBigDecimal = true;
                }
                // 填充fieldList
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.setFieldName(field);
                fieldInfo.setPropertyName(transferCamel(field, false));
                fieldInfo.setComment(comment);
                fieldInfo.setAutoIncrement("auto_increment".equals(extra));
                fieldInfo.setSqlType(type);
                fieldInfo.setJavaType(transferJavaType(type));

                fieldInfoList.add(fieldInfo);
            }
            tableInfo.setHaveDate(haveDate);
            tableInfo.setHaveDateTime(haveDateTime);
            tableInfo.setHaveBigDecimal(haveBigDecimal);

            tableInfo.setFieldList(fieldInfoList);


        } catch (SQLException e) {
            logger.error("读取字段操作失败", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error("ResultSet关流失败", e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    logger.error("PreparedStatement关流失败", e);
                }
            }
        }
    }

    /**
     * @description: 设置表的索引
     * @param tableInfo
     * @return
     * @author liuhd
     * 2025/1/27 20:02
     */

    private static void setIndexInfo(TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, FieldInfo> fieldInfoMap = new HashMap<>();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            fieldInfoMap.put(fieldInfo.getFieldName(),fieldInfo);
        }
        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_INDEX, tableInfo.getTableName()));
            rs = ps.executeQuery();
            LinkedHashMap<String, List<FieldInfo>> indexMap = new LinkedHashMap<>();
            while (rs.next()) {
                String keyName = rs.getString("Key_name");
                Integer nonUnique = rs.getInt("Non_unique");
                String columnName = rs.getString("Column_name");
                // 如果是唯一索引
                if (nonUnique == 0){
                    List<FieldInfo> fieldInfoList = indexMap.get(keyName);
                    // 如果没有这个索引 说明是第一次往indexMap里放
                    if (fieldInfoList == null){
                        fieldInfoList = new ArrayList<>();
                    }
                    // 否则不是第一次往里放 这是个联合索引
                    // 找到对应field 塞到索引对应的List中
                    fieldInfoList.add(fieldInfoMap.get(columnName));
                    indexMap.put(keyName,fieldInfoList);
                }
            }
            tableInfo.setKeyIndexMap(indexMap);
        } catch (SQLException e) {
            logger.error("读取索引失败", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error("ResultSet关流失败", e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    logger.error("PreparedStatement关流失败", e);
                }
            }
        }
    }


    /**
     * @param type MySQL 数据类型
     * @return Java 类型（全限定类名或简单类型）
     * @description: 将 MySQL 中的类型转换成 Java 中的类型
     * @author liuhd
     * 2025/1/27 18:54
     */
    private static String transferJavaType(String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("MySQL 类型不能为空");
        }

        // 统一转为小写，避免大小写敏感问题
        type = type.toLowerCase();

        // 处理带括号的类型（如 varchar(255)、int(11)）
        if (type.contains("(")) {
            type = type.substring(0, type.indexOf("("));
        }

        // 根据 MySQL 类型映射到 Java 类型
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, type)) {
            return "Integer";
        } else if (ArrayUtils.contains(Constants.SQL_LONG_TYPE, type)) {
            return "Long";
        } else if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type) || ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)) {
            return "Date";
        } else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
            return "BigDecimal";
        } else {
            throw new RuntimeException("无法识别的类型:" + type);
        }
    }
}
