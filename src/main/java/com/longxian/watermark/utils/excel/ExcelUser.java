package com.longxian.watermark.utils.excel;

import java.math.BigDecimal;

/**
 * 业务描述：Excel解析Demo实体对象
 * @author longxian
 * @version 1.0
 * 2018-11-28 10:26:57
 */
public class ExcelUser {
    private String name;
    private String age;
    private String  address;
    private BigDecimal sex;
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public String getAge() {
        return age;
    }
 
    public void setAge(String age) {
        this.age = age;
    }
 
    public String getAddress() {
        return address;
    }
 
    public void setAddress(String address) {
        this.address = address;
    }
 
    public BigDecimal getSex() {
        return sex;
    }
 
    public void setSex(BigDecimal sex) {
        this.sex = sex;
    }
}
