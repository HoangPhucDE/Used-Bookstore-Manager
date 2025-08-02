package com.example.utils;

import com.example.model.OrderItem;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class InvoiceGenerator {

    public static void generateInvoice(File file,
                                       int orderId,
                                       String customerName,
                                       String phone,
                                       String email,
                                       String address,
                                       String orderType,
                                       List<OrderItem> items,
                                       double totalAmount) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Tải font từ resource
            BaseFont baseFont = loadFont();
            Font font = new Font(baseFont, 12);
            Font titleFont = new Font(baseFont, 16, Font.BOLD);

            // Tiêu đề
            Paragraph title = new Paragraph("📄 HÓA ĐƠN BÁN SÁCH", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Thông tin khách hàng
            document.add(new Paragraph("Mã đơn: " + orderId, font));
            document.add(new Paragraph("Tên khách: " + customerName, font));
            document.add(new Paragraph("Số điện thoại: " + phone, font));
            document.add(new Paragraph("Email: " + email, font));
            document.add(new Paragraph("Địa chỉ: " + address, font));
            document.add(new Paragraph("Loại đơn: " + orderType, font));
            document.add(new Paragraph("\n"));

            // Bảng chi tiết đơn hàng
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 2, 3, 3});

            addCell(table, "Tên sách", font, true);
            addCell(table, "Số lượng", font, true);
            addCell(table, "Đơn giá", font, true);
            addCell(table, "Thành tiền", font, true);

            for (OrderItem item : items) {
                addCell(table, item.getBookTitle(), font);
                addCell(table, String.valueOf(item.getQuantity()), font);
                addCell(table, formatCurrency(item.getUnitPrice()), font);
                addCell(table, formatCurrency(item.getTotalPrice()), font);
            }

            document.add(table);

            // Tổng tiền
            document.add(new Paragraph("\n"));
            Paragraph total = new Paragraph("Tổng cộng: " + formatCurrency(totalAmount), titleFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BaseFont loadFont() throws IOException, DocumentException {
        try (InputStream fontStream = InvoiceGenerator.class.getResourceAsStream("/fonts/arial.ttf")) {
            if (fontStream == null) {
                throw new FileNotFoundException("Font file 'arial.ttf' not found in /fonts/.");
            }

            byte[] fontBytes = fontStream.readAllBytes();
            return BaseFont.createFont("arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.CACHED, fontBytes, null);
        }
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        addCell(table, text, font, false);
    }

    private static void addCell(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (isHeader) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        table.addCell(cell);
    }

    private static String formatCurrency(double value) {
        return String.format(Locale.US, "%,.0f VNĐ", value);
    }
}
