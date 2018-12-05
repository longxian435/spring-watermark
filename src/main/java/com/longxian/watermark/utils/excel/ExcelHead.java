package com.longxian.watermark.utils.excel;

/**
 * 业务描述：Excel解析列头信息映射实体类
 * @author longxian
 * @version 1.0
 * 2018-11-28 10:26:57
 */
public class ExcelHead {
    private String excelName;             //Excel名
    private String entityName;            //实体类属性名
    private boolean required=false;      //值必填
 
    public String getExcelName() {
        return excelName;
    }
 
    public void setExcelName(String excelName) {
        this.excelName = excelName;
    }
 
    public String getEntityName() {
        return entityName;
    }
 
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
 
    public boolean isRequired() {
        return required;
    }
 
    public void setRequired(boolean required) {
        this.required = required;
    }
 
    public ExcelHead(String excelName, String entityName, boolean required) {
        this.excelName = excelName;
        this.entityName = entityName;
        this.required = required;
    }
 
    public ExcelHead(String excelName, String entityName) {
        this.excelName = excelName;
        this.entityName = entityName;
    }
 
    public ExcelHead(){};
}
