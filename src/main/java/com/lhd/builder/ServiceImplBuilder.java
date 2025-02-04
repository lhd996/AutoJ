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
import java.util.List;
import java.util.Map;

/**
 * @Author: liuhd
 * @Date: 2025/2/4 16:18
 * @Description: 构建服务层实现类
 */
public class ServiceImplBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBuilder.class);

    public static void execute(TableInfo tableInfo) {
        // 创建目录
        File folder = new File(Constants.PATH_SERVICEIMPL);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = tableInfo.getBeanName() + Constants.SERVICEIMPL_BEAN_SUFFIX;
        File serviceFile = new File(folder,fileName + ".java");

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(serviceFile));
            // package
            bw.write("package " + Constants.PACKAGE_SERVICEIMPL + ";");
            bw.newLine();
            bw.newLine();

            // import
            bw.write("import java.util.List;");
            bw.newLine();
            bw.newLine();

            bw.write("import javax.annotation.Resource;");
            bw.newLine();
            bw.newLine();

            bw.write("import org.springframework.stereotype.Service;");
            bw.newLine();
            bw.newLine();


            String queryName = tableInfo.getBeanName() + Constants.QUERY_BEAN_SUFFIX;
            String mapperName = tableInfo.getBeanName() + Constants.MAPPER_BEAN_SUFFIX;
            String serviceName = tableInfo.getBeanName() + Constants.SERVICE_BEAN_SUFFIX;

            bw.write(String.format("import %s.PageSize;\n",Constants.PACKAGE_ENUM));
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_QUERY,queryName));
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_PO,tableInfo.getBeanName()));
            bw.write(String.format("import %s.PaginationResultVO;\n",Constants.PACKAGE_VO));
            bw.write(String.format("import %s.SimplePage;\n",Constants.PACKAGE_QUERY));
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_MAPPERS,mapperName));
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_SERVICE,serviceName));
            bw.write(String.format("import %s.StringTools;\n",Constants.PACKAGE_UTILS));



            if (tableInfo.getHaveDate() || tableInfo.getHaveDateTime()) {
                bw.write("import java.util.Date;");
                bw.newLine();
            }
            if (tableInfo.getHaveBigDecimal()) {
                bw.write("import java.math.BigDecimal;");
                bw.newLine();
            }
            bw.newLine();

            CommentBuilder.buildClassComment(bw,tableInfo.getBeanName() + "业务接口实现类");
            bw.write("@Service");
            bw.newLine();
            bw.write(String.format("public class %s implements %s { ",fileName,serviceName));
            bw.newLine();

            bw.write("\t@Resource");
            bw.newLine();
            bw.write(String.format("\tprivate %s<%s, %s> %s;",mapperName,tableInfo.getBeanName(),queryName, StringTools.lowerHead(mapperName,1)));
            bw.newLine();

            // 构建方法
            // base部分
            CommentBuilder.buildMethodComment(bw,"根据条件查询列表");
            bw.write("\t@Override");
            bw.newLine();
            bw.write(String.format("\tpublic List<%s> findListByQuery(%s query){",tableInfo.getBeanName(),queryName));
            bw.newLine();
            bw.write(String.format("\t\treturn %s.selectList(query);",StringTools.lowerHead(mapperName,1)));
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"根据条件查询数量");
            bw.write("\t@Override");
            bw.newLine();
            bw.write(String.format("\tpublic Long findCountByQuery(%s query){",queryName));
            bw.newLine();
            bw.write(String.format("\t\treturn %s.selectCount(query);",StringTools.lowerHead(mapperName,1)));
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"分页查询");
            bw.write("\t@Override");
            bw.newLine();
            bw.write(String.format("\tpublic PaginationResultVO<%s> findListByPage(%s query){",tableInfo.getBeanName(),queryName));
            bw.newLine();
            bw.write(String.format("\t\t// 查出查询的结果数量\n" +
                    "\t\tlong count = findCountByQuery(query);\n" +
                    "\t\t// 如果没给pageSize 默认是15\n" +
                    "\t\tlong pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();\n" +
                    "\t\t// 构建SimplePage 可以确定limit的index与offset以及总页数 以及真正的pageNo\n" +
                    "\t\tSimplePage page = new SimplePage(query.getPageNo(), count, pageSize);\n" +
                    "\t\t// 设置到query中 xml中会使用query.simplePage.start与query.simplePage.end\n" +
                    "\t\tquery.setSimplePage(page);\n" +
                    "\t\t// 用query查询 此时是分页条件查询\n" +
                    "\t\tList<%s> list = this.findListByQuery(query);\n" +
                    "\t\t// 将结果塞到PaginationResultVO中\n" +
                    "\t\tPaginationResultVO<%s> result = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);\n" +
                    "\t\treturn result;",tableInfo.getBeanName(),tableInfo.getBeanName()));
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"新增");
            bw.write("\t@Override");
            bw.newLine();
            bw.write(String.format("\tpublic Long add(%s bean){",tableInfo.getBeanName()));
            bw.newLine();
            bw.write(String.format("\t\treturn %s.insert(bean);",StringTools.lowerHead(mapperName,1)));
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"新增或更新");
            bw.write("\t@Override");
            bw.newLine();
            bw.write(String.format("\tpublic Long addOrUpdate(%s bean){",tableInfo.getBeanName()));
            bw.newLine();
            bw.write(String.format("\t\treturn %s.insertOrUpdate(bean);",StringTools.lowerHead(mapperName,1)));
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"批量新增");
            bw.write("\t@Override");
            bw.newLine();
            bw.write(String.format("\tpublic Long addBatch(List<%s> listBean){",tableInfo.getBeanName()));
            bw.newLine();
            bw.write(String.format("\t\tif (listBean == null || listBean.isEmpty()) {\n" +
                    "\t\t\treturn 0L;\n" +
                    "\t\t}\n" +
                    "\t\treturn %s.insertBatch(listBean);",StringTools.lowerHead(mapperName,1)));
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"批量新增或更新");
            bw.write("\t@Override");
            bw.newLine();
            bw.write(String.format("\tpublic Long addOrUpdateBatch(List<%s> listBean){",tableInfo.getBeanName()));
            bw.newLine();
            bw.write(String.format("\t\tif (listBean == null || listBean.isEmpty()) {\n" +
                    "\t\t\treturn 0L;\n" +
                    "\t\t}\n" +
                    "\t\treturn %s.insertOrUpdateBatch(listBean);",StringTools.lowerHead(mapperName,1)));
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"多条件更新");
            bw.write("\t@Override");
            bw.newLine();
            bw.write(String.format("\tpublic Long updateByQuery(%s bean, %s query){",tableInfo.getBeanName(),queryName));
            bw.newLine();
            bw.write(String.format("\t\treturn %s.updateByQuery(bean, query);",StringTools.lowerHead(mapperName,1)));
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildMethodComment(bw,"多条件删除");
            bw.write("\t@Override");
            bw.newLine();
            bw.write(String.format("\tpublic Long deleteByQuery(%s query){",queryName));
            bw.newLine();
            bw.write(String.format("\t\treturn %s.deleteByQuery(query);",StringTools.lowerHead(mapperName,1)));
            bw.newLine();
            bw.write("\t}");
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
                // 拼接方法调用的参数列表
                StringBuilder invokeParamList = new StringBuilder();
                for (int i = 0; i < indexList.size(); i++) {
                    String tail = ", ";
                    if (i == indexList.size() - 1) tail = "";
                    invokeParamList.append(indexList.get(i).getPropertyName()).append(tail);
                }
                // select
                CommentBuilder.buildMethodComment(bw,String.format("根据%s查询",keyName));
                bw.write("\t@Override");
                bw.newLine();
                bw.write(String.format("\tpublic %s get%sBy%s(%s){",tableInfo.getBeanName(),tableInfo.getBeanName(),methodNameSuffix,paramList));
                bw.newLine();
                bw.write(String.format("\t\treturn %s.selectBy%s(%s);",StringTools.lowerHead(mapperName,1),methodNameSuffix,invokeParamList));
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();

                // delete
                CommentBuilder.buildMethodComment(bw,String.format("根据%s删除",keyName));
                bw.write("\t@Override");
                bw.newLine();
                bw.write(String.format("\tpublic Long delete%sBy%s(%s){",tableInfo.getBeanName(),methodNameSuffix,paramList));
                bw.newLine();
                bw.write(String.format("\t\treturn %s.deleteBy%s(%s);",StringTools.lowerHead(mapperName,1),methodNameSuffix,invokeParamList));
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();

                // update
                StringBuilder head1 = new StringBuilder();
                head1.append(String.format("%s bean, ",tableInfo.getBeanName()));
                paramList = head1.append(paramList);

                StringBuilder head2 = new StringBuilder();
                head2.append("bean, ");
                invokeParamList = head2.append(invokeParamList);

                CommentBuilder.buildMethodComment(bw,String.format("根据%s更新",keyName));
                bw.write("\t@Override");
                bw.newLine();
                bw.write(String.format("\tpublic Long update%sBy%s(%s){",tableInfo.getBeanName(),methodNameSuffix,paramList));
                bw.newLine();
                bw.write(String.format("\t\treturn %s.updateBy%s(%s);",StringTools.lowerHead(mapperName,1),methodNameSuffix,invokeParamList));
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();

            }

            bw.write("}");

        } catch (IOException e) {
            logger.error("Service类创建异常");
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

}
