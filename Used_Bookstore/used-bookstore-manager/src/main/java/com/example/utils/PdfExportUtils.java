package com.example.utils;

import com.example.model.OrderItem;
import com.example.model.RevenueByBook;
import com.example.model.RevenueByDate;
import com.example.model.RevenueByEmployee;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class PdfExportUtils {

    // === Dùng lại export hóa đơn
    public static void exportInvoice(File file, int orderId, String customerName,
                                     String phone, String email, String address, String orderType,
                                     List<OrderItem> items, double totalAmount) throws Exception {

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        BaseFont baseFont = loadFont();
        Font font = new Font(baseFont, 12);
        Font titleFont = new Font(baseFont, 16, Font.BOLD);

        Paragraph title = new Paragraph("📄 HÓA ĐƠN BÁN SÁCH", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Mã đơn: " + orderId, font));
        document.add(new Paragraph("Tên khách: " + customerName, font));
        document.add(new Paragraph("SĐT: " + phone, font));
        document.add(new Paragraph("Email: " + email, font));
        document.add(new Paragraph("Địa chỉ: " + address, font));
        document.add(new Paragraph("Loại đơn: " + orderType, font));
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 2, 3, 3});

        addHeaderCell(table, "Tên sách", font);
        addHeaderCell(table, "Số lượng", font);
        addHeaderCell(table, "Đơn giá", font);
        addHeaderCell(table, "Thành tiền", font);

        for (OrderItem item : items) {
            addCell(table, item.getBookTitle(), font);
            addCell(table, String.valueOf(item.getQuantity()), font);
            addCell(table, CurrencyFormatter.format(item.getUnitPrice()), font);
            addCell(table, CurrencyFormatter.format(item.getTotalPrice()), font);
        }

        document.add(table);
        document.add(new Paragraph("\n"));

        Paragraph total = new Paragraph("Tổng cộng: " + CurrencyFormatter.format(totalAmount), titleFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        document.close();
    }

    // === Dùng lại export thống kê
    public static void exportStatistics(File file,
                                        List<RevenueByDate> byDate,
                                        List<RevenueByBook> byBook,
                                        List<RevenueByEmployee> byEmployee) throws Exception {

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        BaseFont bf = loadFont();
        Font font = new Font(bf, 12);
        Font titleFont = new Font(bf, 14, Font.BOLD);

        // === Doanh thu theo ngày
        document.add(new Paragraph("📅 DOANH THU THEO NGÀY\n\n", titleFont));
        PdfPTable dateTable = new PdfPTable(3);
        dateTable.setWidthPercentage(100);
        dateTable.setWidths(new float[]{3, 3, 3});
        addHeaderCell(dateTable, "Ngày", font);
        addHeaderCell(dateTable, "Số hóa đơn", font);
        addHeaderCell(dateTable, "Tổng doanh thu", font);

        for (RevenueByDate entry : byDate) {
            addCell(dateTable, entry.getDate().toString(), font);
            addCell(dateTable, String.valueOf(entry.getInvoiceCount()), font);
            addCell(dateTable, CurrencyFormatter.format(entry.getTotalRevenue()), font);
        }

        document.add(dateTable);
        document.add(new Paragraph("\n"));

        // === Sách bán chạy
        document.add(new Paragraph("📚 SÁCH BÁN CHẠY\n\n", titleFont));
        PdfPTable bookTable = new PdfPTable(2);
        bookTable.setWidthPercentage(100);
        bookTable.setWidths(new float[]{4, 2});
        addHeaderCell(bookTable, "Tên sách", font);
        addHeaderCell(bookTable, "Số lượng", font);

        for (RevenueByBook book : byBook) {
            addCell(bookTable, book.getBookName(), font);
            addCell(bookTable, String.valueOf(book.getQuantity()), font);
        }

        document.add(bookTable);
        document.add(new Paragraph("\n"));

        // === Nhân viên
        document.add(new Paragraph("👥 DOANH THU THEO NHÂN VIÊN\n\n", titleFont));
        PdfPTable empTable = new PdfPTable(3);
        empTable.setWidthPercentage(100);
        empTable.setWidths(new float[]{4, 2, 3});
        addHeaderCell(empTable, "Tên nhân viên", font);
        addHeaderCell(empTable, "Số hóa đơn", font);
        addHeaderCell(empTable, "Tổng doanh thu", font);

        for (RevenueByEmployee emp : byEmployee) {
            addCell(empTable, emp.getEmployeeName(), font);
            addCell(empTable, String.valueOf(emp.getInvoiceCount()), font);
            addCell(empTable, CurrencyFormatter.format(emp.getRevenue()), font);
        }

        document.add(empTable);
        document.close();
    }

    // === Hàm tiện ích dùng chung
    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        table.addCell(cell);
    }

    private static BaseFont loadFont() throws Exception {
        try (InputStream fontStream = PdfExportUtils.class.getResourceAsStream("/fonts/arial.ttf")) {
            if (fontStream == null)
                throw new FileNotFoundException("Không tìm thấy font arial.ttf trong thư mục /fonts/.");
            byte[] fontBytes = fontStream.readAllBytes();
            return BaseFont.createFont("arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.CACHED, fontBytes, null);
        }
    }
}
