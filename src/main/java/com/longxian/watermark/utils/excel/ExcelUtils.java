package com.longxian.watermark.utils.excel;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 业务描述：Excel解析工具类
 * @author longxian
 * @version 1.0
 * 2018-11-28 10:26:57
 */
public class ExcelUtils {
    private static final String FULL_DATA_FORMAT = "yyyy/MM/dd  HH:mm:ss";
    private static final String SHORT_DATA_FORMAT = "yyyy/MM/dd";
    private static final String NUMERIC_FORMAT = "0.00";


    /**
     * Excel表头对应Entity属性 解析封装javabean
     *
     * @param classzz    类
     * @param in         excel流
     * @param fileName   文件名
     * @param excelHeads excel表头与entity属性对应关系
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> readExcelToEntity(Class<T> classzz, InputStream in, String fileName, List<ExcelHead> excelHeads) throws Exception {
        checkFile(fileName);    //是否EXCEL文件
        Workbook workbook = getWorkBoot(in, fileName); //兼容新老版本
        List<T> excelForBeans = readExcel(classzz, workbook, excelHeads);  //解析Excel
        return excelForBeans;
    }

    /**
     * 解析Excel转换为Entity
     *
     * @param classzz  类
     * @param in       excel流
     * @param fileName 文件名
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> readExcelToEntity(Class<T> classzz, InputStream in, String fileName) throws Exception {
        return readExcelToEntity(classzz, in, fileName,null);
    }

    /**
     * 校验是否是Excel文件
     *
     * @param fileName
     * @throws Exception
     */
    public static void checkFile(String fileName) throws Exception {
        if (!StringUtils.isEmpty(fileName) && !(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
            throw new Exception("不是Excel文件！");
        }
    }

    /**
     * 兼容新老版Excel
     *
     * @param in
     * @param fileName
     * @return
     * @throws IOException
     */
    private static Workbook getWorkBoot(InputStream in, String fileName) throws IOException {
        if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(in);
        } else {
            return new HSSFWorkbook(in);
        }
    }

