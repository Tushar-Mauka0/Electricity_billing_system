package ui;

import config.DBConnection;
import ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private JPanel contentCardPanel;
    private CardLayout cardLayout;

    private DashboardPanel dashboardPanel;
    private CustomerPanel customerPanel;
    private MeterReadingPanel meterReadingPanel;
    private TariffConfigPanel tariffConfigPanel;
    private BillManagementPanel billManagementPanel;
    private AIAnalyticsPanel aiAnalyticsPanel;

    private Map<String, JButton> navButtons = new HashMap<>();

    public MainFrame() {
        setTitle("Electricity Bill Generation & AI Analytics System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout(10, 0));
        topHeader.setBackground(UITheme.NAV_DARK);
        topHeader.setPreferredSize(new Dimension(getWidth(), 55));
        topHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(51, 65, 85)));

        JPanel logoTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        logoTitlePanel.setOpaque(false);

        JLabel logoLbl = new JLabel("POWER-GRID SMART BILLING SYSTEM");
        logoLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        logoLbl.setForeground(Color.WHITE);
        logoTitlePanel.add(logoLbl);

        String activeDb = DBConnection.getActiveDbType();
        JLabel dbBadge = new JLabel("Database Engine: " + activeDb + "  ");
        dbBadge.setFont(UITheme.FONT_SMALL);
        dbBadge.setForeground(UITheme.PRIMARY);

        topHeader.add(logoTitlePanel, BorderLayout.WEST);
        topHeader.add(dbBadge, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // Sidebar Navigation Panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.NAV_DARK);
        sidebar.setPreferredSize(new Dimension(210, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 8, 15, 8));

        sidebar.add(createNavButton("Dashboard", "DASHBOARD"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Consumers", "CUSTOMERS"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Meter Readings", "METER_READINGS"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Tariff Slabs", "TARIFF_CONFIG"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Bills & Invoices", "BILLS"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("AI Analytics", "AI_ANALYTICS"));

        sidebar.add(Box.createVerticalGlue());

        JLabel footerLbl = new JLabel("<html><div style='color:#64748B; font-size:10px;'>v2.5 (INR Tariffs)<br/>Java Swing + PDF Engine</div></html>");
        sidebar.add(footerLbl);

        add(sidebar, BorderLayout.WEST);

        // Center Content Area with CardLayout
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);

        dashboardPanel = new DashboardPanel(() -> switchTab("METER_READINGS"));
        customerPanel = new CustomerPanel();
        meterReadingPanel = new MeterReadingPanel();
        tariffConfigPanel = new TariffConfigPanel();
        billManagementPanel = new BillManagementPanel();
        aiAnalyticsPanel = new AIAnalyticsPanel();

        contentCardPanel.add(wrapInScrollPane(dashboardPanel), "DASHBOARD");
        contentCardPanel.add(wrapInScrollPane(customerPanel), "CUSTOMERS");
        contentCardPanel.add(wrapInScrollPane(meterReadingPanel), "METER_READINGS");
        contentCardPanel.add(wrapInScrollPane(tariffConfigPanel), "TARIFF_CONFIG");
        contentCardPanel.add(wrapInScrollPane(billManagementPanel), "BILLS");
        contentCardPanel.add(wrapInScrollPane(aiAnalyticsPanel), "AI_ANALYTICS");

        add(contentCardPanel, BorderLayout.CENTER);

        switchTab("DASHBOARD");
    }

    private JScrollPane wrapInScrollPane(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JButton createNavButton(String text, String cardKey) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_HEADER);
        btn.setForeground(new Color(203, 213, 225)); // Slate 300
        btn.setBackground(UITheme.NAV_DARK);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(190, 40));
        btn.setPreferredSize(new Dimension(190, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ActionListener act = e -> switchTab(cardKey);
        btn.addActionListener(act);

        navButtons.put(cardKey, btn);
        return btn;
    }

    public void switchTab(String cardKey) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            if (entry.getKey().equals(cardKey)) {
                b.setBackground(UITheme.NAV_HOVER);
                b.setForeground(UITheme.PRIMARY);
            } else {
                b.setBackground(UITheme.NAV_DARK);
                b.setForeground(new Color(203, 213, 225));
            }
        }

        // Trigger Data Refresh on Switch
        switch (cardKey) {
            case "DASHBOARD":
                dashboardPanel.refreshData();
                break;
            case "CUSTOMERS":
                customerPanel.refreshCustomerTable();
                break;
            case "METER_READINGS":
                meterReadingPanel.loadCustomers();
                break;
            case "TARIFF_CONFIG":
                tariffConfigPanel.refreshSlabTable();
                break;
            case "BILLS":
                billManagementPanel.refreshBillTable();
                break;
            case "AI_ANALYTICS":
                aiAnalyticsPanel.refreshAIAnalytics();
                break;
        }

        cardLayout.show(contentCardPanel, cardKey);
    }
}
