package com.lhd.builder;

import com.lhd.bean.Constants;
import com.lhd.bean.FieldInfo;
import com.lhd.bean.TableInfo;
import com.lhd.utils.StringTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * @Author: liuhd
 * @Date: 2025/1/27 21:15
 * @Description: 构造表的Mapper类
 */
public class MapperBuilder {
    public static final Logger logger = LoggerFactory.getLogger(MapperBuilder.class);

    public static void execute(TableInfo tableInfo) {
        // 创建文件输出目录
        File folder = new File(Constants.PATH_MAPPERS);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String mapperName = tableInfo.getBeanName() + Constants.MAPPER_BEAN_SUFFIX;
        // 创建mapper-java文件
        File poFile = new File(folder, mapperName + ".java");
        BufferedWriter bw = null;
        try {
            // package import
            bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(poFile.toPath()), StandardCharsets.UTF_8));
            bw.write("package " + Constants.PACKAGE_MAPPERS + ";");
            bw.newLine();
            bw.newLine();

            bw.write("import org.apache.ibatis.annotations.Param;");
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
            CommentBuilder.buildClassComment(bw, tableInfo.getComment() + Constants.MAPPER_BEAN_SUFFIX);
            bw.write("public interface " + mapperName + "<P, Q>" + " extends BaseMapper<P, Q> {");
            bw.newLine();

            // 为索引生成select,update,delete方法
            List<String> list = new ArrayList<>();
            Collections.addAll(list, "select", "delete", "update");

            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            StringBuilder sb;

            for (String solution : list) {
                if (solution.equals("select")) {
                    for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                        sb = new StringBuilder();
                        sb.append("\tP selectBy");
                        List<FieldInfo> indexList = entry.getValue();
                        joinMethodName(sb, indexList);
                        joinParam(sb, indexList);

                        CommentBuilder.buildMethodComment(bw,"根据索引" + entry.getKey() + "查询");
                        bw.write(sb.toString());
                        bw.newLine();
                        bw.newLine();
                    }
                }
                if (solution.equals("update")) {
                    for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                        sb = new StringBuilder();
                        sb.append("\tLong updateBy");

                        List<FieldInfo> indexList = entry.getValue();
                        joinMethodName(sb, indexList);

                        sb.append("@Param(\"bean\") P p,");
                        joinParam(sb, indexList);

                        CommentBuilder.buildMethodComment(bw,"根据索引" + entry.getKey() + "更新");
                        bw.write(sb.toString());
                        bw.newLine();
                        bw.newLine();
                    }
                }
                if (solution.equals("delete")) {
                    for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                        sb = new StringBuilder();
                        sb.append("\tLong deleteBy");
                        List<FieldInfo> indexList = entry.getValue();
                        joinMethodName(sb, indexList);
                        joinParam(sb, indexList);

                        CommentBuilder.buildMethodComment(bw,"根据索引" + entry.getKey() + "删除");
                        bw.write(sb.toString());
                        bw.newLine();
                        bw.newLine();
                    }
                }
            }
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
     * @description: 拼接方法名
     * @param sb
     * @param indexList
     * @return
     * @author liuhd
     * 2025/1/30 17:04
     */
    public static void joinMethodName(StringBuilder sb, List<FieldInfo> indexList) {
        for (int i = 0; i < indexList.size(); i++) {
            if (i != 0) sb.append("And");
            sb.append(StringTools.UpperHead(indexList.get(i).getPropertyName(), 1));
        }
        sb.append("(");
    }

    /**
     * @description: 拼接参数列表部分
     * @param sb
     * @param indexList
     * @return
     * @author liuhd
     * 2025/1/30 16:55
     */
    private static void joinParam(StringBuilder sb, List<FieldInfo> indexList) throws IOException {
        for (int i = 0; i < indexList.size(); i++) {
            sb.append("@Param(\"")
                    .append(indexList.get(i).getPropertyName())
                    .append("\")")
                    .append(" ")
                    .append(indexList.get(i).getJavaType())
                    .append(" ")
                    .append(indexList.get(i).getPropertyName());
            if (i != indexList.size() - 1) sb.append(",");
        }
        sb.append(");");
    }

}
