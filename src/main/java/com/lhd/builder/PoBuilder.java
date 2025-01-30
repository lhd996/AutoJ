package com.lhd.builder;

import com.lhd.bean.Constants;
import com.lhd.bean.FieldInfo;
import com.lhd.bean.TableInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @Author: liuhd
 * @Date: 2025/1/27 21:15
 * @Description: 构造表的po类
 */
public class PoBuilder {
    public static final Logger logger = LoggerFactory.getLogger(PoBuilder.class);

    public static void execute(TableInfo tableInfo) {
        // 创建文件输出目录
        File folder = new File(Constants.PATH_PO);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // 创建po-java文件
        File poFile = new File(folder, tableInfo.getBeanName() + ".java");
        BufferedWriter bw = null;
        try {
            // 1. package import
            bw = new BufferedWriter(new FileWriter(poFile));
            bw.write("package " + Constants.PACKAGE_PO + ";");
            bw.newLine();
            bw.newLine();

            bw.write("import java.io.Serializable;");
            bw.newLine();

            if (tableInfo.getHaveDate() || tableInfo.getHaveDateTime()) {
                bw.write("import java.util.Date;");
                bw.newLine();
                bw.write("import com.fasterxml.jackson.annotation.JsonFormat;");
                bw.newLine();
                bw.write("import java.text.SimpleDateFormat;");
                bw.newLine();
            }
            if (tableInfo.getHaveBigDecimal()) {
                bw.write("import java.math.BigDecimal;");
                bw.newLine();
            }
            bw.newLine();
            CommentBuilder.buildClassComment(bw, tableInfo.getComment());
            bw.write("public class " + tableInfo.getBeanName() + " implements Serializable {");
            bw.newLine();

            // 2. 生成字段
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                CommentBuilder.buildFieldComment(bw, fieldInfo.getComment());
                if (isSQLDateOrDateTime(fieldInfo.getSqlType()) == 1) {
                    bw.write(String.format("\t@JsonFormat(pattern = \"%s\")", Constants.DATE_PATTERN));
                    bw.newLine();
                }
                if (isSQLDateOrDateTime(fieldInfo.getSqlType()) == 2) {
                    bw.write(String.format("\t@JsonFormat(pattern = \"%s\")", Constants.DATETIME_PATTERN));
                    bw.newLine();
                }

                bw.write("\tprivate " + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName() + ";");
                bw.newLine();
                bw.newLine();
            }

            // 3. 生成方法
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                String propertyName = fieldInfo.getPropertyName();
                // getter
                String get = "\tpublic " + fieldInfo.getJavaType() + " get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1) + "() {";
                bw.write(get);
                bw.newLine();
                bw.write("\t\treturn " + fieldInfo.getPropertyName() + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
                // setter
                String set = "\tpublic " + "void" + " set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1) + String.format("(%s %s)", fieldInfo.getJavaType(), propertyName) + " {";
                bw.write(set);
                bw.newLine();
                bw.write("\t\t" + "this." + propertyName + " = " + propertyName + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
            }

            // 4. 重写toString方法
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic String toString() {");
            bw.newLine();
            bw.write("\t\treturn ");

            StringBuilder sb = new StringBuilder();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                sb.append("\",")
                        .append(StringUtils.isEmpty(fieldInfo.getComment()) ? fieldInfo.getPropertyName() : fieldInfo.getComment())
                        .append(" : \"")
                        .append(" + ")
                        .append("(")
                        .append(fieldInfo.getPropertyName())
                        .append(" == null ? ")
                        .append("\"空\"")
                        .append(" : ");
                if (isSQLDateOrDateTime(fieldInfo.getSqlType()) == 0) {
                    sb.append(fieldInfo.getPropertyName());
                }
                if (isSQLDateOrDateTime(fieldInfo.getSqlType()) == 1) {
                    sb.append("new SimpleDateFormat(").append("\"").append(Constants.DATE_PATTERN).append("\"").append(").format(").append(fieldInfo.getPropertyName()).append(")");
                }
                if (isSQLDateOrDateTime(fieldInfo.getSqlType()) == 2) {
                    sb.append("new SimpleDateFormat(").append("\"").append(Constants.DATETIME_PATTERN).append("\"").append(").format(").append(fieldInfo.getPropertyName()).append(")");
                }
                sb.append(")").append(" + ");
            }
            String res = sb.toString();
            res = res.substring(2, res.lastIndexOf("+"));
            res = "\"" + res + ";";
            bw.write(res);
            bw.newLine();

            bw.write("\t}");
            bw.newLine();


            bw.write("}");
            bw.flush();
        } catch (IOException e) {
            logger.error("po生成失败", e);
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param sqlType
     * @return int 0：不是，1：是date，2：是datetime
     * @description: 是不是sql的date或者datetime类型
     * @author liuhd
     * 2025/1/29 12:35
     */
    private static int isSQLDateOrDateTime(String sqlType) {
        return ArrayUtils.contains(Constants.SQL_DATE_TYPES, sqlType) ? 1 : (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, sqlType) ? 2 : 0);
    }
}
