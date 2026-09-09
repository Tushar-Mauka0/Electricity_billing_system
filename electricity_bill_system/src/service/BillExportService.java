package service;

import model.Bill;
import model.Customer;

import java.text.NumberFormat;
import java.util.Locale;

public class BillExportService {
    private final SlabCalculatorService slabCalculatorService = new SlabCalculatorService();

    public String generateHTMLInvoice(Bill bill, Customer customer) {
        SlabCalculatorService.BillBreakdown breakdown = slabCalculatorService.calculateBill(bill.getUnitsConsumed(), customer.getCategory());
        NumberFormat currFmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><style>")
          .append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; color: #1E293B; background: #FFFFFF; }")
          .append(".header { border-bottom: 3px solid #0EA5E9; padding-bottom: 12px; margin-bottom: 20px; }")
          .append(".discom-title { font-size: 22px; font-weight: bold; color: #0EA5E9; }")
          .append(".sub-title { font-size: 12px; color: #64748B; margin-top: 4px; }")
          .append(".bill-box { width: 100%; border-collapse: collapse; margin-bottom: 20px; }")
          .append(".bill-box td { padding: 8px; font-size: 13px; border-bottom: 1px solid #E2E8F0; }")
          .append(".label { font-weight: bold; color: #475569; width: 35%; }")
          .append(".table-slab { width: 100%; border-collapse: collapse; margin-top: 15px; margin-bottom: 20px; }")
          .append(".table-slab th { background: #F1F5F9; color: #334155; font-size: 12px; font-weight: bold; text-align: left; padding: 8px; border: 1px solid #CBD5E1; }")
          .append(".table-slab td { font-size: 12px; padding: 8px; border: 1px solid #E2E8F0; }")
          .append(".amount-table { width: 50%; float: right; border-collapse: collapse; margin-top: 10px; }")
          .append(".amount-table td { padding: 6px 10px; font-size: 13px; text-align: right; }")
          .append(".total-row { font-size: 16px; font-weight: bold; color: #0F172A; border-top: 2px solid #0EA5E9; background: #F0F9FF; }")
          .append(".status-badge { display: inline-block; padding: 4px 12px; font-size: 12px; font-weight: bold; border-radius: 12px; }")
          .append(".status-paid { background: #DCFCE7; color: #166534; }")
          .append(".status-unpaid { background: #FEF3C7; color: #92400E; }")
          .append(".qr-box { margin-top: 30px; padding: 15px; border: 1px dashed #94A3B8; text-align: center; font-size: 12px; background: #F8FAFC; border-radius: 6px; }")
          .append("</style></head><body>");

        // Clean Text Header
        sb.append("<div class='header'>")
          .append("<div class='discom-title'>").append(customer.getDiscomName()).append(" ELECTRICITY DISTRIBUTION</div>")
          .append("<div class='sub-title'>Govt. of India Recognized Power Utility Provider | GSTIN: 27AAACE1820E1Z6</div>")
          .append("</div>");

        // Consumer Info
        sb.append("<table class='bill-box'>")
          .append("<tr><td class='label'>Bill Invoice No:</td><td><b>INV-IN-").append(bill.getBillId() + 100000).append("</b></td>")
          .append("<td class='label'>Bill Date:</td><td>").append(bill.getBillDate()).append("</td></tr>")
          .append("<tr><td class='label'>Consumer Name:</td><td><b>").append(customer.getName()).append("</b></td>")
          .append("<td class='label'>Due Date:</td><td><b style='color:#E11D48;'>").append(bill.getDueDate()).append("</b></td></tr>")
          .append("<tr><td class='label'>Consumer Account No:</td><td><b>").append(customer.getConsumerNo()).append("</b></td>")
          .append("<td class='label'>Category Tariff:</td><td>").append(customer.getCategory()).append("</td></tr>")
          .append("<tr><td class='label'>Meter No / Address:</td><td>").append(customer.getMeterNo()).append(" | ").append(customer.getAddress()).append("</td>")
          .append("<td class='label'>Payment Status:</td><td>");

        if ("PAID".equalsIgnoreCase(bill.getPaymentStatus())) {
            sb.append("<span class='status-badge status-paid'>PAID (").append(bill.getPaymentDate() != null ? bill.getPaymentDate() : "COMPLETED").append(")</span>");
        } else {
            sb.append("<span class='status-badge status-unpaid'>UNPAID (DUE)</span>");
        }
        sb.append("</td></tr></table>");

        // Consumption summary
        sb.append("<h4 style='color:#0F172A; margin-bottom:5px;'>1. Consumption Breakdown (Total: ").append(bill.getUnitsConsumed()).append(" kWh Units)</h4>");
        sb.append("<table class='table-slab'><tr>")
          .append("<th>Tariff Slab Tier</th><th>Rate per Unit (₹)</th><th>Billed Units (kWh)</th><th>Slab Subtotal (₹)</th></tr>");

        for (SlabCalculatorService.SlabItem item : breakdown.getSlabItems()) {
            sb.append("<tr>")
              .append("<td>").append(item.getRangeDisplay()).append("</td>")
              .append("<td>₹ ").append(String.format("%.2f", item.getRatePerUnit())).append("</td>")
              .append("<td>").append(String.format("%.1f", item.getUnitsInSlab())).append(" kWh</td>")
              .append("<td><b>₹ ").append(String.format("%.2f", item.getSlabTotal())).append("</b></td>")
              .append("</tr>");
        }
        sb.append("</table>");

        // Totals
        sb.append("<h4 style='color:#0F172A; margin-bottom:5px;'>2. Charges & Tax Breakdown</h4>");
        sb.append("<table class='amount-table'>")
          .append("<tr><td style='text-align:left;'>Energy Charges Subtotal:</td><td>₹ ").append(String.format("%.2f", bill.getEnergyCharge())).append("</td></tr>")
          .append("<tr><td style='text-align:left;'>Fixed Meter / Demand Charge:</td><td>₹ ").append(String.format("%.2f", bill.getFixedCharge())).append("</td></tr>")
          .append("<tr><td style='text-align:left;'>State Electricity Duty / Tax (").append(String.format("%.1f", breakdown.getTaxPercent())).append("%):</td><td>₹ ").append(String.format("%.2f", bill.getTaxAmount())).append("</td></tr>");

        if (bill.getSubsidyAmount() > 0) {
            sb.append("<tr><td style='text-align:left; color:#166534;'>Less: Govt Electricity Subsidy:</td><td style='color:#166534;'>- ₹ ").append(String.format("%.2f", bill.getSubsidyAmount())).append("</td></tr>");
        }

        sb.append("<tr class='total-row'><td style='text-align:left; padding:10px;'>NET PAYABLE AMOUNT:</td><td style='padding:10px;'>₹ ").append(String.format("%.2f", bill.getTotalAmount())).append("</td></tr>")
          .append("</table><div style='clear:both;'></div>");

        // UPI Mockup QR box
        sb.append("<div class='qr-box'>")
          .append("<b>📲 Easy Digital Payment (UPI / Bharat BillPay)</b><br/>")
          .append("Scan with PhonePe / Google Pay / Paytm / BHIM UPI<br/>")
          .append("<code style='font-size:11px; color:#475569;'>upi://pay?pa=").append(customer.getDiscomName().toLowerCase()).append("@billpay&pn=").append(customer.getDiscomName()).append("&am=").append(bill.getTotalAmount()).append("&cu=INR</code><br/>")
          .append("<span style='font-size:11px; color:#64748B;'>Please pay on or before due date to avoid late payment surcharge @ 1.25% per month.</span>")
          .append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }
}
