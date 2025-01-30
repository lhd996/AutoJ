package com.lhd.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @Author: liuhd
 * @Date: 2025/1/27 19:48
 * @Description: json工具类
 */
public class JsonUtils {
    /**
     * @description: 将对象转成json
     * @param obj
     * @return java.lang.String
     * @author liuhd
     * 2025/1/27 19:50
     */
    public static String transferObj2Json(Object obj){
        if (obj == null) return null;
        return JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect);
    }
}
