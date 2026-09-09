package service;

import dao.AIInsightDAO;
import dao.CustomerDAO;
import dao.MeterReadingDAO;
import model.AIInsight;
import model.Customer;
import model.MeterReading;

import java.util.List;

public class AIConsumptionService {
    private final MeterReadingDAO meterReadingDAO = new MeterReadingDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AIInsightDAO aiInsightDAO = new AIInsightDAO();
    private final SlabCalculatorService slabCalculatorService = new SlabCalculatorService();

    public AIInsight analyzeCustomerConsumption(int customerId, double latestUnitsConsumed) {
        Customer c = customerDAO.getCustomerById(customerId);
        if (c == null) return null;

        List<MeterReading> historicalReadings = meterReadingDAO.getReadingsByCustomer(customerId);

        double totalHistoricalUnits = 0.0;
        int count = 0;
        for (MeterReading r : historicalReadings) {
            // Exclude the very latest reading if analyzing it
            totalHistoricalUnits += r.getUnitsConsumed();
            count++;
        }

        double movingAvg = (count > 0) ? (totalHistoricalUnits / count) : latestUnitsConsumed;

        boolean isAnomaly = false;
        String severity = "NONE";
        String anomalyReason = "Normal consumption within standard expected distribution.";

        if (count >= 1 && movingAvg > 0) {
            double deviationPercent = ((latestUnitsConsumed - movingAvg) / movingAvg) * 100.0;

            if (deviationPercent > 75.0) {
                isAnomaly = true;
                severity = "HIGH";
                anomalyReason = String.format("Critical spike: Consumption of %.1f kWh exceeds historical average (%.1f kWh) by +%.1f%%. Possible meter bypass, leakage, or major machinery fault.",
                        latestUnitsConsumed, movingAvg, deviationPercent);
            } else if (deviationPercent > 40.0) {
                isAnomaly = true;
                severity = "MEDIUM";
                anomalyReason = String.format("Unusual consumption surge: Current usage is +%.1f%% above 3-month baseline (%.1f kWh). Inspect HVAC or appliance health.",
                        deviationPercent, movingAvg);
            } else if (deviationPercent < -50.0) {
                isAnomaly = true;
                severity = "LOW";
                anomalyReason = String.format("Significant drop: Consumption is -%.1f%% below average. Verify meter reader accuracy or property occupancy.",
                        Math.abs(deviationPercent));
            }
        }

        // Forecast next month
        double predictedNextUnits;
        if (count >= 2) {
            // Simple trend slope prediction
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
            int n = historicalReadings.size();
            for (int i = 0; i < n; i++) {
                double x = i + 1;
                double y = historicalReadings.get(i).getUnitsConsumed();
                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumX2 += x * x;
            }
            double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
            double intercept = (sumY - slope * sumX) / n;
            predictedNextUnits = Math.max(20.0, slope * (n + 1) + intercept);
        } else {
            predictedNextUnits = latestUnitsConsumed * 1.05; // 5% growth baseline
        }

        SlabCalculatorService.BillBreakdown predictedBill = slabCalculatorService.calculateBill(predictedNextUnits, c.getCategory());
        double predictedNextBillAmount = predictedBill.getTotalPayable();

        // Custom AI Optimization Tips based on Category & Units
        String tips = generateOptimizationTips(c.getCategory(), latestUnitsConsumed, isAnomaly);

        AIInsight insight = new AIInsight();
        insight.setCustomerId(customerId);
        insight.setAnomaly(isAnomaly);
        insight.setSeverity(severity);
        insight.setAnomalyReason(anomalyReason);
        insight.setPredictedNextUnits(Math.round(predictedNextUnits * 100.0) / 100.0);
        insight.setPredictedNextBill(Math.round(predictedNextBillAmount * 100.0) / 100.0);
        insight.setOptimizationTips(tips);
        insight.setCustomerName(c.getName());
        insight.setConsumerNo(c.getConsumerNo());
        insight.setCategory(c.getCategory());

        aiInsightDAO.saveOrUpdateInsight(insight);
        return insight;
    }

    private String generateOptimizationTips(String category, double units, boolean isAnomaly) {
        StringBuilder sb = new StringBuilder();

        if (isAnomaly) {
            sb.append("⚠️ HIGH PRIORITY ACTION: Immediate onsite energy audit recommended. Check main circuit breaker, ground leakage, or motor insulation.\n\n");
        }

        if ("Residential".equalsIgnoreCase(category)) {
            if (units <= 100) {
                sb.append("✓ Excellent energy conservation! Usage is within the 100 kWh subsidized slab (₹100 Govt Subsidy applied).\n");
                sb.append("• Tip: Switch remaining incandescent bulbs to 9W BEE 5-Star LED lamps to stay in this tier.");
            } else if (units <= 300) {
                sb.append("• Moderate usage slab (101-300 units @ ₹3.50/unit).\n");
                sb.append("• Recommendation: Set Inverter ACs to 24°C instead of 18°C to reduce monthly power draw by ~18%.\n");
                sb.append("• Tip: Use smart plug timers for geysers during morning hours (7:00 AM - 9:00 AM).");
            } else {
                sb.append("⚠️ High consumption slab (>300 units @ higher tariff rates up to ₹8.50/unit).\n");
                sb.append("• Rooftop Solar Feasibility: Installing a 3 kW Grid-Tied PM Surya Ghar solar rooftop system can offset ~360 units/month with up to ₹78,000 Central Govt Subsidy.\n");
                sb.append("• Load Shifting: Operate high wattage washing machines & water pumps outside peak evening hours (6:00 PM - 10:00 PM).");
            }
        } else if ("Commercial".equalsIgnoreCase(category)) {
            sb.append("• Commercial Tariff Optimization:\n");
            sb.append("• Power Factor Penalty Avoidance: Install Automatic Power Factor Controller (APFC) panels to maintain PF above 0.98 and gain 2% DISCOM rebate.\n");
            sb.append("• Lighting: Upgrade office troffers to motion-sensor dimmable LED fixtures.\n");
            sb.append("• HVAC: Conduct quarterly chiller coil cleaning & install VFDs on chilled water pumps.");
        } else {
            sb.append("• Industrial Heavy Load Energy Management:\n");
            sb.append("• Time-of-Day (ToD) Tariff Savings: Shift energy-intensive melting/forge operations to off-peak night hours (10:00 PM to 06:00 AM) to receive ToD rebates up to ₹1.50/kWh.\n");
            sb.append("• Motor Efficiency: Replace old IE1/IE2 induction motors with Premium Efficiency IE4/IE5 synchronous reluctance motors.\n");
            sb.append("• Thermal Insulation: Insulate steam lines and furnace walls to prevent radiant heat loss.");
        }

        return sb.toString();
    }
}
