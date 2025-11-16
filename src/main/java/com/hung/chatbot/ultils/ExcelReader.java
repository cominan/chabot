package com.hung.chatbot.ultils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Admin
 * @since 11/15/2025
 */
public class ExcelReader {
    /**
     * Đọc file Excel và trả về dữ liệu dưới dạng List
     * @param filePath Đường dẫn đến file Excel
     * @param sheetName Tên sheet cần đọc (null để đọc sheet đầu tiên)
     * @return List của List String chứa dữ liệu
     */
    public static List<List<String>> readExcel(String filePath, String sheetName) {
        List<List<String>> data = new ArrayList<>();

        try (FileInputStream file = new FileInputStream(new File(filePath))) {
            Workbook workbook;

            // Xác định loại file Excel
            if (filePath.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(file);
            } else if (filePath.endsWith(".xls")) {
                workbook = new HSSFWorkbook(file);
            } else {
                throw new IllegalArgumentException("File không phải định dạng Excel");
            }

            // Chọn sheet
            Sheet sheet;
            if (sheetName != null) {
                sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new IllegalArgumentException("Sheet '" + sheetName + "' không tồn tại");
                }
            } else {
                sheet = workbook.getSheetAt(0);
            }

            // Đọc dữ liệu
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(getCellValueAsString(cell));
                }
                data.add(rowData);
            }

            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file Excel: " + e.getMessage(), e);
        }

        return data;
    }

    /**
     * Chuyển giá trị cell thành String
     */
    private static String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "";
        };
    }

    /**
     * Đọc Excel và map vào danh sách đối tượng
     * @param filePath Đường dẫn file
     * @param sheetName Tên sheet
     * @param clazz Class của đối tượng đích
     * @param hasHeader Có dòng tiêu đề không
     * @return List của đối tượng
     */

    public static <T> List<T> readExcelToObjects(String filePath, String sheetName,
                                                 Class<T> clazz, boolean hasHeader) {
        List<T> result = new ArrayList<>();
        List<List<String>> rawData = ExcelReader.readExcel(filePath, sheetName);

        if (rawData.isEmpty()) {
            return result;
        }

        int startRow = hasHeader ? 1 : 0;
        Field[] fields = clazz.getDeclaredFields();

        for (int i = startRow; i < rawData.size(); i++) {
            try {
                List<String> rowData = rawData.get(i);
                T obj = clazz.getDeclaredConstructor().newInstance();

                for (int j = 0; j < Math.min(fields.length, rowData.size()); j++) {
                    Field field = fields[j];
                    field.setAccessible(true);
                    setFieldValue(obj, field, rowData.get(j));
                }

                result.add(obj);
            } catch (Exception e) {
                System.err.println("Lỗi khi tạo đối tượng từ dòng " + i + ": " + e.getMessage());
            }
        }

        return result;
    }

    private static <T> void setFieldValue(T obj, Field field, String value) throws Exception {
        Class<?> fieldType = field.getType();

        if (value == null || value.trim().isEmpty()) {
            return;
        }

        if (fieldType == String.class) {
            field.set(obj, value);
        } else if (fieldType == int.class || fieldType == Integer.class) {
            field.set(obj, Integer.parseInt(value.replace(".0", "")));
        } else if (fieldType == double.class || fieldType == Double.class) {
            field.set(obj, Double.parseDouble(value));
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            field.set(obj, Boolean.parseBoolean(value));
        } else if (fieldType == LocalDateTime.class) {
            field.set(obj, parseLocalDateTime(value));
        }
        // Thêm các kiểu dữ liệu khác nếu cần
    }

    /**
     * Phương thức chuyển đổi String sang LocalDateTime
     * Xử lý nhiều định dạng ngày tháng khác nhau
     */
    private static LocalDateTime parseLocalDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        value = value.trim();

        // Danh sách các định dạng có thể có
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd",
                "dd/MM/yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm",
                "dd/MM/yyyy",
                "MM/dd/yyyy HH:mm:ss",
                "MM/dd/yyyy HH:mm",
                "MM/dd/yyyy",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy/MM/dd HH:mm",
                "yyyy/MM/dd"
        };

        for (String pattern : patterns) {
            try {
                if (pattern.contains("HH:mm")) {
                    // Định dạng có cả thời gian
                    java.time.format.DateTimeFormatter formatter =
                            java.time.format.DateTimeFormatter.ofPattern(pattern);
                    return LocalDateTime.parse(value, formatter);
                } else {
                    // Định dạng chỉ có ngày
                    java.time.format.DateTimeFormatter formatter =
                            java.time.format.DateTimeFormatter.ofPattern(pattern);
                    LocalDate localDate = LocalDate.parse(value, formatter);
                    return localDate.atStartOfDay(); // Mặc định thời gian là 00:00:00
                }
            } catch (Exception e) {
                // Thử định dạng tiếp theo
                continue;
            }
        }

        // Nếu không parse được với các định dạng trên, thử parse số (trường hợp từ Excel numeric)
        try {
            double numericValue = Double.parseDouble(value);
            java.util.Date date = DateUtil.getJavaDate(numericValue);
            return date.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể chuyển đổi giá trị '" + value + "' sang LocalDateTime");
        }
    }

}
