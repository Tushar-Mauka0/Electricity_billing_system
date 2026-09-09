package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Customer {
    private int customerId;
    private String consumerNo;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String category; // Residential, Commercial, Industrial
    private String discomName; // MSEDCL, BESCOM, TATA Power, etc.
    private String meterNo;
    private Date connectionDate;
    private Timestamp createdAt;

    public Customer() {}

    public Customer(int customerId, String consumerNo, String name, String email, String phone,
                    String address, String category, String discomName, String meterNo, Date connectionDate) {
        this.customerId = customerId;
        this.consumerNo = consumerNo;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.category = category;
        this.discomName = discomName;
        this.meterNo = meterNo;
        this.connectionDate = connectionDate;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getConsumerNo() { return consumerNo; }
    public void setConsumerNo(String consumerNo) { this.consumerNo = consumerNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDiscomName() { return discomName; }
    public void setDiscomName(String discomName) { this.discomName = discomName; }

    public String getMeterNo() { return meterNo; }
    public void setMeterNo(String meterNo) { this.meterNo = meterNo; }

    public Date getConnectionDate() { return connectionDate; }
    public void setConnectionDate(Date connectionDate) { this.connectionDate = connectionDate; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return consumerNo + " - " + name + " (" + category + ")";
    }
}
