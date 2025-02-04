package com.lhd;

import com.lhd.bean.ExtendField;
import com.lhd.bean.TableInfo;
import com.lhd.builder.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Author: liuhd
 * @Date: 2025/1/27 15:22
 * @Description: 启动类
 */
public class RunApplication {
    public static void main(String[] args) {
        List<TableInfo> tableInfoList = TableBuilder.getTables();
        for (TableInfo tableInfo : tableInfoList) {
            TemplateBuilder.execute();
            PoBuilder.execute(tableInfo);
            QueryBuilder.execute(tableInfo);
            MapperBuilder.execute(tableInfo);
            XMLBuilder.execute(tableInfo);
            ServiceBuilder.execute(tableInfo);
            ServiceImplBuilder.execute(tableInfo);
            ControllerBuilder.execute(tableInfo);
        }
    }
}
