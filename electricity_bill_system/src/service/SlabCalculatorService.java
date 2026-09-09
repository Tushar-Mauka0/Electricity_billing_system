package service;

import dao.SlabRateDAO;
import model.SlabRate;

import java.util.ArrayList;
import java.util.List;

public class SlabCalculatorService {
    private final SlabRateDAO slabRateDAO = new SlabRateDAO();

    public static class SlabItem {
        private String rangeDisplay;
        private double unitsInSlab;
        private double ratePerUnit;
        private double slabTotal;

        public SlabItem(String rangeDisplay, double unitsInSlab, double ratePerUnit, double slabTotal) {
            this.rangeDisplay = rangeDisplay;
            this.unitsInSlab = unitsInSlab;
            this.ratePerUnit = ratePerUnit;
            this.slabTotal = slabTotal;
        }

        public String getRangeDisplay() { return rangeDisplay; }
        public double getUnitsInSlab() { return unitsInSlab; }
        public double getRatePerUnit() { return ratePerUnit; }
        public double getSlabTotal() { return slabTotal; }
    }

    public static class BillBreakdown {
        private double unitsConsumed;
        private String category;
        private List<SlabItem> slabItems = new ArrayList<>();
        private double energyCharge;
        private double fixedCharge;
        private double taxPercent;
        private double taxAmount;
        private double subsidyAmount;
        private double totalPayable;

        public double getUnitsConsumed() { return unitsConsumed; }
        public String getCategory() { return category; }
        public List<SlabItem> getSlabItems() { return slabItems; }
        public double getEnergyCharge() { return energyCharge; }
        public double getFixedCharge() { return fixedCharge; }
        public double getTaxPercent() { return taxPercent; }
        public double getTaxAmount() { return taxAmount; }
        public double getSubsidyAmount() { return subsidyAmount; }
        public double getTotalPayable() { return totalPayable; }
    }

    public BillBreakdown calculateBill(double unitsConsumed, String category) {
        List<SlabRate> slabs = slabRateDAO.getSlabRatesByCategory(category);
        if (slabs == null || slabs.isEmpty()) {
            // Default fallback if database slab empty
            slabs = getDefaultSlabsForCategory(category);
        }

        BillBreakdown breakdown = new BillBreakdown();
        breakdown.unitsConsumed = unitsConsumed;
        breakdown.category = category;

        double remainingUnits = Math.max(0, unitsConsumed);
        double energyChargeSum = 0.0;
        double fixedCharge = 0.0;
        double taxPercent = 0.0;
        double subsidyAmount = 0.0;

        for (SlabRate slab : slabs) {
            fixedCharge = Math.max(fixedCharge, slab.getFixedCharge());
            taxPercent = Math.max(taxPercent, slab.getTaxPercent());

            if (remainingUnits > 0) {
                int minU = slab.getMinUnits();
                int maxU = slab.getMaxUnits();
                double slabCapacity = (minU == 0) ? maxU : (maxU - minU + 1);

                double unitsInThisSlab = Math.min(remainingUnits, slabCapacity);
                if (unitsInThisSlab > 0) {
                    double slabTotal = unitsInThisSlab * slab.getRatePerUnit();
                    energyChargeSum += slabTotal;
                    breakdown.slabItems.add(new SlabItem(slab.getSlabRangeDisplay(), unitsInThisSlab, slab.getRatePerUnit(), slabTotal));
                    remainingUnits -= unitsInThisSlab;
                }
            }

            // Check subsidy (e.g. if residential and unitsConsumed <= 100)
            if (unitsConsumed <= 100 && slab.getSubsidyAmount() > 0) {
                subsidyAmount = Math.max(subsidyAmount, slab.getSubsidyAmount());
            }
        }

        double taxableBase = energyChargeSum + fixedCharge;
        double taxAmount = (taxableBase * taxPercent) / 100.0;
        double totalPayable = Math.max(0, taxableBase + taxAmount - subsidyAmount);

        breakdown.energyCharge = round2(energyChargeSum);
        breakdown.fixedCharge = round2(fixedCharge);
        breakdown.taxPercent = round2(taxPercent);
        breakdown.taxAmount = round2(taxAmount);
        breakdown.subsidyAmount = round2(subsidyAmount);
        breakdown.totalPayable = round2(totalPayable);

        return breakdown;
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private List<SlabRate> getDefaultSlabsForCategory(String category) {
        List<SlabRate> list = new ArrayList<>();
        if ("Residential".equalsIgnoreCase(category)) {
            list.add(new SlabRate(1, "Residential", 0, 100, 1.50, 85.00, 9.00, 100.00));
            list.add(new SlabRate(2, "Residential", 101, 300, 3.50, 85.00, 9.00, 0.00));
            list.add(new SlabRate(3, "Residential", 301, 500, 6.00, 85.00, 9.00, 0.00));
            list.add(new SlabRate(4, "Residential", 501, 999999, 8.50, 85.00, 9.00, 0.00));
        } else if ("Commercial".equalsIgnoreCase(category)) {
            list.add(new SlabRate(5, "Commercial", 0, 200, 6.50, 250.00, 12.00, 0.00));
            list.add(new SlabRate(6, "Commercial", 201, 500, 8.50, 250.00, 12.00, 0.00));
            list.add(new SlabRate(7, "Commercial", 501, 999999, 11.00, 250.00, 12.00, 0.00));
        } else {
            list.add(new SlabRate(8, "Industrial", 0, 1000, 8.00, 750.00, 15.00, 0.00));
            list.add(new SlabRate(9, "Industrial", 1001, 999999, 10.50, 750.00, 15.00, 0.00));
        }
        return list;
    }
}
