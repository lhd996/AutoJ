package com.lhd.builder;

import com.lhd.bean.Constants;
import com.lhd.bean.FieldInfo;
import com.lhd.bean.TableInfo;
import com.lhd.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: liuhd
 * @Date: 2025/1/27 21:15
 * @Description: 构造表的XML文件
 */
public class XMLBuilder {
    private static final Logger logger = LoggerFactory.getLogger(XMLBuilder.class);
    private static final String BASE_RESULT_MAP = "base_result_map";
    private static final String BASE_RESULT_COLUMN = "base_result_column";
    private static final String BASE_QUERY_CONDITION = "base_query_condition";

    private static String alias;

    public static void execute(TableInfo tableInfo) {
        // 创建文件输出目录
        File folder = new File(Constants.PATH_XML);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // xml名字
        String xmlName = tableInfo.getBeanName() + Constants.MAPPER_BEAN_SUFFIX;
        // 该表PO全类名
        String beanAllName = Constants.PACKAGE_PO + "." + tableInfo.getBeanName();
        // 创建xml文件
        File xml = new File(folder, xmlName + ".xml");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(xml));

            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n" +
                    "        \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
            bw.newLine();
            bw.write(String.format("<mapper namespace=\"%s\">", Constants.PACKAGE_MAPPERS + "." + xmlName));
            bw.newLine();
            bw.newLine();

            // 构建实体映射
            buildResultMap(tableInfo, bw, beanAllName);
            // 构建基础查询结果字段
            buildBaseResultColumns(tableInfo, bw);
            // 构建基础查询条件
            buildBaseQueryCondition(tableInfo,bw);
            bw.write("</mapper>");

            bw.flush();
        } catch (IOException e) {
            logger.error("生成{}.xml文件失败", xmlName, e);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                logger.error("BufferedWriter关流失败");
            }
        }
    }

    /**
     * @param tableInfo
     * @param bw
     * @param beanAllName
     * @return
     * @description: 构建实体映射
     * @author liuhd
     * 2025/1/31 12:40
     */
    private static void buildResultMap(TableInfo tableInfo, BufferedWriter bw, String beanAllName) throws IOException {
        // 实体映射
        bw.write("\t<!--实体映射 将结果集映射到实体上-->");
        bw.newLine();
        bw.write(String.format("\t<resultMap id=\"" + BASE_RESULT_MAP + "\" type=\"%s\">", beanAllName));
        bw.newLine();

        // 找到主键
        List<String> primaryNameList = new ArrayList<>();
        Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
        Set<Map.Entry<String, List<FieldInfo>>> indexEntry = keyIndexMap.entrySet();
        for (Map.Entry<String, List<FieldInfo>> index : indexEntry) {
            // 组装主键名集合
            if ("PRIMARY".equals(index.getKey())) {
                for (FieldInfo primaryField : index.getValue()) {
                    primaryNameList.add(primaryField.getPropertyName());
                }
                break;
            }
        }

        // 设置resultMap的内容
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            String head = "result";
            if (primaryNameList.contains(fieldInfo.getPropertyName())) {
                head = "id";
            }
            CommentBuilder.buildXMLFieldComment(bw, fieldInfo.getComment());
            bw.write(String.format("\t\t<%s column=\"%s\" property=\"%s\"/>", head, fieldInfo.getFieldName(), fieldInfo.getPropertyName()));
            bw.newLine();
        }

        bw.write("\t</resultMap>");
        bw.newLine();
        bw.newLine();
    }

    /**
     * @description: 构建查询结果字段 即 select后紧跟的字段
     * @param tableInfo
     * @param bw
     * @return
     * @author liuhd
     * 2025/1/31 13:08
     */

    private static void buildBaseResultColumns(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.write("\t<!--所有的查询结果字段-->");
        bw.newLine();

        bw.write("\t<sql id=\"" + BASE_RESULT_COLUMN + "\">");
        bw.newLine();

        alias = StringTools.lowerHead(tableInfo.getTableName(), 1).substring(0,1);
        StringBuilder sb = new StringBuilder("\t\t");
        for (int i = 0; i < tableInfo.getFieldList().size(); i++) {
            sb.append(alias).append(".").append(tableInfo.getFieldList().get(i).getPropertyName());
            if (i != tableInfo.getFieldList().size() -1) sb.append(",");
            if (i != tableInfo.getFieldList().size() -1 && (i + 1) % 5 == 0) sb.append("\n\t\t");
        }
        sb.append("\n\t</sql>");
        bw.write(sb.toString());
        bw.newLine();
        bw.newLine();
    }

    /**
     * @description: 构建基础查询条件 if xxx xxx = #{xxx}
     * @param tableInfo
     * @param bw
     * @return
     * @author liuhd
     * 2025/1/31 13:53
     */
    private static void buildBaseQueryCondition(TableInfo tableInfo,BufferedWriter bw) throws IOException {
        bw.write("\t<!--基础查询条件-->");
        bw.newLine();
        bw.write(String.format("\t<sql id=\"%s\">", BASE_QUERY_CONDITION));
        bw.newLine();
        bw.write("\t\t<where>");
        bw.newLine();

        StringBuilder sb = new StringBuilder();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            String partCondition = "";
            if ("String".equals(fieldInfo.getJavaType())){
                partCondition = String.format(" and query.%s != ''",fieldInfo.getPropertyName());
            }
            sb.append(String.format("\t\t\t<if test = \"query.%s != null%s\">",fieldInfo.getPropertyName(),partCondition));
            sb.append("\n");
            sb.append(String.format("\t\t\t\tand %s.%s = #{query.%s}",alias,fieldInfo.getFieldName(),fieldInfo.getPropertyName()));
            sb.append("\n");
            sb.append("\t\t\t</if>");
            sb.append("\n");
        }

        bw.write(sb.toString());

        bw.write("\t\t</where>");
        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();
    }

}
