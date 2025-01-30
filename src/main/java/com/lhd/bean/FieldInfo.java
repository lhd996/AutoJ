package com.lhd.bean;

public class FieldInfo {
    /**
     * 字段名称
     */
    private String FieldName;

    /**
     * bean属性名称 例如salary_info对应的名称是salaryInfo
     */
    private String propertyName;

    /**
    * 这个字段在数据库中的类型
    */
    private String sqlType;

    /**
     * 这个字段在java中的类型 因为要构造private javaType propertyName
     */
    private String javaType;

    /**
     * 字段备注 因为要构造字段注释
     */
    private String comment;

    /**
     * 字段是否是自增长 因为插入时可能要返回新记录id
     * 如果不是自增长 那么新纪录的id我们肯定知道 我们在插入的时候肯定会指定id
     * 如果是自增长 那么我们需要返回这个id
     */
    private Boolean isAutoIncrement;

    public String getFieldName() {
        return FieldName;
    }

    public void setFieldName(String fieldName) {
        FieldName = fieldName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getAutoIncrement() {
        return isAutoIncrement;
    }

    public void setAutoIncrement(Boolean autoIncrement) {
        isAutoIncrement = autoIncrement;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }
}