    /**
     * 解析Excel
     *
     * @param classzz    类
     * @param workbook   工作簿对象
     * @param excelHeads excel与entity对应关系实体
     * @param <T>
     * @return
     * @throws Exception
     */
    private static <T> List<T> readExcel(Class<T> classzz, Workbook workbook, List<ExcelHead> excelHeads) throws Exception {
        List<T> beans = new ArrayList<T>();
        int sheetNum = workbook.getNumberOfSheets();
        for (int sheetIndex = 0; sheetIndex < sheetNum; sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            String sheetName=sheet.getSheetName();
            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            Row head = sheet.getRow(firstRowNum);
            if (head == null)
                continue;
            short firstCellNum = head.getFirstCellNum();
            short lastCellNum = head.getLastCellNum();
            Field[] fields = classzz.getDeclaredFields();
            for (int rowIndex = firstRowNum + 1; rowIndex <= lastRowNum; rowIndex++) {
                Row dataRow = sheet.getRow(rowIndex);
                if (dataRow == null)
                    continue;
                T instance = classzz.newInstance();
                if(CollectionUtils.isEmpty(excelHeads)){  //非头部映射方式，默认不校验是否为空，提高效率
                    firstCellNum=dataRow.getFirstCellNum();
                    lastCellNum=dataRow.getLastCellNum();
                }
                for (int cellIndex = firstCellNum; cellIndex < lastCellNum; cellIndex++) {
                    Cell headCell = head.getCell(cellIndex);
                    if (headCell == null)
                        continue;
                    Cell cell = dataRow.getCell(cellIndex);
                    headCell.setCellType(CellType.STRING);
                    String headName = headCell.getStringCellValue().trim();
                    if (StringUtils.isEmpty(headName)) {
                        continue;
                    }

                    ExcelHead eHead = null;
                    if (!CollectionUtils.isEmpty(excelHeads)) {
                        for (ExcelHead excelHead : excelHeads) {
                            if (headName.equals(excelHead.getExcelName())) {
                                eHead = excelHead;
                                headName = eHead.getEntityName();
                                break;
                            }
                        }
                    }
                    for (Field field : fields) {
                        if (headName.equalsIgnoreCase(field.getName())) {
                            String methodName = MethodUtils.setMethodName(field.getName());
                            Method method = classzz.getMethod(methodName, field.getType());
                            if (isDateFied(field)) {
                                Date date=null;
                                if(cell!=null){
                                    date=cell.getDateCellValue();
                                }
                                if (date == null) {
                                    volidateValueRequired(eHead,sheetName,rowIndex);
                                    break;
                                }
                                method.invoke(instance, cell.getDateCellValue());
                            } else {
                                String value = null;
                                if (null != cell)
                                {
                                    // 以下是判断数据的类型
                                    switch (cell.getCellTypeEnum()) {
                                        case NUMERIC: // 数字
                                            if (CellType.NUMERIC == cell.getCellTypeEnum()) {//判断单元格的类型是否则NUMERIC类型
                                                if (HSSFDateUtil.isCellDateFormatted(cell)) {// 判断是否为日期类型
                                                    Date date = cell.getDateCellValue();
                                                    DateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                    cell.setCellType(CellType.STRING);
                                                    value = formater.format(date);
                                                } else {
                                                    cell.setCellType(CellType.STRING);
                                                    value = cell.getStringCellValue();
                                                }
                                            }
                                            break;
                                        case STRING: // 字符串
                                            value = cell.getStringCellValue();
                                            cell.setCellType(CellType.STRING);
                                            break;
                                        case BOOLEAN: // Boolean
                                            value = cell.getBooleanCellValue() + "";
                                            cell.setCellType(CellType.BOOLEAN);
                                            break;
                                        case FORMULA: // 公式
                                            value = cell.getCellFormula() + "";
                                            cell.setCellType(CellType.FORMULA);
                                            break;
                                        case BLANK: // 空值
                                            cell.setCellType(CellType.BLANK);
                                            value = "";
                                            break;
                                        case ERROR: // 故障
                                            value = "非法字符";
                                            cell.setCellType(CellType.ERROR);
                                            break;
                                        default:
                                            value = "未知类型";
                                            cell.setCellType(CellType.ERROR);
                                            break;
                                    }
                                }

                                if (StringUtils.isEmpty(value)) {
                                    volidateValueRequired(eHead,sheetName,rowIndex);
                                    break;
                                }
                                //处理为为空或者包含特殊字符的数据
                                if(StringUtils.isNotBlank(value) && value.equals("-")){
                                    continue;
                                }

                                method.invoke(instance, convertType(field.getType(), value));

                            }
                            break;
                        }
                    }
                }
                beans.add(instance);
            }
        }
        return beans;
    }
    /**
     * 是否日期字段
     *
     * @param field
     * @return
     */
    private static boolean isDateFied(Field field) {
        return (Date.class == field.getType());
    }

    /**
     * 空值校验
     *
     * @param excelHead
     * @throws Exception
     */
    private static void volidateValueRequired(ExcelHead excelHead,String sheetName,int rowIndex) throws Exception {
        if (excelHead != null && excelHead.isRequired()) {
            throw new Exception("《"+sheetName+"》第"+(rowIndex+1)+"行:\""+excelHead.getExcelName() + "\"不能为空！");
        }
    }


    /**
     * 类型转换
     *
     * @param classzz
     * @param value
     * @return
     */
    private static Object convertType(Class classzz, String value) {
        if (Integer.class == classzz || int.class == classzz) {
            return Integer.valueOf(value);
        }
        if (Short.class == classzz || short.class == classzz) {
            return Short.valueOf(value);
        }
        if (Byte.class == classzz || byte.class == classzz) {
            return Byte.valueOf(value);
        }
        if (Character.class == classzz || char.class == classzz) {
            return value.charAt(0);
        }
        if (Long.class == classzz || long.class == classzz) {
            return Long.valueOf(value);
        }
        if (Float.class == classzz || float.class == classzz) {
            return Float.valueOf(value);
        }
        if (Double.class == classzz || double.class == classzz) {
            return Double.valueOf(value);
        }
        if (Boolean.class == classzz || boolean.class == classzz) {
            return Boolean.valueOf(value.toLowerCase());
        }
        if (BigDecimal.class == classzz) {
            return new BigDecimal(value);
        }
        if (Date.class == classzz) {
            SimpleDateFormat formatter = new SimpleDateFormat(FULL_DATA_FORMAT);
            ParsePosition pos = new ParsePosition(0);
            Date date = formatter.parse(value, pos);
            return date;
        }
        return value;
    }

