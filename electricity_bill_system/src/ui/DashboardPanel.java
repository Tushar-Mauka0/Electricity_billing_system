package ui;

import dao.AIInsightDAO;
import dao.BillDAO;
import dao.CustomerDAO;
import model.Bill;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel {
    private final BillDAO billDAO = new BillDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AIInsightDAO aiInsightDAO = new AIInsightDAO();

    private JPanel metricsContainer;
    private JTable recentBillsTable;
    private DefaultTableModel tableModel;
    private Runnable navigateCallback;

    public DashboardPanel(Runnable navigateCallback) {
        this.navigateCallback = navigateCallback;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("DISCOM Utility Billing & AI Dashboard");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Real-time Overview of Indian Grid Consumption, Revenues, & Anomaly Flags");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);

        headerPanel.add(titleBox, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content (Metrics Cards + Table)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // Metrics Grid (4 cards)
        metricsContainer = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsContainer.setOpaque(false);
        centerPanel.add(metricsContainer, BorderLayout.NORTH);

        // Recent Activity Table Panel
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JLabel tableTitle = new JLabel("Recent Billing Transactions");
        tableTitle.setFont(UITheme.FONT_SUBTITLE);
        tableTitle.setForeground(UITheme.TEXT_DARK);

        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Bill ID", "Consumer No", "Customer Name", "Category", "Units (kWh)", "Total (₹)", "Due Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recentBillsTable = new JTable(tableModel);
        recentBillsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UITheme.styleTable(recentBillsTable);

        JScrollPane scrollPane = new JScrollPane(recentBillsTable);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        metricsContainer.removeAll();

        double revenue = billDAO.getTotalRevenueCollected();
        int customersCount = customerDAO.getTotalCustomerCount();
        int pendingBills = billDAO.getPendingBillCount();
        int anomaliesCount = aiInsightDAO.getAnomalyCount();

        NumberFormat currFmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        metricsContainer.add(UITheme.createMetricCard("TOTAL REVENUE", currFmt.format(revenue), "Collected Payments", UITheme.COLOR_SUCCESS));
        metricsContainer.add(UITheme.createMetricCard("TOTAL CONSUMERS", String.valueOf(customersCount), "Active Connections", UITheme.PRIMARY));
        metricsContainer.add(UITheme.createMetricCard("PENDING INVOICES", String.valueOf(pendingBills), "Awaiting Settlement", UITheme.COLOR_WARNING));
        metricsContainer.add(UITheme.createMetricCard("AI ANOMALY FLAGS", String.valueOf(anomaliesCount), "Spikes / Leaks Flagged", UITheme.COLOR_DANGER));

        metricsContainer.revalidate();
        metricsContainer.repaint();

        // Refresh Recent Bills Table
        tableModel.setRowCount(0);
        List<Bill> bills = billDAO.getAllBills();
        int limit = Math.min(10, bills.size());
        for (int i = 0; i < limit; i++) {
            Bill b = bills.get(i);
            tableModel.addRow(new Object[]{
                    "INV-IN-" + (b.getBillId() + 100000),
                    b.getConsumerNo(),
                    b.getCustomerName(),
                    b.getCategory(),
                    String.format("%.1f kWh", b.getUnitsConsumed()),
                    String.format("₹ %.2f", b.getTotalAmount()),
                    b.getDueDate(),
                    b.getPaymentStatus()
            });
        }
    }
}
