package com.lhd.builder;

import com.lhd.bean.Constants;
import com.lhd.bean.ExtendField;
import com.lhd.bean.FieldInfo;
import com.lhd.bean.TableInfo;
import com.lhd.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

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
            }
            for (Map.Entry<String, List<ExtendField>> entry : tableInfo.getExtendFieldMap().entrySet()) {
                List<ExtendField> extendFieldList = entry.getValue();
                for (ExtendField extendField : extendFieldList) {
                    CommentBuilder.buildFieldComment(bw, entry.getKey() + "扩展字段");
                    bw.write("\tprivate " + extendField.getFieldType() + " " + extendField.getFieldName() + ";");
                    bw.newLine();
                    bw.newLine();
                }
            }

            // 3. 生成方法
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                String propertyName = fieldInfo.getPropertyName();
                // getter
                String get = "\tpublic " + fieldInfo.getJavaType() + " get" + StringTools.UpperHead(propertyName,1) + "() {";
                bw.write(get);
                bw.newLine();
                bw.write("\t\treturn " + fieldInfo.getPropertyName() + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
                // setter
                String set = "\tpublic " + "void" + " set" + StringTools.UpperHead(propertyName,1) + String.format("(%s %s)", fieldInfo.getJavaType(), propertyName) + " {";
                bw.write(set);
                bw.newLine();
                bw.write("\t\t" + "this." + propertyName + " = " + propertyName + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
            }
            for (Map.Entry<String, List<ExtendField>> entry : tableInfo.getExtendFieldMap().entrySet()) {
                List<ExtendField> extendFieldList = entry.getValue();
                for (ExtendField extendField : extendFieldList) {
                    // getter
                    String get = "\tpublic " + extendField.getFieldType() + " get" + StringTools.UpperHead(extendField.getFieldName(),1) + "() {";
                    bw.write(get);
                    bw.newLine();
                    bw.write("\t\treturn " + extendField.getFieldName() + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                    // setter
                    String set = "\tpublic " + "void" + " set" + StringTools.UpperHead(extendField.getFieldName(),1) + String.format("(%s %s)", extendField.getFieldType(), extendField.getFieldName()) + " {";
                    bw.write(set);
                    bw.newLine();
                    bw.write("\t\t" + "this." + extendField.getFieldName() + " = " + extendField.getFieldName() + ";");
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
