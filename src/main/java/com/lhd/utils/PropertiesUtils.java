package com.lhd.utils;

import com.lhd.builder.TableBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: liuhd
 * @Date: 2025/1/27 14:37
 * @Description: 读取配置文件
 */
public class PropertiesUtils {
    private static final Properties props = new Properties();
    private static final Map<String,String> PROPER_MAP = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(TableBuilder.class);

    static {
        InputStream is = null;
        try {
            // 通过类加载器获取输入流
            is = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.properties");
            // 将数据加载到props中
            props.load(is);

            // 将数据迁移到PROPER_MAP中
            Set<Map.Entry<Object, Object>> entries = props.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                PROPER_MAP.put((String) entry.getKey(), (String) entry.getValue());
            }

        }catch (Exception e){
            logger.error("数据从Properties迁移到ConcurrentHashMap失败",e);
        }finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("输入流关流失败",e);
                }
            }
        }
    }

    public static  String getString(String key){
        return PROPER_MAP.get(key);
    }
}
