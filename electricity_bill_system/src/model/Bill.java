package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Bill {
    private int billId;
    private int customerId;
    private int readingId;
    private Date billDate;
    private Date dueDate;
    private double unitsConsumed;
    private double energyCharge;
    private double fixedCharge;
    private double taxAmount;
    private double subsidyAmount;
    private double totalAmount;
    private String paymentStatus; // UNPAID, PAID, OVERDUE
    private Date paymentDate;
    private Timestamp createdAt;

    // Display fields joined from Customer
    private String consumerNo;
    private String customerName;
    private String category;
    private String discomName;

    public Bill() {}

    public Bill(int billId, int customerId, int readingId, Date billDate, Date dueDate,
                double unitsConsumed, double energyCharge, double fixedCharge, double taxAmount,
                double subsidyAmount, double totalAmount, String paymentStatus, Date paymentDate) {
        this.billId = billId;
        this.customerId = customerId;
        this.readingId = readingId;
        this.billDate = billDate;
        this.dueDate = dueDate;
        this.unitsConsumed = unitsConsumed;
        this.energyCharge = energyCharge;
        this.fixedCharge = fixedCharge;
        this.taxAmount = taxAmount;
        this.subsidyAmount = subsidyAmount;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }

    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getReadingId() { return readingId; }
    public void setReadingId(int readingId) { this.readingId = readingId; }

    public Date getBillDate() { return billDate; }
    public void setBillDate(Date billDate) { this.billDate = billDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public double getUnitsConsumed() { return unitsConsumed; }
    public void setUnitsConsumed(double unitsConsumed) { this.unitsConsumed = unitsConsumed; }

    public double getEnergyCharge() { return energyCharge; }
    public void setEnergyCharge(double energyCharge) { this.energyCharge = energyCharge; }

    public double getFixedCharge() { return fixedCharge; }
    public void setFixedCharge(double fixedCharge) { this.fixedCharge = fixedCharge; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getSubsidyAmount() { return subsidyAmount; }
    public void setSubsidyAmount(double subsidyAmount) { this.subsidyAmount = subsidyAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getConsumerNo() { return consumerNo; }
    public void setConsumerNo(String consumerNo) { this.consumerNo = consumerNo; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDiscomName() { return discomName; }
    public void setDiscomName(String discomName) { this.discomName = discomName; }
}
