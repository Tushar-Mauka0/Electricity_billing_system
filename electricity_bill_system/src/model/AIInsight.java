package model;

import java.sql.Timestamp;

public class AIInsight {
    private int insightId;
    private int customerId;
    private boolean anomaly;
    private String severity; // NONE, LOW, MEDIUM, HIGH
    private String anomalyReason;
    private double predictedNextUnits;
    private double predictedNextBill;
    private String optimizationTips;
    private Timestamp generatedAt;

    // Joined fields
    private String consumerNo;
    private String customerName;
    private String category;

    public AIInsight() {}

    public AIInsight(int insightId, int customerId, boolean anomaly, String severity,
                     String anomalyReason, double predictedNextUnits, double predictedNextBill,
                     String optimizationTips, Timestamp generatedAt) {
        this.insightId = insightId;
        this.customerId = customerId;
        this.anomaly = anomaly;
        this.severity = severity;
        this.anomalyReason = anomalyReason;
        this.predictedNextUnits = predictedNextUnits;
        this.predictedNextBill = predictedNextBill;
        this.optimizationTips = optimizationTips;
        this.generatedAt = generatedAt;
    }

    public int getInsightId() { return insightId; }
    public void setInsightId(int insightId) { this.insightId = insightId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public boolean isAnomaly() { return anomaly; }
    public void setAnomaly(boolean anomaly) { this.anomaly = anomaly; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getAnomalyReason() { return anomalyReason; }
    public void setAnomalyReason(String anomalyReason) { this.anomalyReason = anomalyReason; }

    public double getPredictedNextUnits() { return predictedNextUnits; }
    public void setPredictedNextUnits(double predictedNextUnits) { this.predictedNextUnits = predictedNextUnits; }

    public double getPredictedNextBill() { return predictedNextBill; }
    public void setPredictedNextBill(double predictedNextBill) { this.predictedNextBill = predictedNextBill; }

    public String getOptimizationTips() { return optimizationTips; }
    public void setOptimizationTips(String optimizationTips) { this.optimizationTips = optimizationTips; }

    public Timestamp getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Timestamp generatedAt) { this.generatedAt = generatedAt; }

    public String getConsumerNo() { return consumerNo; }
    public void setConsumerNo(String consumerNo) { this.consumerNo = consumerNo; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
