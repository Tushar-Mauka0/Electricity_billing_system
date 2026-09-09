package service;

import model.Bill;
import model.Customer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;

public class PDFExportService {
    private final BillExportService billExportService = new BillExportService();

    public boolean exportBillToPDF(Bill bill, Customer customer, File outputFile) {
        try {
            String html = billExportService.generateHTMLInvoice(bill, customer);

            // Render HTML to component container
            JEditorPane pane = new JEditorPane();
            pane.setContentType("text/html");
            pane.setText(html);
            pane.setSize(new Dimension(800, 1100));

            // Create high-res BufferedImage (1600x2200 for 2x crisp printing)
            int imgWidth = 1600;
            int imgHeight = 2200;
            BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, imgWidth, imgHeight);

            g2d.scale(2.0, 2.0); // 2x scaling for high DPI sharp text
            pane.print(g2d);
            g2d.dispose();

            // Create PDF Document via PDFBox
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);

                PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
                try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                    float pageWidth = PDRectangle.A4.getWidth();
                    float pageHeight = PDRectangle.A4.getHeight();
                    
                    // Fit image into A4 dimensions with 20pt margin
                    float margin = 20;
                    float drawWidth = pageWidth - (margin * 2);
                    float drawHeight = (float) imgHeight * (drawWidth / (float) imgWidth);
                    float startY = pageHeight - margin - drawHeight;

                    contentStream.drawImage(pdImage, margin, startY, drawWidth, drawHeight);
                }

                doc.save(outputFile);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportBillToHTML(Bill bill, Customer customer, File outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            String html = billExportService.generateHTMLInvoice(bill, customer);
            writer.write(html);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
