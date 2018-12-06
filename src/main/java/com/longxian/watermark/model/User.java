package com.longxian.watermark.model;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = -1L;

    private String username;
    private Integer age;

    //redis将对象反序列化时候需要使用到，所以空构造函数必须得有
    public User() {
    }

    public User(String username, Integer age) {
        this.username = username;
        this.age = age;
    }

    // 省略getter和setter

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}