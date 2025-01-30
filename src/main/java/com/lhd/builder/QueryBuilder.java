package com.lhd.builder;

import com.lhd.bean.Constants;
import com.lhd.bean.FieldInfo;
import com.lhd.bean.TableInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @Author: liuhd
 * @Date: 2025/1/27 21:15
 * @Description: 构造表的Query类
 */
public class QueryBuilder {
    public static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

    public static void execute(TableInfo tableInfo) {
        // 创建文件输出目录
        File folder = new File(Constants.PATH_QUERY);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // 创建query-java文件
        File queryFile = new File(folder, tableInfo.getBeanName() + Constants.QUERY_BEAN_SUFFIX + ".java");
        BufferedWriter bw = null;
        try {
            // 1. package import
            bw = new BufferedWriter(new FileWriter(queryFile));
            bw.write("package " + Constants.PACKAGE_QUERY + ";");
            bw.newLine();
            bw.newLine();


            if (tableInfo.getHaveDate() || tableInfo.getHaveDateTime()) {
                bw.write("import java.util.Date;");
                bw.newLine();
            }
            if (tableInfo.getHaveBigDecimal()) {
                bw.write("import java.math.BigDecimal;");
                bw.newLine();
            }
            bw.newLine();
            CommentBuilder.buildClassComment(bw, tableInfo.getComment() + "查询参数类");
            bw.write("public class " + tableInfo.getBeanName() + Constants.QUERY_BEAN_SUFFIX + " extends BaseParam {");
            bw.newLine();

            // 2. 生成字段
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                CommentBuilder.buildFieldComment(bw, fieldInfo.getComment());
                bw.write("\tprivate " + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName() + ";");
                bw.newLine();
                bw.newLine();
                // 如果是字符串类型的 那么需要加上对应的模糊匹配字段
                if ("String".equals(fieldInfo.getJavaType())){
                    bw.write("\tprivate " + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName() + "Fuzzy;");
                    bw.newLine();
                    bw.newLine();
                }
                // 如果是Date类型的 那么需要加上对应的开始与结束字段 用于查询时间范围内的记录
                if ("Date".equals(fieldInfo.getJavaType())){
                    bw.write("\tprivate String " + fieldInfo.getPropertyName() + "Start;");
                    bw.newLine();
                    bw.write("\tprivate String " + fieldInfo.getPropertyName() + "End;");
                    bw.newLine();
                }
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
                // 如果是String类型 为其Fuzzy也生成get
                if ("String".equals(fieldInfo.getJavaType())){
                    get = "\tpublic " + fieldInfo.getJavaType() + " get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1) + "Fuzzy() {";
                    bw.write(get);
                    bw.newLine();
                    bw.write("\t\treturn " + fieldInfo.getPropertyName() + "Fuzzy;");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                }
                // 如果是Date类型 为其start与end也生成get
                if ("Date".equals(fieldInfo.getJavaType())){
                    get = "\tpublic String" + " get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1) + "Start() {";
                    bw.write(get);
                    bw.newLine();
                    bw.write("\t\treturn " + fieldInfo.getPropertyName() + "Start;");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();

                    get = "\tpublic String" + " get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1) + "End() {";
                    bw.write(get);
                    bw.newLine();
                    bw.write("\t\treturn " + fieldInfo.getPropertyName() + "End;");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                }
                // setter
                String set = "\tpublic " + "void" + " set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1) + String.format("(%s %s)", fieldInfo.getJavaType(), propertyName) + " {";
                bw.write(set);
                bw.newLine();
                bw.write("\t\t" + "this." + propertyName + " = " + propertyName + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
                // 如果是String类型 为其Fuzzy也生成set
                if ("String".equals(fieldInfo.getJavaType())){
                    set = "\tpublic " + "void" + " set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1) + "Fuzzy" + String.format("(%s %s)", fieldInfo.getJavaType(), propertyName + "Fuzzy") + " {";
                    bw.write(set);
                    bw.newLine();
                    bw.write("\t\t" + "this." + propertyName + "Fuzzy" + " = " + propertyName + "Fuzzy;");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                }
                // 如果是Date类型 为其start与end也生成get
                if ("Date".equals(fieldInfo.getJavaType())){
                    set = "\tpublic " + "void" + " set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1) + "Start" + String.format("(%s %s)", "String", propertyName + "Start") + " {";
                    bw.write(set);
                    bw.newLine();
                    bw.write("\t\t" + "this." + propertyName + "Start" + " = " + propertyName + "Start;");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();

                    set = "\tpublic " + "void" + " set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1) + "End" + String.format("(%s %s)", "String", propertyName + "End") + " {";
                    bw.write(set);
                    bw.newLine();
                    bw.write("\t\t" + "this." + propertyName + "End" + " = " + propertyName + "End;");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                }
            }



            bw.write("}");
            bw.flush();
        } catch (IOException e) {
            logger.error("query生成失败", e);
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
