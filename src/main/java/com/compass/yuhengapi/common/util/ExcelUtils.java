package com.compass.yuhengapi.common.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelUtils {

    public static byte[] exportExcel(String sheetName, List<Map<String, Object>> dataList) throws IOException {
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
            for (String key : firstRow.keySet()) {
                Cell cell = headerRow.createCell(colIndex);
                cell.setCellValue(key);
                cell.setCellStyle(headerStyle);
                colIndex++;
            }
            
            // 填充数据
            int rowIndex = 1;
            for (Map<String, Object> rowData : dataList) {
                Row dataRow = sheet.createRow(rowIndex);
                colIndex = 0;
                for (Map.Entry<String, Object> entry : rowData.entrySet()) {
                    Cell cell = dataRow.createCell(colIndex);
                    Object value = entry.getValue();
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
            
            // 自动调整列宽
            for (int i = 0; i < firstRow.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}