package com.lhd.builder;

import com.lhd.bean.Constants;
import com.lhd.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;

/**
 * @Author: liuhd
 * @Date: 2025/1/28 15:16
 * @Description: 构造注释
 */
public class CommentBuilder {
    private static final Logger logger = LoggerFactory.getLogger(CommentBuilder.class);
    public static void buildFieldComment(BufferedWriter bw,String comment){
        if (StringUtils.isEmpty(comment)) return;
        try {
            bw.write("\t/**");
            bw.newLine();
            bw.write("\t * " + comment);
            bw.newLine();
            bw.write("\t */");
            bw.newLine();
        } catch (IOException e) {
            logger.error("字段描述生成失败",e);
        }
    }

    public static void builderXMLComment(BufferedWriter bw, String comment){
        if (StringUtils.isEmpty(comment)) return;
        try {
            bw.write(String.format("\t<!-- %s -->",comment));
            bw.newLine();
        } catch (IOException e) {
            logger.error("XML字段描述生成失败",e);
        }
    }

    public static void buildMethodComment(BufferedWriter bw,String comment){
        if (StringUtils.isEmpty(comment)) return;
        try {
            bw.write("\t/**");
            bw.newLine();
            bw.write("\t * " + comment);
            bw.newLine();
            bw.write("\t */");
            bw.newLine();
        } catch (IOException e) {
            logger.error("方法描述生成失败",e);
        }
    }

    public static void buildClassComment(BufferedWriter bw,String comment){
        try {
            bw.write("/**");
            bw.newLine();
            bw.write(" * @Author: " + Constants.AUTHOR);
            bw.newLine();

            String format = DateUtils.format(DateUtils.yyyyMMdd,new Date());
            bw.write(" * @Date: " + format);
            bw.newLine();
            bw.write(" * @Description: " + comment);
            bw.newLine();
            bw.write(" */");
            bw.newLine();
        } catch (IOException e) {
            logger.error("类注释生成失败",e);
        }
    }

}
