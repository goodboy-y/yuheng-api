package com.compass.yuhengapi.common.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtils {

    /**
     * 导出Excel（使用原始字段名作为表头）
     */
    public static byte[] exportExcel(String sheetName, List<Map<String, Object>> dataList) throws IOException {
        return exportExcelWithHeaders(sheetName, dataList, null);
    }

    /**
     * 导出Excel（支持自定义表头）
     * @param sheetName 工作表名称
     * @param dataList 数据列表
     * @param headerMapping 字段映射，key为原始字段名，value为显示名称。如果为null则使用原始字段名
     */
    public static byte[] exportExcelWithHeaders(String sheetName, List<Map<String, Object>> dataList, Map<String, String> headerMapping) throws IOException {
        return exportExcelWithHeaders(sheetName, dataList, headerMapping, null);
    }

    /**
     * 导出Excel（支持自定义表头和列宽）
     * @param sheetName 工作表名称
     * @param dataList 数据列表
     * @param headerMapping 字段映射，key为原始字段名，value为显示名称。如果为null则使用原始字段名
     * @param columnWidthMapping 列宽映射，key为原始字段名，value为列宽。如果为null或未设置，则根据显示名称字数计算列宽
     */
    public static byte[] exportExcelWithHeaders(String sheetName, List<Map<String, Object>> dataList, Map<String, String> headerMapping, Map<String, Integer> columnWidthMapping) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 创建工作表
            Sheet sheet = workbook.createSheet(sheetName);
            
            if (dataList == null || dataList.isEmpty()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // 获取表头字段
            Map<String, Object> firstRow = dataList.get(0);
            int colIndex = 0;
            
            // 构建有序的字段列表和对应的表头名称
            LinkedHashMap<String, String> fieldHeaders = new LinkedHashMap<>();
            for (String key : firstRow.keySet()) {
                String displayName = (headerMapping != null && headerMapping.containsKey(key)) 
                    ? headerMapping.get(key) 
                    : key;
                fieldHeaders.put(key, displayName);
                
                Cell cell = headerRow.createCell(colIndex);
                cell.setCellValue(displayName);
                cell.setCellStyle(headerStyle);
                colIndex++;
            }
            
            // 填充数据
            int rowIndex = 1;
            for (Map<String, Object> rowData : dataList) {
                Row dataRow = sheet.createRow(rowIndex);
                colIndex = 0;
                for (Map.Entry<String, String> fieldEntry : fieldHeaders.entrySet()) {
                    Cell cell = dataRow.createCell(colIndex);
                    Object value = rowData.get(fieldEntry.getKey());
                    if (value != null) {
                        if (value instanceof String) {
                            cell.setCellValue((String) value);
                        } else if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof Boolean) {
                            cell.setCellValue((Boolean) value);
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                    colIndex++;
                }
                rowIndex++;
            }
            
            // 设置列宽
            colIndex = 0;
            for (Map.Entry<String, String> fieldEntry : fieldHeaders.entrySet()) {
                String fieldName = fieldEntry.getKey();
                String displayName = fieldEntry.getValue();
                
                if (columnWidthMapping != null && columnWidthMapping.containsKey(fieldName)) {
                    // 使用配置的列宽
                    sheet.setColumnWidth(colIndex, columnWidthMapping.get(fieldName) * 256);
                } else {
                    // 根据显示名称字数计算列宽，每个汉字占用2个字符宽度，每个字母/数字占用1个字符宽度
                    int charCount = 0;
                    for (char c : displayName.toCharArray()) {
                        if (c >= 0x4e00 && c <= 0x9fa5) {
                            charCount += 2;
                        } else {
                            charCount += 1;
                        }
                    }
                    // 最小宽度为10，最大宽度为50
                    int width = Math.max(10, Math.min(50, charCount + 2));
                    sheet.setColumnWidth(colIndex, width * 256);
                }
                colIndex++;
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}