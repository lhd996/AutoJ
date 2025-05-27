package com.lhd.builder;

import com.lhd.bean.Constants;
import com.lhd.bean.TableInfo;
import com.lhd.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * @Author: liuhd
 * @Date: 2025/2/4 20:04
 * @Description: 构建控制器
 */
public class ControllerBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ControllerBuilder.class);
    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_CONTROLLER);
        if (folder.exists()) {
            folder.mkdirs();
        }
        String fileName = tableInfo.getBeanName() + Constants.CONTROLLER_BEAN_SUFFIX;
        File controllerFile = new File(folder, fileName + ".java");
        BufferedWriter bw = null;
        String serviceName = tableInfo.getBeanName() + Constants.SERVICE_BEAN_SUFFIX;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(controllerFile.toPath()), StandardCharsets.UTF_8));
            // package
            bw.write("package " + Constants.PACKAGE_CONTROLLER + ";");
            bw.newLine();
            bw.newLine();
            // import
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_SERVICE,serviceName));
            bw.write("import org.springframework.web.bind.annotation.RequestMapping;\n" +
                    "import org.springframework.web.bind.annotation.RestController;\n" +
                    "\n" +
                    "import javax.annotation.Resource;");
            bw.newLine();
            bw.newLine();

            CommentBuilder.buildClassComment(bw,tableInfo.getBeanName() + "控制器");

            // 注解
            bw.write(String.format("@RestController\n" +
                    "@RequestMapping(\"/%s\")",tableInfo.getTableName()));
            bw.newLine();

            bw.write(String.format("public class %s extends ABaseController{",fileName));
            bw.newLine();
            bw.newLine();
            bw.write("\t@Resource");
            bw.newLine();
            bw.write(String.format("\tprivate %s %s;",serviceName, StringTools.lowerHead(serviceName,1)));
            bw.newLine();
            bw.write("}");

        } catch (IOException e) {
            logger.error("控制器生成失败");
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
