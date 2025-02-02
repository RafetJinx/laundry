package com.laundry.service.impl;

import com.laundry.entity.Order;
import com.laundry.entity.OrderItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExcelReportGeneratorServiceImpl {

    public byte[] generateOrderReportExcel(List<Order> orders) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Orders Report");

            DataFormat dataFormat = workbook.createDataFormat();
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));

            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "Id",
                    "Referans No",
                    "Müşteri Adı",
                    "Ürün Adı",
                    "Hizmetler",
                    "Toplam Tutar",
                    "Adet",
                    "Oluşturulma Tarihi",
                    "Durum"
            };
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getReferenceNo());
                row.createCell(2).setCellValue(order.getUser().getDisplayName());
                row.createCell(3).setCellValue(order.getProduct().getName());
                row.createCell(4).setCellValue(buildServiceTypeString(order));
                Cell totalAmountCell = row.createCell(5);
                totalAmountCell.setCellValue(order.getTotalAmount().doubleValue());
                totalAmountCell.setCellStyle(currencyStyle);
                int quantity = order.getOrderItems().stream().mapToInt(OrderItem::getQuantity).sum();
                row.createCell(6).setCellValue(quantity);
                row.createCell(7).setCellValue(order.getCreatedAt().toString());
                row.createCell(8).setCellValue(order.getStatus().name());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columns.length - 1));

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new Exception("Error generating Excel report: " + e.getMessage(), e);
        }
    }

    private String buildServiceTypeString(Order order) {
        return order.getOrderItems().stream()
                .map(item -> item.getService().getName())
                .distinct()
                .collect(Collectors.joining(", "));
    }
}
