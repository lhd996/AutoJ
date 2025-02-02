package com.lhd.bean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
// 表信息bean
public class TableInfo {
    /**
     * 表名
     */
    private String tableName;

    /**
     * bean名称 例如student表对应的bean是Student（转驼峰）
     */
    private String beanName;

    /**
     * 查询参数的名字 例如StudentQuery
     */
    private String beanParamName;

    /**
     * 表注释 因为要构造类注释
     */
    private String comment;

    /**
     * 表中所有字段信息
     */
    private List<FieldInfo> fieldList;

    /**
     * 唯一索引集合 保证存取有序 索引可以是单列也可以是联合的
     */
    private Map<String, List<FieldInfo>> keyIndexMap = new LinkedHashMap();

    /**
     * 是否有date类型 因为不属于基本数据类型的要构造import
     */
    private Boolean haveDate;

    /**
     * 是否有时间类型 因为不属于基本数据类型的要构造import
     * date与date_time是不一样的
     * date:2025-1-27
     * date_time:2025-1-27 12:00:00
     */
    private Boolean haveDateTime;

    /**
     * 是否有 bigdecimal类型(mysql中有bigdecimal类型) 不属于基本数据类型的要构造import
     */
    private Boolean haveBigDecimal;

    // 扩展字段 例如Fuzzy以及start end
    private Map<FieldInfo,List<ExtendField>> extendFieldMap;

    public Map<FieldInfo, List<ExtendField>> getExtendFieldMap() {
        return extendFieldMap;
    }

    public void setExtendFieldMap(Map<FieldInfo, List<ExtendField>> extendFieldMap) {
        this.extendFieldMap = extendFieldMap;
    }

    public Boolean getHaveBigDecimal() {
        return haveBigDecimal;
    }

    public void setHaveBigDecimal(Boolean haveBigDecimal) {
        this.haveBigDecimal = haveBigDecimal;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanParamName() {
        return beanParamName;
    }

    public void setBeanParamName(String beanParamName) {
        this.beanParamName = beanParamName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<FieldInfo> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<FieldInfo> fieldList) {
        this.fieldList = fieldList;
    }

    public Map<String, List<FieldInfo>> getKeyIndexMap() {
        return keyIndexMap;
    }

    public void setKeyIndexMap(Map<String, List<FieldInfo>> keyIndexMap) {
        this.keyIndexMap = keyIndexMap;
    }

    public Boolean getHaveDate() {
        return haveDate;
    }

    public void setHaveDate(Boolean haveDate) {
        this.haveDate = haveDate;
    }

    public Boolean getHaveDateTime() {
        return haveDateTime;
    }

    public void setHaveDateTime(Boolean haveDateTime) {
        this.haveDateTime = haveDateTime;
    }
}
