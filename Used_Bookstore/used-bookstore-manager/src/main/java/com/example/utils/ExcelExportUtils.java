package com.example.utils;

import com.example.model.RevenueByBook;
import com.example.model.RevenueByDate;
import com.example.model.RevenueByEmployee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelExportUtils {

    public static void exportStatisticsToExcel(File file,
                                               List<RevenueByDate> byDate,
                                               List<RevenueByBook> byBook,
                                               List<RevenueByEmployee> byEmp) throws Exception {

        Workbook workbook = new XSSFWorkbook();

        // === Sheet 1: Doanh thu theo ngày
        Sheet sheet1 = workbook.createSheet("Doanh thu theo ngày");
        writeHeader(sheet1, new String[]{"Ngày", "Số hóa đơn", "Tổng doanh thu"});
        int rowNum = 1;
        for (RevenueByDate r : byDate) {
            Row row = sheet1.createRow(rowNum++);
            row.createCell(0).setCellValue(r.getDate().toString());
            row.createCell(1).setCellValue(r.getInvoiceCount());
            row.createCell(2).setCellValue(r.getTotalRevenue());
        }

        // === Sheet 2: Sách bán chạy
        Sheet sheet2 = workbook.createSheet("Bán theo sách");
        writeHeader(sheet2, new String[]{"Tên sách", "Số lượng"});
        rowNum = 1;
        for (RevenueByBook r : byBook) {
            Row row = sheet2.createRow(rowNum++);
            row.createCell(0).setCellValue(r.getBookName());
            row.createCell(1).setCellValue(r.getQuantity());
        }

        // === Sheet 3: Doanh thu theo nhân viên
        Sheet sheet3 = workbook.createSheet("Doanh thu theo nhân viên");
        writeHeader(sheet3, new String[]{"Tên nhân viên", "Số hóa đơn", "Tổng doanh thu"});
        rowNum = 1;
        for (RevenueByEmployee r : byEmp) {
            Row row = sheet3.createRow(rowNum++);
            row.createCell(0).setCellValue(r.getEmployeeName());
            row.createCell(1).setCellValue(r.getInvoiceCount());
            row.createCell(2).setCellValue(r.getRevenue());
        }

        // === Save
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        workbook.close();
        out.close();
    }

    private static void writeHeader(Sheet sheet, String[] headers) {
        Row header = sheet.createRow(0);
        CellStyle style = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
        }
    }
}
