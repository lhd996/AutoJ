package com.lhd;

import com.lhd.bean.TableInfo;
import com.lhd.builder.*;

import java.util.List;

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
        }
    }
}
