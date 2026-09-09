package ui;

import dao.AIInsightDAO;
import dao.CustomerDAO;
import dao.MeterReadingDAO;
import model.AIInsight;
import model.Customer;
import model.MeterReading;
import service.AIConsumptionService;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AIAnalyticsPanel extends JPanel {
    private final AIInsightDAO aiInsightDAO = new AIInsightDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final MeterReadingDAO meterReadingDAO = new MeterReadingDAO();
    private final AIConsumptionService aiConsumptionService = new AIConsumptionService();

    private JTable anomalyTable;
    private DefaultTableModel tableModel;

    private JComboBox<Customer> customerCombo;
    private JLabel forecastUnitsLbl;
    private JLabel forecastBillLbl;
    private JTextArea tipsArea;
    private JTextArea reasonArea;

    public AIAnalyticsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initUI();
        refreshAIAnalytics();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("AI Utility Analytics & Energy Optimization");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Spike Anomaly Detection, Predictive Consumption Trend Modeling, & Custom Conservation Strategies");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Main Layout (Top: Anomaly Table Card, Bottom: Forecast & Advisor Split)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setResizeWeight(0.45);
        mainSplit.setContinuousLayout(true);
        mainSplit.setOpaque(false);
        mainSplit.setBorder(null);

        // Top Anomaly Card
        JPanel topCard = UITheme.createCardPanel();
        topCard.setLayout(new BorderLayout(10, 10));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel anomalyTitle = new JLabel("Meter Consumption Anomaly & Tamper Detector");
        anomalyTitle.setFont(UITheme.FONT_SUBTITLE);
        anomalyTitle.setForeground(UITheme.COLOR_DANGER);

        JButton runAuditBtn = UITheme.createPrimaryButton("Run Full Grid AI Audit");
        runAuditBtn.addActionListener(e -> runFullGridAudit());

        topHeader.add(anomalyTitle, BorderLayout.WEST);
        topHeader.add(runAuditBtn, BorderLayout.EAST);

        topCard.add(topHeader, BorderLayout.NORTH);

        String[] cols = {"ID", "Consumer No", "Customer Name", "Category", "Anomaly Flag", "Severity", "Detected Diagnosis"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        anomalyTable = new JTable(tableModel);
        anomalyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UITheme.styleTable(anomalyTable);

        anomalyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onAnomalySelected();
            }
        });

        topCard.add(new JScrollPane(anomalyTable), BorderLayout.CENTER);

        mainSplit.setTopComponent(topCard);

        // Bottom Split (Left: Predictive Forecaster, Right: AI Conservation Advisor)
        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bottomSplit.setResizeWeight(0.5);
        bottomSplit.setContinuousLayout(true);
        bottomSplit.setOpaque(false);
        bottomSplit.setBorder(null);

        // Left Forecast Card
        JPanel forecastCard = UITheme.createCardPanel();
        forecastCard.setLayout(new BorderLayout(10, 10));

        JLabel fcTitle = new JLabel("Predictive Metering (Next Cycle Forecast)");
        fcTitle.setFont(UITheme.FONT_SUBTITLE);
        forecastCard.add(fcTitle, BorderLayout.NORTH);

        JPanel fcGrid = new JPanel(new GridBagLayout());
        fcGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        customerCombo = new JComboBox<>();
        customerCombo.setFont(UITheme.FONT_BODY);
        customerCombo.addActionListener(e -> loadForecastForSelectedCustomer());

        forecastUnitsLbl = new JLabel("--- kWh");
        forecastUnitsLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        forecastUnitsLbl.setForeground(UITheme.PRIMARY_DARK);

        forecastBillLbl = new JLabel("₹ ---");
        forecastBillLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        forecastBillLbl.setForeground(UITheme.COLOR_SUCCESS);

        reasonArea = new JTextArea(3, 15);
        reasonArea.setFont(UITheme.FONT_SMALL);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setEditable(false);
        reasonArea.setBackground(new Color(248, 250, 252));

        int r = 0;
        addFormField(fcGrid, gbc, "Select Consumer:", customerCombo, r++);
        addFormField(fcGrid, gbc, "Projected Energy Load:", forecastUnitsLbl, r++);
        addFormField(fcGrid, gbc, "Estimated Bill Amount:", forecastBillLbl, r++);

        gbc.gridx = 0; gbc.gridy = r++; gbc.weightx = 0.35;
        fcGrid.add(new JLabel("Anomaly Diagnosis:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        fcGrid.add(new JScrollPane(reasonArea), gbc);

        forecastCard.add(new JScrollPane(fcGrid), BorderLayout.CENTER);

        bottomSplit.setLeftComponent(forecastCard);

        // Right Advisor Card
        JPanel advisorCard = UITheme.createCardPanel();
        advisorCard.setLayout(new BorderLayout(10, 10));

        JLabel advTitle = new JLabel("AI Energy Conservation Advisor");
        advTitle.setFont(UITheme.FONT_SUBTITLE);
        advTitle.setForeground(UITheme.COLOR_WARNING.darker());
        advisorCard.add(advTitle, BorderLayout.NORTH);

        tipsArea = new JTextArea();
        tipsArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tipsArea.setLineWrap(true);
        tipsArea.setWrapStyleWord(true);
        tipsArea.setEditable(false);
        tipsArea.setBackground(new Color(254, 243, 199)); // Amber light tint
        tipsArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_WARNING, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        advisorCard.add(new JScrollPane(tipsArea), BorderLayout.CENTER);

        bottomSplit.setRightComponent(advisorCard);

        mainSplit.setBottomComponent(bottomSplit);

        add(mainSplit, BorderLayout.CENTER);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, Component comp, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    public void refreshAIAnalytics() {
        tableModel.setRowCount(0);
        List<AIInsight> insights = aiInsightDAO.getAllInsights();
        for (AIInsight ai : insights) {
            tableModel.addRow(new Object[]{
                    ai.getInsightId(),
                    ai.getConsumerNo(),
                    ai.getCustomerName(),
                    ai.getCategory(),
                    ai.isAnomaly() ? "ANOMALY" : "NORMAL",
                    ai.getSeverity(),
                    ai.getAnomalyReason()
            });
        }

        // Load Customer dropdown for Forecaster
        customerCombo.removeAllItems();
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer c : customers) {
            customerCombo.addItem(c);
        }
        if (!customers.isEmpty()) {
            loadForecastForSelectedCustomer();
        }
    }

    private void runFullGridAudit() {
        List<Customer> customers = customerDAO.getAllCustomers();
        int count = 0;
        for (Customer c : customers) {
            MeterReading mr = meterReadingDAO.getLatestReadingForCustomer(c.getCustomerId());
            double units = (mr != null) ? mr.getUnitsConsumed() : 150.0;
            aiConsumptionService.analyzeCustomerConsumption(c.getCustomerId(), units);
            count++;
        }
        JOptionPane.showMessageDialog(this, "Completed AI Grid Audit across " + count + " consumer accounts!", "AI Audit Complete", JOptionPane.INFORMATION_MESSAGE);
        refreshAIAnalytics();
    }

    private void onAnomalySelected() {
        int row = anomalyTable.getSelectedRow();
        if (row >= 0) {
            int insightId = (Integer) tableModel.getValueAt(row, 0);
            List<AIInsight> list = aiInsightDAO.getAllInsights();
            for (AIInsight ai : list) {
                if (ai.getInsightId() == insightId) {
                    forecastUnitsLbl.setText(String.format("%.1f kWh", ai.getPredictedNextUnits()));
                    forecastBillLbl.setText(String.format("₹ %.2f", ai.getPredictedNextBill()));
                    reasonArea.setText(ai.getAnomalyReason());
                    tipsArea.setText(ai.getOptimizationTips());
                    break;
                }
            }
        }
    }

    private void loadForecastForSelectedCustomer() {
        Customer c = (Customer) customerCombo.getSelectedItem();
        if (c != null) {
            AIInsight ai = aiInsightDAO.getInsightByCustomer(c.getCustomerId());
            if (ai == null) {
                MeterReading mr = meterReadingDAO.getLatestReadingForCustomer(c.getCustomerId());
                double units = (mr != null) ? mr.getUnitsConsumed() : 150.0;
                ai = aiConsumptionService.analyzeCustomerConsumption(c.getCustomerId(), units);
            }
            if (ai != null) {
                forecastUnitsLbl.setText(String.format("%.1f kWh", ai.getPredictedNextUnits()));
                forecastBillLbl.setText(String.format("₹ %.2f", ai.getPredictedNextBill()));
                reasonArea.setText(ai.getAnomalyReason());
                tipsArea.setText(ai.getOptimizationTips());
            }
        }
    }
}
