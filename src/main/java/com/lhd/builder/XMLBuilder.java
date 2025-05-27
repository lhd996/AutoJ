package com.lhd.builder;

import com.lhd.bean.Constants;
import com.lhd.bean.ExtendField;
import com.lhd.bean.FieldInfo;
import com.lhd.bean.TableInfo;
import com.lhd.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

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

    // 表中自增主键字段
    private static FieldInfo autoIncrementField = null;

    // 表中主键字段
    private static List<FieldInfo> primaryList = null;

    private static String alias;

    public static void execute(TableInfo tableInfo) {
        // 创建文件输出目录
        File folder = new File(Constants.PATH_XML);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // 找到自增字段
        tableInfo.getFieldList().forEach(item -> {
            if (item.getAutoIncrement()) {
                autoIncrementField = item;
            }
        });
        System.out.println(autoIncrementField);
        // 找到主键
        primaryList = null;
        Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            if ("PRIMARY".equals(entry.getKey())) {
                primaryList = entry.getValue();
                break;
            }
        }


        // xml名字
        String xmlName = tableInfo.getBeanName() + Constants.MAPPER_BEAN_SUFFIX;
        // 该表PO全类名
        String beanAllName = Constants.PACKAGE_PO + "." + tableInfo.getBeanName();
        // 创建xml文件
        File xml = new File(folder, xmlName + ".xml");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(xml.toPath()), StandardCharsets.UTF_8));

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
            buildBaseQueryCondition(tableInfo, bw);
            // 构建selectList方法
            buildSelectList(tableInfo, bw);
            // 构建selectCount方法
            buildSelectCount(tableInfo, bw);
            // 构建insert方法
            buildInsert(tableInfo, bw);
            // 构建insertOrUpdate方法
            buildInsertOrUpdate(tableInfo, bw);
            // 构建insertBatch方法
            buildInsertBatch(tableInfo, bw);
            // 构建insertOrUpdateBatch方法
            buildInsertOrUpdateBatch(tableInfo,bw);
            // 构建多条件更新方法
            buildUpdateByQuery(tableInfo,bw);
            // 构建多条件删除方法
            buildDeleteByQuery(tableInfo,bw);
            // 构建唯一索引的select方法
            buildUniqueIndexSelect(tableInfo,bw);
            // 构建唯一索引的delete方法
            buildUniqueIndexDelete(tableInfo,bw);
            // 构建唯一索引的update方法
            buildUniqueIndexUpdate(tableInfo,bw);

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

    private static void buildUniqueIndexUpdate(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            String keyName = entry.getKey();
            List<FieldInfo> indexList = entry.getValue();

            // 方法名
            StringBuilder methodName = new StringBuilder();
            MapperBuilder.joinMethodName(methodName,indexList);
            methodName.delete(methodName.indexOf("("),methodName.indexOf("(") + 1);
            // 参数 xxx = #{xxx}
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indexList.size(); i++) {
                String tail = " and ";
                if (i == indexList.size() - 1) tail = "";
                sb.append(indexList.get(i).getFieldName()).append(" = ").append(String.format("#{%s}",indexList.get(i).getPropertyName())).append(tail);
            }
            // 条件 <if test >
            StringBuilder condition = new StringBuilder();
            // 不能更新自己
            List<FieldInfo> filterList = tableInfo.getFieldList().stream().filter(item -> !indexList.contains(item)).collect(Collectors.toList());
            for (FieldInfo fieldInfo : filterList) {
                condition.append(String.format("\t\t\t<if test=\"bean.%s != null\">\n" +
                        "\t\t\t\t%s = #{bean.%s},\n" +
                        "\t\t\t</if>\n",fieldInfo.getPropertyName(),fieldInfo.getFieldName(),fieldInfo.getPropertyName()));
            }
            CommentBuilder.builderXMLComment(bw,String.format("根据%s更新",keyName));

            bw.write(String.format("\t<update id=\"updateBy%s\">\n" +
                    "\t\tUPDATE %s\n" +
                    "\t\t<set>\n" +
                    "%s" +
                    "\t\t</set>\n" +
                    "\t\twhere %s\n" +
                    "\t</update>",methodName,tableInfo.getTableName(),condition,sb));

            bw.newLine();
            bw.newLine();
        }
    }
    /**
     * @description: 构建唯一索引的delete方法
     * @param tableInfo
     * @param bw
     * @return
     * @author liuhd
     * 2025/2/3 20:16
     */
    private static void buildUniqueIndexDelete(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            String keyName = entry.getKey();
            List<FieldInfo> indexList = entry.getValue();

            // 方法名
            StringBuilder methodName = new StringBuilder();
            MapperBuilder.joinMethodName(methodName,indexList);
            methodName.delete(methodName.indexOf("("),methodName.indexOf("(") + 1);
            // 参数 xxx = #{xxx}
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indexList.size(); i++) {
                String tail = " and ";
                if (i == indexList.size() - 1) tail = "";
                sb.append(indexList.get(i).getFieldName()).append(" = ").append(String.format("#{%s}",indexList.get(i).getPropertyName())).append(tail);
            }

            CommentBuilder.builderXMLComment(bw,String.format("根据%s删除",keyName));
            bw.write(String.format("\t<delete id=\"deleteBy%s\">\n" +
                    "\t\tdelete\n" +
                    "\t\tfrom %s where %s\n" +
                    "\t</delete>", methodName,tableInfo.getTableName(),sb));
            bw.newLine();
            bw.newLine();
        }
    }


    /**
     * @description: 构建唯一索引的select方法
     * @param tableInfo
     * @param bw
     * @return
     * @author liuhd
     * 2025/2/3 19:09
     */
    private static void buildUniqueIndexSelect(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            String keyName = entry.getKey();
            List<FieldInfo> indexList = entry.getValue();

            // 方法名
            StringBuilder methodName = new StringBuilder();
            MapperBuilder.joinMethodName(methodName,indexList);
            methodName.delete(methodName.indexOf("("),methodName.indexOf("(") + 1);
            // 参数 xxx = #{xxx}
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indexList.size(); i++) {
                String tail = " and ";
                if (i == indexList.size() - 1) tail = "";
                sb.append(indexList.get(i).getFieldName()).append(" = ").append(String.format("#{%s}",indexList.get(i).getPropertyName())).append(tail);
            }

            CommentBuilder.builderXMLComment(bw,String.format("根据%s查询",keyName));
            bw.write(String.format("\t<select id=\"selectBy%s\" resultMap=\"%s\">\n" +
                    "\t\tselect\n" +
                    "\t\t<include refid=\"%s\"/>\n" +
                    "\t\tfrom %s %s where %s\n" +
                    "\t</select>", methodName,BASE_RESULT_MAP,BASE_RESULT_COLUMN,tableInfo.getTableName(),alias,sb));
            bw.newLine();
            bw.newLine();
        }
    }

    /**
     * @description: 构建多条件删除方法
     * @param tableInfo
     * @param bw
     * @return
     * @author liuhd
     * 2025/2/3 19:48
     */
    private static void buildDeleteByQuery(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        CommentBuilder.builderXMLComment(bw,"多条件删除");
        bw.write(String.format("\t<delete id=\"deleteByQuery\">\n" +
                "\t\tdelete from %s %s\n" +
                "\t\t<include refid=\"%s\"/>\n" +
                "\t</delete>",tableInfo.getTableName(),alias,BASE_QUERY_CONDITION));
        bw.newLine();
        bw.newLine();
    }


    /**
     * @param tableInfo 构建多条件更新方法
     * @param bw
     * @return
     * @author liuhd
     * 2025/2/3 19:18
     */
    private static void buildUpdateByQuery(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        CommentBuilder.builderXMLComment(bw,"多条件更新方法");
        bw.write("\t<update id=\"updateByQuery\">");
        bw.newLine();

        bw.write(String.format("\t\tUPDATE %s %s",tableInfo.getTableName(),alias));
        bw.newLine();

        StringBuilder sb = new StringBuilder();
        sb.append("\t\t<set>\n");

        // 只删除不含主键的部分
        List<FieldInfo> fieldInfoList = tableInfo.getFieldList();
        // 过滤出来
        List<FieldInfo> filteredFieldInfoList = fieldInfoList.stream().filter((item) -> !primaryList.contains(item)).collect(Collectors.toList());
        for (FieldInfo fieldInfo : filteredFieldInfoList) {
            sb.append(String.format("\t\t\t<if test=\"bean.%s != null\">\n" +
                    "\t\t\t\t%s = #{bean.%s},\n" +
                    "\t\t\t</if>\n",fieldInfo.getPropertyName(),fieldInfo.getFieldName(),fieldInfo.getPropertyName()));
        }
        sb.append("\t\t</set>\n");
        sb.append(String.format("\t\t<include refid=\"%s\"/>",BASE_QUERY_CONDITION));

        bw.write(sb.toString());
        bw.newLine();

        bw.write("\t</update>");
        bw.newLine();
        bw.newLine();
    }

    /**
     * @description: 构建insertOrUpdateBatch方法
     * @param tableInfo
     * @param bw
     * @return
     * @author liuhd
     * 2025/2/3 18:47
     */
    private static void buildInsertOrUpdateBatch(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        CommentBuilder.builderXMLComment(bw,"批量新增或修改");

        String useGeneratedKeysString = "";
        String keyPropertyString = "";

        // 设置主键回显
        Map<String, String> map = setUseGeneratedKeysAndKeyProperty(autoIncrementField,"insertBatch");
        useGeneratedKeysString = map.get("useGeneratedKeysString");
        keyPropertyString = map.get("keyPropertyString");

        bw.write(String.format("\t<insert id=\"insertOrUpdateBatch\" parameterType=\"%s.%s\" %s %s>",
                Constants.PACKAGE_PO, tableInfo.getBeanName(), useGeneratedKeysString, keyPropertyString));
        bw.newLine();


        bw.write(String.format("\t\tINSERT INTO %s", tableInfo.getTableName()));
        // 拼接括号里面的
        List<FieldInfo> fieldInfoList = tableInfo.getFieldList();
        StringBuilder sb = new StringBuilder();
        sb.append("(\n");
        for (int i = 0; i < fieldInfoList.size(); i++) {
            String tail = ",\n";
            if (i == fieldInfoList.size() - 1) tail = "\n";
            sb.append("\t\t\t").append(fieldInfoList.get(i).getFieldName()).append(tail);
        }
        sb.append("\t\t)");

        bw.write(sb.toString());
        bw.newLine();

        bw.write("\t\tVALUES");
        bw.newLine();

        // 拼接foreach
        sb = new StringBuilder();
        sb.append("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">\n")
                .append("\t\t\t(\n");
        for (int i = 0; i < fieldInfoList.size(); i++) {
            String tail = ",\n";
            if (i == fieldInfoList.size() - 1) tail = "\n";
            sb.append(String.format("\t\t\t#{item.%s}", fieldInfoList.get(i).getPropertyName())).append(tail);
        }
        sb.append("\t\t\t)\n");
        sb.append("\t\t</foreach>");

        bw.write(sb.toString());
        bw.newLine();


        bw.write("\t\ton DUPLICATE key update");
        bw.newLine();

        sb = new StringBuilder();

        for (int i = 0; i < fieldInfoList.size(); i++) {
            if (autoIncrementField != null && fieldInfoList.get(i).getPropertyName().equals(autoIncrementField.getPropertyName()))
                continue;
            String tail = ",\n";
            if (i == fieldInfoList.size() - 1) tail = "\n";
            sb.append("\t\t").append(fieldInfoList.get(i).getFieldName()).append(" = ").append(String.format("VALUES(%s)",fieldInfoList.get(i).getFieldName())).append(tail);
        }

        bw.write(sb.toString());

        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();
    }

    /**
     * @param tableInfo
     * @param bw
     * @return
     * @description: 创建批量插入方法
     * @author liuhd
     * 2025/2/2 21:57
     */
    private static void buildInsertBatch(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        String useGeneratedKeysString = "";
        String keyPropertyString = "";

        // 设置主键回显
        Map<String, String> map = setUseGeneratedKeysAndKeyProperty(autoIncrementField,"insertBatch");
        useGeneratedKeysString = map.get("useGeneratedKeysString");
        keyPropertyString = map.get("keyPropertyString");

        CommentBuilder.builderXMLComment(bw, "批量插入");
        bw.write(String.format("\t<insert id=\"insertBatch\" parameterType=\"%s.%s\" %s %s>",
                Constants.PACKAGE_PO, tableInfo.getBeanName(), useGeneratedKeysString, keyPropertyString));
        bw.newLine();


        bw.write(String.format("\t\tINSERT INTO %s", tableInfo.getTableName()));
        // 拼接括号里面的
        List<FieldInfo> fieldInfoList = tableInfo.getFieldList();
        StringBuilder sb = new StringBuilder();
        sb.append("(\n");
        for (int i = 0; i < fieldInfoList.size(); i++) {
            if (autoIncrementField != null && fieldInfoList.get(i).getPropertyName().equals(autoIncrementField.getPropertyName()))
                continue;
            String tail = ",\n";
            if (i == fieldInfoList.size() - 1) tail = "\n";
            sb.append("\t\t\t").append(fieldInfoList.get(i).getFieldName()).append(tail);
        }
        sb.append("\t\t)");

        bw.write(sb.toString());
        bw.newLine();

        bw.write("\t\tVALUES");
        bw.newLine();

        // 拼接foreach
        sb = new StringBuilder();
        sb.append("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">\n")
                .append("\t\t\t(\n");
        for (int i = 0; i < fieldInfoList.size(); i++) {
            if (autoIncrementField != null && fieldInfoList.get(i).getPropertyName().equals(autoIncrementField.getPropertyName()))
                continue;
            String tail = ",\n";
            if (i == fieldInfoList.size() - 1) tail = "\n";
            sb.append(String.format("\t\t\t#{item.%s}", fieldInfoList.get(i).getPropertyName())).append(tail);
        }
        sb.append("\t\t\t)\n");
        sb.append("\t\t</foreach>");

        bw.write(sb.toString());
        bw.newLine();

        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();
    }

    /**
     * @param tableInfo
     * @param bw
     * @return
     * @description: 插入或者更新 只插入或更新bean中有值的字段
     * @author liuhd
     * 2025/2/2 20:11
     */
    private static void buildInsertOrUpdate(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        String useGeneratedKeysString = "";
        String keyPropertyString = "";

        // 设置主键回显
        Map<String, String> map = setUseGeneratedKeysAndKeyProperty(autoIncrementField,"insertOrUpdate");
        useGeneratedKeysString = map.get("useGeneratedKeysString");
        keyPropertyString = map.get("keyPropertyString");


        CommentBuilder.builderXMLComment(bw, "插入或者更新 （只插入或更新bean中有值的字段）");
        bw.write(String.format("\t<insert id=\"insertOrUpdate\" parameterType=\"%s.%s\" %s %s>",
                Constants.PACKAGE_PO,
                tableInfo.getBeanName(),
                useGeneratedKeysString,
                keyPropertyString));
        bw.newLine();

        bw.write(String.format("\t\tINSERT INTO %s", tableInfo.getTableName()));
        bw.newLine();

        bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        bw.newLine();

        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            bw.write(String.format("\t\t\t<if test=\"bean.%s != null\">\n" +
                    "\t\t\t\t%s,\n" +
                    "\t\t\t</if>", fieldInfo.getPropertyName(), fieldInfo.getFieldName()));
            bw.newLine();
        }

        bw.write("\t\t</trim>");
        bw.newLine();

        bw.write("\t\tVALUES");
        bw.newLine();

        bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        bw.newLine();

        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            bw.write(String.format("\t\t\t<if test=\"bean.%s != null\">\n" +
                    "\t\t\t\t#{bean.%s},\n" +
                    "\t\t\t</if>", fieldInfo.getPropertyName(), fieldInfo.getPropertyName()));
            bw.newLine();
        }

        bw.write("\t\t</trim>");
        bw.newLine();

        bw.write("\t\ton DUPLICATE key update");
        bw.newLine();

        bw.write("\t\t<trim suffixOverrides=\",\">");
        bw.newLine();

        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (autoIncrementField != null && fieldInfo.getPropertyName().equals(autoIncrementField.getPropertyName()))
                continue;
            bw.write(String.format("\t\t\t<if test=\"bean.%s != null\">\n" +
                    "\t\t\t\t%s = VALUES(%s),\n" +
                    "\t\t\t</if>", fieldInfo.getPropertyName(), fieldInfo.getFieldName(), fieldInfo.getFieldName()));
            bw.newLine();
        }

        bw.write("\t\t</trim>");
        bw.newLine();

        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();
    }

    /**
     * @param tableInfo
     * @param bw
     * @return
     * @description: 构建insert方法 只插入bean有值的部分
     * @author liuhd
     * 2025/2/2 17:14
     */
    private static void buildInsert(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        String useGeneratedKeysString = "";
        String keyPropertyString = "";

        // 设置主键回显
        Map<String, String> map = setUseGeneratedKeysAndKeyProperty(autoIncrementField,"insert");
        useGeneratedKeysString = map.get("useGeneratedKeysString");
        keyPropertyString = map.get("keyPropertyString");

        CommentBuilder.builderXMLComment(bw, "插入 （只插入bean中有值的字段）");
        bw.write(String.format("\t<insert id=\"insert\" parameterType=\"%s.%s\" %s %s>",
                Constants.PACKAGE_PO,
                tableInfo.getBeanName(),
                useGeneratedKeysString,
                keyPropertyString));
        bw.newLine();

        bw.write(String.format("\t\tINSERT INTO %s", tableInfo.getTableName()));
        bw.newLine();

        bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        bw.newLine();

        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (autoIncrementField != null && fieldInfo.getPropertyName().equals(autoIncrementField.getPropertyName()))
                continue;
            bw.write(String.format("\t\t\t<if test=\"bean.%s != null\">\n" +
                    "\t\t\t\t%s,\n" +
                    "\t\t\t</if>", fieldInfo.getPropertyName(), fieldInfo.getFieldName()));
            bw.newLine();
        }

        bw.write("\t\t</trim>");
        bw.newLine();

        bw.write("\t\tVALUES");
        bw.newLine();

        bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        bw.newLine();

        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (autoIncrementField != null && fieldInfo.getPropertyName().equals(autoIncrementField.getPropertyName()))
                continue;
            bw.write(String.format("\t\t\t<if test=\"bean.%s != null\">\n" +
                    "\t\t\t\t#{bean.%s},\n" +
                    "\t\t\t</if>", fieldInfo.getPropertyName(), fieldInfo.getPropertyName()));
            bw.newLine();
        }

        bw.write("\t\t</trim>");
        bw.newLine();

        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();
    }

    /*
     * @description: 构建SelectCount方法
     * @param null
     * @return null
     * @author liuhd
     * 2025/2/1 11:38
     */
    private static void buildSelectCount(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.write(String.format("\t<!-- 查询数量-->\n" +
                "\t<select id=\"selectCount\" resultType=\"java.lang.Long\" >\n" +
                "\t\t SELECT count(*) FROM %s %s <include refid=\"%s\" />\n" +
                "\t</select>\n", tableInfo.getTableName(), alias, BASE_QUERY_CONDITION));
        bw.newLine();
        bw.newLine();
    }

    private static void buildSelectList(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.write(String.format("\t<!-- 查询集合-->\n" +
                "\t<select id=\"selectList\" resultMap=\"%s\" >\n" +
                "\t\tSELECT\n" +
                "\t\t<include refid=\"%s\"/>\n" +
                "\t\tFROM %s %s\n" +
                "\t\t<include refid=\"%s\"/>\n" +
                "\t\t<if test=\"query.orderBy!=null\">\n" +
                "\t\t\torder by ${query.orderBy}\n" +
                "\t\t</if>\n" +
                "\t\t<if test=\"query.simplePage!=null\">\n" +
                "\t\t\tlimit #{query.simplePage.start},#{query.simplePage.end}\n" +
                "\t\t</if>\n" +
                "\t</select>\n", BASE_RESULT_MAP, BASE_RESULT_COLUMN, tableInfo.getTableName(), alias, BASE_QUERY_CONDITION));
        bw.newLine();
        bw.newLine();
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
            CommentBuilder.builderXMLComment(bw, fieldInfo.getComment());
            bw.write(String.format("\t\t<%s column=\"%s\" property=\"%s\"/>", head, fieldInfo.getFieldName(), fieldInfo.getPropertyName()));
            bw.newLine();
        }

        bw.write("\t</resultMap>");
        bw.newLine();
        bw.newLine();
    }

    /**
     * @param tableInfo
     * @param bw
     * @return
     * @description: 构建查询结果字段 即 select后紧跟的字段
     * @author liuhd
     * 2025/1/31 13:08
     */

    private static void buildBaseResultColumns(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.write("\t<!--所有的查询结果字段-->");
        bw.newLine();

        bw.write("\t<sql id=\"" + BASE_RESULT_COLUMN + "\">");
        bw.newLine();

        alias = StringTools.lowerHead(tableInfo.getTableName(), 1).substring(0, 1);
        StringBuilder sb = new StringBuilder("\t\t");
        for (int i = 0; i < tableInfo.getFieldList().size(); i++) {
            sb.append(alias).append(".").append(tableInfo.getFieldList().get(i).getFieldName());
            if (i != tableInfo.getFieldList().size() - 1) sb.append(",");
            if (i != tableInfo.getFieldList().size() - 1 && (i + 1) % 5 == 0) sb.append("\n\t\t");
        }
        sb.append("\n\t</sql>");
        bw.write(sb.toString());
        bw.newLine();
        bw.newLine();
    }

    /**
     * @param tableInfo
     * @param bw
     * @return
     * @description: 构建基础查询条件 if xxx xxx = #{xxx}
     * @author liuhd
     * 2025/1/31 13:53
     */
    private static void buildBaseQueryCondition(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.write("\t<!--基础查询条件-->");
        bw.newLine();
        bw.write(String.format("\t<sql id=\"%s\">", BASE_QUERY_CONDITION));
        bw.newLine();
        bw.write("\t\t<where>");
        bw.newLine();

        // 生成查询条件
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            String partCondition = "";
            if ("String".equals(fieldInfo.getJavaType())) {
                partCondition = String.format(" and query.%s != ''", fieldInfo.getPropertyName());
            }
            sb.append(String.format("\t\t\t<if test=\"query.%s != null%s\">", fieldInfo.getPropertyName(), partCondition));
            sb.append("\n");
            sb.append(String.format("\t\t\t\tand %s.%s = #{query.%s}", alias, fieldInfo.getFieldName(), fieldInfo.getPropertyName()));
            sb.append("\n");
            sb.append("\t\t\t</if>");
            sb.append("\n");

            // 额外查询条件
            Set<Map.Entry<FieldInfo, List<ExtendField>>> entries = tableInfo.getExtendFieldMap().entrySet();
            for (Map.Entry<FieldInfo, List<ExtendField>> entry : entries) {
                if (entry.getKey().getPropertyName().equals(fieldInfo.getPropertyName())) {
                    if ("String".equals(fieldInfo.getJavaType())) {
                        sb1.append(String.format("\t\t\t<if test=\"query.%s!= null  and query.%s!=''\">\n" +
                                        "\t\t\t\tand %s.%s like concat('%s', #{query.%s}, '%s')\n" +
                                        "\t\t\t</if>\n",
                                entry.getValue().get(0).getFieldName(),
                                entry.getValue().get(0).getFieldName(),
                                alias,
                                entry.getKey().getFieldName(),
                                "%",
                                entry.getValue().get(0).getFieldName(),
                                "%"));
                    }
                    if ("Date".equals(fieldInfo.getJavaType())) {
                        sb1.append(String.format("\t\t\t<if test=\"query.%s!= null and query.%s!=''\">\n" +
                                        "\t\t\t\t<![CDATA[ and  %s.%s>=str_to_date(#{query.%s}, '%sY-%sm-%sd') ]]>\n" +
                                        "\t\t\t</if>\n",
                                entry.getValue().get(0).getFieldName(),
                                entry.getValue().get(0).getFieldName(),
                                alias,
                                entry.getKey().getFieldName(),
                                entry.getValue().get(0).getFieldName(),
                                "%",
                                "%",
                                "%"));
                        sb1.append(String.format("\t\t\t<if test=\"query.%s!= null and query.%s!=''\">\n" +
                                        "\t\t\t\t<![CDATA[ and  %s.%s<str_to_date(#{query.%s}, '%sY-%sm-%sd') ]]>\n" +
                                        "\t\t\t</if>\n",
                                entry.getValue().get(1).getFieldName(),
                                entry.getValue().get(1).getFieldName(),
                                alias,
                                entry.getKey().getFieldName(),
                                entry.getValue().get(1).getFieldName(),
                                "%",
                                "%",
                                "%"));
                    }

                }
            }
        }


        bw.write(sb.toString());
        bw.write(sb1.toString());

        bw.write("\t\t</where>");
        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();
    }

    /**
     * @param autoIncrementField
     * @param methodName 不同的方法对应的keyProperty不同
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @description: 设置是否主键回显
     * @author liuhd
     * 2025/2/2 22:13
     */
    private static Map<String, String> setUseGeneratedKeysAndKeyProperty(FieldInfo autoIncrementField,String methodName) {
        Map<String, String> map = new HashMap<>();
        if (autoIncrementField != null) {
            map.put("useGeneratedKeysString", String.format("useGeneratedKeys=\"%s\"", "true"));
            if (methodName.contains("Batch")){
                map.put("keyPropertyString", String.format("keyProperty=\"%s\"", autoIncrementField.getPropertyName()));
            }else{
                map.put("keyPropertyString", String.format("keyProperty=\"bean.%s\"", autoIncrementField.getPropertyName()));
            }
        } else {
            map.put("useGeneratedKeysString", "");
            map.put("keyPropertyString", "");
        }
        return map;
    }
}