    /**
     *
     * 描述：填充 单元格 <br>
     *
     * @method ：fillCell<br>
     * @author ：wanglongjie<br>
     * @createDate ：2015年12月2日下午3:57:40 <br>
     * @param value
     *            ：单元格 内容
     * @param cell
     *            ：要填充的单元格
     * @param wb
     *            ： 创建的WorkBook 工作薄对象
     * @param dateFormat
     *            ：日期格式
     */
    private static void fillCell(Object value, Cell cell, Workbook wb,String dateFormat) {
        if (null == value) {
            cell.setCellValue("");
            return;
        }
        if (value instanceof Date) {
            Date d = (Date) value;
            DataFormat format = wb.createDataFormat();
            // 日期格式化
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat(dateFormat));

            cell.setCellStyle(cellStyle);
            cell.setCellValue(d);
            return;
        }
        if (value instanceof java.sql.Date) {
            java.sql.Date d = (java.sql.Date) value;
            cell.setCellValue(d);
            return;
        }
        if (value instanceof Timestamp) {
            Timestamp ts = (Timestamp) value;
            cell.setCellValue(ts);
            return;
        }
        if (value instanceof BigDecimal) {
            BigDecimal b = (BigDecimal) value;
            cell.setCellValue(b.doubleValue());
            return;
        }
        if (value instanceof Double) {
            Double d = (Double) value;
            DataFormat format = wb.createDataFormat();
            // 数字格式化
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat(NUMERIC_FORMAT));
            cell.setCellStyle(cellStyle);
            cell.setCellValue(d);
            return;
        }
        if (value instanceof Float) {
            Float f = (Float) value;
            DataFormat format = wb.createDataFormat();
            // 数字格式化
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(format.getFormat(NUMERIC_FORMAT));//"0.00"
            cell.setCellStyle(cellStyle);
            cell.setCellValue(f);
            return;
        }
        if (value instanceof Long) {
            Long l = (Long) value;
            cell.setCellValue(l);
            return;
        }
        if (value instanceof Integer) {
            Integer i = (Integer) value;
            cell.setCellValue(i);
            return;
        }
        if (value instanceof Boolean) {
            Boolean b = (Boolean) value;
            cell.setCellValue(b);
            return;
        }
        if (value instanceof String) {
            String s = (String) value;
            cell.setCellValue(s);
            return;
        }
    }

    /**
     * 获取properties的set和get方法
     */
    static class MethodUtils {
        private static final String SET_PREFIX = "set";
        private static final String GET_PREFIX = "get";
        private static String capitalize(String name) {
            if (name == null || name.length() == 0) {
                return name;
            }
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        public static String setMethodName(String propertyName) {
            return SET_PREFIX + capitalize(propertyName);
        }
        public static String getMethodName(String propertyName) {
            return GET_PREFIX + capitalize(propertyName);
        }
    }

    /*public static void main(String[] args) {
        File file = new File("G:/RecentTransactionList.xls");
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            List<ExcelHead> excelHeads = new ArrayList<ExcelHead>();
            ExcelHead excelHead = new ExcelHead("Credit Card No", "name");
            ExcelHead excelHead1 = new ExcelHead("Net Amount(THB)", "sex");
            ExcelHead excelHead2 = new ExcelHead("Invoice No", "age");
            ExcelHead excelHead3 = new ExcelHead("Date/Time", "address", false);
            excelHeads.add(excelHead);
            excelHeads.add(excelHead1);
            excelHeads.add(excelHead2);
            excelHeads.add(excelHead3);
            List<ExcelUser> list = ExcelUtils.readExcelToEntity(ExcelUser.class, in, file.getName(), excelHeads);
            for (ExcelUser excelUser : list) {
                System.out.println(excelUser.getName() + ":      " + excelUser.getSex() + ":          " + excelUser.getAge() + ":      " + excelUser.getAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(in!=null) {
                try {
                    in.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }*/

}