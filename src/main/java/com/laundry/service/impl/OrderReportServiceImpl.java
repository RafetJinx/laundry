package com.laundry.service.impl;

import com.laundry.entity.Order;
import com.laundry.repository.OrderRepository;
import com.laundry.service.OrderReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class OrderReportServiceImpl implements OrderReportService {

    private final OrderRepository orderRepository;

    private final ExcelReportGeneratorServiceImpl excelReportGeneratorService;

    private final MailService mailService;

    @Value("${app.company.name}")
    private String companyName;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public OrderReportServiceImpl(OrderRepository orderRepository,
                                  ExcelReportGeneratorServiceImpl excelReportGeneratorService,
                                  MailService mailService) {
        this.orderRepository = orderRepository;
        this.excelReportGeneratorService = excelReportGeneratorService;
        this.mailService = mailService;
    }

    @Override
    public byte[] downloadOrderReport(LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        return excelReportGeneratorService.generateOrderReportExcel(orders);
    }

    @Override
    public void sendOrderReport(LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        byte[] excelBytes = excelReportGeneratorService.generateOrderReportExcel(orders);
        String to = "rafet.ersoy.cs@gmail.com";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        String subject = companyName + " " + formattedStartDate + " - " + formattedEndDate + " Sipariş Listesi";

        String body = companyName + "'den selamlar,\n\n" +
                "Tarih aralığı: " + formattedStartDate + " - " + formattedEndDate + " sipariş listesi raporu Ek'tedir.\n\n" +
                "İyi çalışmalar,\n" +
                "Saygılarımızla,";

        String attachmentFilename = companyName + "_Sipariş_Listesi_" + formattedStartDate.replace("/", "-") + "_"
                + formattedEndDate.replace("/", "-") + ".xlsx";

        mailService.sendEmailWithAttachment(to, subject, body, excelBytes, attachmentFilename);
    }


}
