package com.lhd.builder;

import com.lhd.bean.Constants;
import com.lhd.bean.FieldInfo;
import com.lhd.bean.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: liuhd
 * @Date: 2025/2/4 12:20
 * @Description: 服务层构建
 */
public class ServiceBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ServiceBuilder.class);

    public static void execute(TableInfo tableInfo) {
        // 创建目录
        File folder = new File(Constants.PATH_SERVICE);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = tableInfo.getBeanName() + Constants.SERVICE_BEAN_SUFFIX;
        File serviceFile = new File(folder,fileName + ".java");

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(serviceFile));
            // package
            bw.write("package " + Constants.PACKAGE_SERVICE + ";");
            bw.newLine();
            bw.newLine();

            // import
            bw.write("import java.util.List;");
            bw.newLine();
            bw.newLine();

            String queryName = tableInfo.getBeanName() + Constants.QUERY_BEAN_SUFFIX;

            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_QUERY,queryName));
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_PO,tableInfo.getBeanName()));
            bw.write(String.format("import %s.PaginationResultVO;\n",Constants.PACKAGE_VO));

            if (tableInfo.getHaveDate() || tableInfo.getHaveDateTime()) {
                bw.write("import java.util.Date;");
                bw.newLine();
            }
            if (tableInfo.getHaveBigDecimal()) {
                bw.write("import java.math.BigDecimal;");
                bw.newLine();
            }
            bw.newLine();

            CommentBuilder.buildClassComment(bw,tableInfo.getBeanName() + "业务接口");
            bw.write(String.format("public interface %s {",fileName));
            bw.newLine();

            // 构建方法
             // base部分
            CommentBuilder.buildMethodComment(bw,"根据条件查询列表");
            bw.write(String.format("\tList<%s> findListByQuery(%s query);",tableInfo.getBeanName(),queryName));
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"根据条件查询数量");
            bw.write(String.format("\tInteger findCountByQuery(%s query);",queryName));
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"分页查询");
            bw.write(String.format("\tPaginationResultVO<%s> findListByPage(%s query);",tableInfo.getBeanName(),queryName));
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"新增");
            bw.write(String.format("\tInteger add(%s bean);",tableInfo.getBeanName()));
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"批量新增");
            bw.write(String.format("\tInteger addOrUpdateBatch(List<%s> listBean);",tableInfo.getBeanName()));
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"多条件更新");
            bw.write(String.format("\tInteger updateByQuery(%s bean, %s query);",tableInfo.getBeanName(),queryName));
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"多条件删除");
            bw.write(String.format("\tInteger deleteByQuery(%s query);",queryName));
            bw.newLine();
            bw.newLine();

             // uniqueIndex部分
            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                String keyName = entry.getKey();
                List<FieldInfo> indexList = entry.getValue();

                // 拼接方法后缀
                StringBuilder methodNameSuffix = new StringBuilder();
                MapperBuilder.joinMethodName(methodNameSuffix,indexList);
                methodNameSuffix.delete(methodNameSuffix.indexOf("("),methodNameSuffix.indexOf("(") + 1);
                // 拼接参数列表
                StringBuilder paramList = new StringBuilder();
                for (int i = 0; i < indexList.size(); i++) {
                    String tail = ", ";
                    if (i == indexList.size() - 1) tail = "";
                    paramList.append(indexList.get(i).getJavaType()).append(" ").append(indexList.get(i).getPropertyName()).append(tail);
                }
                // select
                CommentBuilder.buildMethodComment(bw,String.format("根据%s查询",keyName));
                bw.write(String.format("\t%s get%sBy%s(%s);",tableInfo.getBeanName(),tableInfo.getBeanName(),methodNameSuffix,paramList));
                bw.newLine();
                bw.newLine();

                // delete
                CommentBuilder.buildMethodComment(bw,String.format("根据%s删除",keyName));
                bw.write(String.format("\tInteger delete%sBy%s(%s);",tableInfo.getBeanName(),methodNameSuffix,paramList));
                bw.newLine();
                bw.newLine();

                // update
                StringBuilder head = new StringBuilder();
                head.append(String.format("%s bean, ",tableInfo.getBeanName()));
                paramList = head.append(paramList);

                CommentBuilder.buildMethodComment(bw,String.format("根据%s更新",keyName));
                bw.write(String.format("\tInteger update%sBy%s(%s);",tableInfo.getBeanName(),methodNameSuffix,paramList));
                bw.newLine();
                bw.newLine();

            }

            bw.write("}");

        } catch (IOException e) {
            logger.error("Service类创建异常");
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                logger.error("BufferedWriter关流失败");
            }
        }


    }

}
