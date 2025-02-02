package com.laundry.service.impl;

import com.google.zxing.WriterException;
import com.laundry.util.TurkishCharacterUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import java.awt.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class QrCodePrintingService {

    private final QrCodeGeneratorService qrCodeGeneratorService;

    @Value("${app.company.name}")
    private String companyName;

    @Value("${app.company.slogan}")
    private String companySlogan;

    public QrCodePrintingService(QrCodeGeneratorService qrCodeGeneratorService) {
        this.qrCodeGeneratorService = qrCodeGeneratorService;
    }

    public void printReceipt(
            String referenceNumber,
            String displayName,
            String productType,
            String servicesString,
            String weightGr,
            int quantity,
            String orderDate
    ) throws PrinterException, IOException, WriterException {

        LocalDateTime ldt = LocalDateTime.parse(orderDate);
        String formattedDate = ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String formattedWeight = weightGr;
        try {
            long weightVal = Long.parseLong(weightGr);
            if (weightVal >= 1000) {
                String weightWithComma = String.format("%,d", weightVal);
                double kg = weightVal / 1000.0;
                String kgStr = String.format("%.1f", kg);
                formattedWeight = weightWithComma + "     (~" + kgStr + " kg)";
            }
        } catch (NumberFormatException _) {
        }

        String qrData = String.join("\n",
                TurkishCharacterUtil.convertTurkishCharacters("Satıcı: " + companyName),
                TurkishCharacterUtil.convertTurkishCharacters("Müşteri: " + displayName),
                TurkishCharacterUtil.convertTurkishCharacters("Ürün: " + productType),
                TurkishCharacterUtil.convertTurkishCharacters("Servisler: " + servicesString),
                TurkishCharacterUtil.convertTurkishCharacters("Ağırlık (GR): " + formattedWeight),
                TurkishCharacterUtil.convertTurkishCharacters("Parça sayısı: " + quantity),
                TurkishCharacterUtil.convertTurkishCharacters("Referans Numarası: " + referenceNumber),
                TurkishCharacterUtil.convertTurkishCharacters("Sipariş Tarihi: " + formattedDate)
        );

        byte[] qrBytes = qrCodeGeneratorService.generateQRCodeImage(qrData, 100, 100);
        BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrBytes));

        final String finalFormattedWeight = formattedWeight;

        PrinterJob printerJob = PrinterJob.getPrinterJob();
        PageFormat pageFormat = printerJob.defaultPage();
        Paper paper = new Paper();
        double paperWidth = 5 * 28.35;
        double paperHeight = 8 * 28.35;
        paper.setImageableArea(2, 2, paperWidth - 4, paperHeight - 4);
        paper.setSize(paperWidth, paperHeight);
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.LANDSCAPE);

        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pf, int pageIndex) throws PrinterException {
                if (pageIndex > 0) return NO_SUCH_PAGE;
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pf.getImageableX(), pf.getImageableY());
                int availableWidth = (int) pf.getImageableWidth();

                int topOffset = (int) (0.5 * 28.35);
                int currentY = topOffset;
                int sideMargin = 5;
                int tableWidth = availableWidth - 2 * sideMargin;
                int tableX = sideMargin;

                int cellPadding = 3;

                Font smallFont = new Font("Arial", Font.PLAIN, 7);
                g2d.setFont(smallFont);
                FontMetrics fm = g2d.getFontMetrics();
                int verticalSpacing = 2;

                String[][] tableData = {
                        {"Satıcı:", companyName},
                        {"Müşteri:", displayName},
                        {"Ürün:", productType},
                        {"Servisler:", servicesString},
                        {"Ağırlık (GR):", finalFormattedWeight},
                        {"Parça sayısı:", String.valueOf(quantity)},
                        {"Referans Numarası:", referenceNumber},
                        {"Sipariş Tarihi:", formattedDate}
                };

                int maxHeaderWidth = 0;
                for (String[] row : tableData) {
                    int w = fm.stringWidth(row[0]);
                    if (w > maxHeaderWidth) maxHeaderWidth = w;
                }
                int col1Width = maxHeaderWidth + 2 * cellPadding;
                int col2Width = tableWidth - col1Width;

                for (String[] row : tableData) {
                    String header = row[0];
                    String value = row[1];
                    int allowedMaxLines = header.equals("Servisler:") ? 2 : 1;
                    List<String> wrappedValue = wrapTextLimited(value, fm, col2Width - 2 * cellPadding, allowedMaxLines);
                    int numLines = wrappedValue.size();
                    int valueBlockHeight = numLines * fm.getHeight() + (numLines - 1) * verticalSpacing;
                    int rowHeight = Math.max(fm.getHeight(), valueBlockHeight);

                    int headerY = currentY + (rowHeight - fm.getHeight()) / 2 + fm.getAscent();
                    int valueY = currentY + (rowHeight - valueBlockHeight) / 2 + fm.getAscent();

                    g2d.drawRect(tableX, currentY, col1Width, rowHeight);
                    g2d.drawRect(tableX + col1Width, currentY, col2Width, rowHeight);

                    g2d.drawString(header, tableX + cellPadding, headerY);
                    int valueX = tableX + col1Width + cellPadding;
                    for (String line : wrappedValue) {
                        g2d.drawString(line, valueX, valueY);
                        valueY += fm.getHeight() + verticalSpacing;
                    }
                    currentY += rowHeight;
                }

                int newCellY = currentY + (int) (0.2 * 28.35);
                int leftLowerX = tableX;
                int leftLowerWidth = col1Width;
                int rightLowerX = tableX + col1Width;
                int rightLowerWidth = col2Width;

                BufferedImage croppedQrImage = cropWhiteBorder(qrImage);
                int desiredQRSize = Math.min(leftLowerWidth - 4, 40);
                int qrX = leftLowerX + (leftLowerWidth - desiredQRSize) / 2;
                g2d.drawImage(croppedQrImage, qrX, newCellY, desiredQRSize, desiredQRSize, null);

                g2d.setFont(smallFont);
                FontMetrics fmRef = g2d.getFontMetrics(smallFont);
                int refStrWidth = fmRef.stringWidth(referenceNumber);
                int refX = leftLowerX + (leftLowerWidth - refStrWidth) / 2;
                int refY = newCellY + desiredQRSize + fmRef.getAscent();
                g2d.drawString(referenceNumber, refX, refY);

                Font brandFontBold = new Font("Arial", Font.BOLD, 14);
                Font brandFontPlain = new Font("Arial", Font.PLAIN, 13);

                FontMetrics fmBrand = g2d.getFontMetrics(brandFontBold);
                int brandNameWidth = fmBrand.stringWidth(companyName);
                if (brandNameWidth > rightLowerWidth) {
                    float newSize = 14 * rightLowerWidth / (float) brandNameWidth;
                    brandFontBold = brandFontBold.deriveFont(newSize);
                    fmBrand = g2d.getFontMetrics(brandFontBold);
                }
                FontMetrics fmSlogan = g2d.getFontMetrics(brandFontPlain);
                int sloganWidth = fmSlogan.stringWidth(companySlogan);
                if (sloganWidth > rightLowerWidth) {
                    float newSize = 13 * rightLowerWidth / (float) sloganWidth;
                    brandFontPlain = brandFontPlain.deriveFont(newSize);
                    fmSlogan = g2d.getFontMetrics(brandFontPlain);
                }

                int totalBrandHeight = fmBrand.getHeight() + fmSlogan.getHeight() + 2;
                int brandStartY = newCellY + (desiredQRSize - totalBrandHeight) / 2 + fmBrand.getAscent();

                g2d.setFont(brandFontBold);
                int companyStrWidth = fmBrand.stringWidth(companyName);
                int companyX = rightLowerX + (rightLowerWidth - companyStrWidth) / 2;
                g2d.drawString(companyName, companyX, brandStartY);

                g2d.setFont(brandFontPlain);
                int sloganX = rightLowerX + (rightLowerWidth - fmSlogan.stringWidth(companySlogan)) / 2;
                int sloganY = brandStartY + fmBrand.getHeight() + 2;
                g2d.drawString(companySlogan, sloganX, sloganY);

                return PAGE_EXISTS;
            }
        }, pageFormat);

        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(new MediaPrintableArea(0, 0, 500, 800, MediaPrintableArea.MM));
        printerJob.print(aset);
    }

    private List<String> wrapTextLimited(String text, FontMetrics fm, int maxWidth, int maxLines) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }
        String[] words = text.split("\\s+");
        int wordIndex = 0;
        while (wordIndex < words.length && lines.size() < maxLines) {
            StringBuilder line = new StringBuilder();
            while (wordIndex < words.length) {
                String word = words[wordIndex];
                String testLine = line.isEmpty() ? word : line + " " + word;
                if (fm.stringWidth(testLine) <= maxWidth) {
                    line.append(line.isEmpty() ? word : " " + word);
                    wordIndex++;
                } else {
                    break;
                }
            }
            lines.add(line.toString());
        }
        if (wordIndex < words.length && !lines.isEmpty()) {
            String lastLine = lines.getLast();
            while (fm.stringWidth(lastLine + "...") > maxWidth && !lastLine.isEmpty()) {
                lastLine = lastLine.substring(0, lastLine.length() - 1);
            }
            lines.set(lines.size() - 1, lastLine + "...");
        }
        return lines;
    }

    private BufferedImage cropWhiteBorder(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int top = 0, left = 0, right = width, bottom = height;

        for (int y = 0; y < height; y++) {
            boolean found = false;
            for (int x = 0; x < width; x++) {
                if (isWhite(image.getRGB(x, y))) {
                    top = y;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        for (int y = height - 1; y >= 0; y--) {
            boolean found = false;
            for (int x = 0; x < width; x++) {
                if (isWhite(image.getRGB(x, y))) {
                    bottom = y;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        for (int x = 0; x < width; x++) {
            boolean found = false;
            for (int y = 0; y < height; y++) {
                if (isWhite(image.getRGB(x, y))) {
                    left = x;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        for (int x = width - 1; x >= 0; x--) {
            boolean found = false;
            for (int y = 0; y < height; y++) {
                if (isWhite(image.getRGB(x, y))) {
                    right = x;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        int margin = 3;
        left = Math.max(0, left - margin);
        top = Math.max(0, top - margin);
        right = Math.min(width - 1, right + margin);
        bottom = Math.min(height - 1, bottom + margin);

        int newWidth = right - left + 1;
        int newHeight = bottom - top + 1;
        return image.getSubimage(left, top, newWidth, newHeight);
    }

    private boolean isWhite(int rgb) {
        Color color = new Color(rgb, true);
        return color.getAlpha() != 0 && (color.getRed() <= 240 || color.getGreen() <= 240 || color.getBlue() <= 240);
    }
}
