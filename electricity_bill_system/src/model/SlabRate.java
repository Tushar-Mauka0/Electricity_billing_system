package model;

public class SlabRate {
    private int slabId;
    private String category;
    private int minUnits;
    private int maxUnits;
    private double ratePerUnit;
    private double fixedCharge;
    private double taxPercent;
    private double subsidyAmount;

    public SlabRate() {}

    public SlabRate(int slabId, String category, int minUnits, int maxUnits,
                    double ratePerUnit, double fixedCharge, double taxPercent, double subsidyAmount) {
        this.slabId = slabId;
        this.category = category;
        this.minUnits = minUnits;
        this.maxUnits = maxUnits;
        this.ratePerUnit = ratePerUnit;
        this.fixedCharge = fixedCharge;
        this.taxPercent = taxPercent;
        this.subsidyAmount = subsidyAmount;
    }

    public int getSlabId() { return slabId; }
    public void setSlabId(int slabId) { this.slabId = slabId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getMinUnits() { return minUnits; }
    public void setMinUnits(int minUnits) { this.minUnits = minUnits; }

    public int getMaxUnits() { return maxUnits; }
    public void setMaxUnits(int maxUnits) { this.maxUnits = maxUnits; }

    public double getRatePerUnit() { return ratePerUnit; }
    public void setRatePerUnit(double ratePerUnit) { this.ratePerUnit = ratePerUnit; }

    public double getFixedCharge() { return fixedCharge; }
    public void setFixedCharge(double fixedCharge) { this.fixedCharge = fixedCharge; }

    public double getTaxPercent() { return taxPercent; }
    public void setTaxPercent(double taxPercent) { this.taxPercent = taxPercent; }

    public double getSubsidyAmount() { return subsidyAmount; }
    public void setSubsidyAmount(double subsidyAmount) { this.subsidyAmount = subsidyAmount; }

    public String getSlabRangeDisplay() {
        if (maxUnits >= 999999) {
            return "> " + minUnits + " units";
        }
        return minUnits + " - " + maxUnits + " units";
    }
}
