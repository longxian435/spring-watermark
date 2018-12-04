package com.longxian.watermark.service;

import java.text.DecimalFormat;
import java.util.regex.Pattern;
public class Test {

    static Pattern pattern = Pattern.compile("(-?\\d+\\.?\\d*)[Ee]{1}[\\+-]?[0-9]*");
    static DecimalFormat ds = new DecimalFormat("0");
    static boolean isENum(String input) {//判断输入字符串是否为科学计数法
        return pattern.matcher(input).matches();
    }

    public static void main(String[] args) {
        String str = "1.81129E+11";
        System.out.println(isENum(str));
        if (isENum(str)) {
            String sPhone = ds.format(Double.parseDouble(str)).trim();
            System.out.println(sPhone);
        }
    }
    
}