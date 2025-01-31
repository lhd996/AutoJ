package com.lhd.builder;

import com.lhd.bean.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: liuhd
 * @Date: 2025/1/29 16:43
 * @Description: 模板构造器 生成固定的java文件
 */
public class TemplateBuilder {
    public static final Logger logger = LoggerFactory.getLogger(TemplateBuilder.class);

    public static void execute(){
        List<String> fileHeadList = new ArrayList<>();
        // 枚举
        fileHeadList.add("package " + Constants.PACKAGE_ENUM + ";");
        build(fileHeadList,"DateTimePatternEnum", Constants.PATH_ENUM);
        build(fileHeadList,"ResponseCodeEnum", Constants.PATH_ENUM);
        build(fileHeadList,"PageSize", Constants.PATH_ENUM);
        // Controller
        fileHeadList.clear();
        fileHeadList.add("package " + Constants.PACKAGE_CONTROLLER + ";");
        fileHeadList.add("import " + Constants.PACKAGE_ENUM + ".ResponseCodeEnum;");
        fileHeadList.add("import " + Constants.PACKAGE_VO + ".ResponseVO;");
        fileHeadList.add("import " + Constants.PACKAGE_EXCEPTION + ".BusinessException;");
        build(fileHeadList,"ABaseController",Constants.PATH_CONTROLLER);
        build(fileHeadList,"AGlobalExceptionHandlerController",Constants.PATH_CONTROLLER);
        // Utils
        fileHeadList.clear();
        fileHeadList.add("package " + Constants.PACKAGE_UTILS + ";");
        build(fileHeadList,"DateUtil",Constants.PATH_UTILS);
        fileHeadList.add("import " + Constants.PACKAGE_EXCEPTION + ".BusinessException;");
        build(fileHeadList,"StringTools",Constants.PATH_UTILS);
        // Exception
        fileHeadList.clear();
        fileHeadList.add("package " + Constants.PACKAGE_EXCEPTION + ";");
        fileHeadList.add("import " + Constants.PACKAGE_ENUM + ".ResponseCodeEnum;");
        build(fileHeadList,"BusinessException",Constants.PATH_EXCEPTION);
        // Mapper
        fileHeadList.clear();
        fileHeadList.add("package " + Constants.PACKAGE_MAPPERS + ";");
        build(fileHeadList,"BaseMapper",Constants.PATH_MAPPERS);
        // query
        fileHeadList.clear();
        fileHeadList.add("package " + Constants.PACKAGE_QUERY + ";");
        fileHeadList.add("import " + Constants.PACKAGE_ENUM + ".PageSize;");
        build(fileHeadList,"SimplePage",Constants.PATH_QUERY);
        build(fileHeadList,"BaseParam",Constants.PATH_QUERY);
        // vo
        fileHeadList.clear();
        fileHeadList.add("package " + Constants.PACKAGE_VO + ";");
        build(fileHeadList,"ResponseVO",Constants.PATH_VO);
        build(fileHeadList,"PaginationResultVO",Constants.PATH_VO);
    }

    /**
     * @description: 将模板文件变成java文件
     * @param fileHeadList 需要补充的文件头
     * @param fileName 源
     * @param targetPath 目标
     * @return
     * @author liuhd
     * 2025/1/29 17:03
     */

    public static void build(List<String> fileHeadList,String fileName,String targetPath){
        BufferedWriter bw = null;
        InputStream is = null;
        BufferedReader br = null;


        try {
            File folder = new File(targetPath);
            if (!folder.exists()){
                folder.mkdirs();
            }
            File enumFile = new File(folder, fileName + ".java");
            // 输出流
            bw = new BufferedWriter(new FileWriter(enumFile));
            // 输入流
            is = TemplateBuilder.class.getClassLoader().getResourceAsStream("template/" + fileName + ".txt");
            br = new BufferedReader(new InputStreamReader(is));

            // 写入头
            if (!fileHeadList.isEmpty()){
                for (String fileHead : fileHeadList) {
                    bw.write(fileHead);
                    bw.newLine();
                }
            }

            String line;
            while ((line = br.readLine()) != null){
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            logger.error("文件{}生成失败",fileName,e);
        }finally {
            // 关流
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                is.close()
                ;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
