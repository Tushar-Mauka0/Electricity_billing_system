package model;

import java.sql.Date;
import java.sql.Timestamp;

public class MeterReading {
    private int readingId;
    private int customerId;
    private Date readingDate;
    private double prevReading;
    private double currReading;
    private double unitsConsumed;
    private String readerName;
    private Timestamp createdAt;

    // Additional display field
    private String consumerName;
    private String consumerNo;

    public MeterReading() {}

    public MeterReading(int readingId, int customerId, Date readingDate, double prevReading,
                        double currReading, double unitsConsumed, String readerName) {
        this.readingId = readingId;
        this.customerId = customerId;
        this.readingDate = readingDate;
        this.prevReading = prevReading;
        this.currReading = currReading;
        this.unitsConsumed = unitsConsumed;
        this.readerName = readerName;
    }

    public int getReadingId() { return readingId; }
    public void setReadingId(int readingId) { this.readingId = readingId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Date getReadingDate() { return readingDate; }
    public void setReadingDate(Date readingDate) { this.readingDate = readingDate; }

    public double getPrevReading() { return prevReading; }
    public void setPrevReading(double prevReading) { this.prevReading = prevReading; }

    public double getCurrReading() { return currReading; }
    public void setCurrReading(double currReading) { this.currReading = currReading; }

    public double getUnitsConsumed() { return unitsConsumed; }
    public void setUnitsConsumed(double unitsConsumed) { this.unitsConsumed = unitsConsumed; }

    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getConsumerName() { return consumerName; }
    public void setConsumerName(String consumerName) { this.consumerName = consumerName; }

    public String getConsumerNo() { return consumerNo; }
    public void setConsumerNo(String consumerNo) { this.consumerNo = consumerNo; }
}
