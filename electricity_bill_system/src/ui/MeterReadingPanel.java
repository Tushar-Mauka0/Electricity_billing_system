package ui;

import dao.BillDAO;
import dao.CustomerDAO;
import dao.MeterReadingDAO;
import model.Bill;
import model.Customer;
import model.MeterReading;
import service.AIConsumptionService;
import service.SlabCalculatorService;
import ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class MeterReadingPanel extends JPanel {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final MeterReadingDAO meterReadingDAO = new MeterReadingDAO();
    private final BillDAO billDAO = new BillDAO();
    private final SlabCalculatorService slabCalculatorService = new SlabCalculatorService();
    private final AIConsumptionService aiConsumptionService = new AIConsumptionService();

    private JComboBox<Customer> customerCombo;
    private JTextField prevReadingTxt;
    private JTextField currReadingTxt;
    private JTextField unitsConsumedTxt;
    private JTextField readingDateTxt;
    private JTextField readerNameTxt;

    private JEditorPane previewEditor;

    private Customer selectedCustomer;

    public MeterReadingPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initUI();
        loadCustomers();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Meter Reading & Instant Slab Billing");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Record kWh consumption to compute itemized slab charges & trigger AI anomaly audit");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Split Pane (Left Form, Right Real-time Preview)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.48);
        splitPane.setContinuousLayout(true);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Left Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout(10, 10));

        JLabel formTitle = new JLabel("Record Meter Entry");
        formTitle.setFont(UITheme.FONT_SUBTITLE);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        customerCombo = new JComboBox<>();
        customerCombo.setFont(UITheme.FONT_BODY);
        customerCombo.addActionListener(e -> onCustomerSelected());

        prevReadingTxt = new JTextField("0.0");
        prevReadingTxt.setEditable(false);

        currReadingTxt = new JTextField();
        currReadingTxt.setFont(UITheme.FONT_BODY);
        currReadingTxt.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                calculateUnitsAndPreview();
            }
        });

        unitsConsumedTxt = new JTextField("0.0");
        unitsConsumedTxt.setEditable(false);
        unitsConsumedTxt.setFont(new Font("SansSerif", Font.BOLD, 14));
        unitsConsumedTxt.setForeground(UITheme.PRIMARY_DARK);

        readingDateTxt = new JTextField(LocalDate.now().toString());
        readerNameTxt = new JTextField("Meter Reader - Sanjay Kumar");

        int r = 0;
        addFormField(formGrid, gbc, "Select Consumer:", customerCombo, r++);
        addFormField(formGrid, gbc, "Previous Reading (kWh):", prevReadingTxt, r++);
        addFormField(formGrid, gbc, "Current Reading (kWh):*", currReadingTxt, r++);
        addFormField(formGrid, gbc, "Units Consumed (kWh):", unitsConsumedTxt, r++);
        addFormField(formGrid, gbc, "Reading Date (YYYY-MM-DD):", readingDateTxt, r++);
        addFormField(formGrid, gbc, "Meter Reader Name:", readerNameTxt, r++);

        formCard.add(new JScrollPane(formGrid), BorderLayout.CENTER);

        // Action Buttons
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnBox.setOpaque(false);

        JButton calcBtn = UITheme.createSecondaryButton("Recalculate Preview");
        JButton submitBtn = UITheme.createPrimaryButton("Generate Bill & Save");

        calcBtn.addActionListener(e -> calculateUnitsAndPreview());
        submitBtn.addActionListener(e -> submitMeterReading());

        btnBox.add(calcBtn);
        btnBox.add(submitBtn);

        formCard.add(btnBox, BorderLayout.SOUTH);

        splitPane.setLeftComponent(formCard);

        // Right Preview Card
        JPanel previewCard = UITheme.createCardPanel();
        previewCard.setLayout(new BorderLayout(10, 10));

        JLabel prevTitle = new JLabel("Real-time Slab Breakdown Preview");
        prevTitle.setFont(UITheme.FONT_SUBTITLE);
        previewCard.add(prevTitle, BorderLayout.NORTH);

        previewEditor = new JEditorPane();
        previewEditor.setContentType("text/html");
        previewEditor.setEditable(false);

        previewCard.add(new JScrollPane(previewEditor), BorderLayout.CENTER);

        splitPane.setRightComponent(previewCard);

        add(splitPane, BorderLayout.CENTER);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, Component comp, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    public void loadCustomers() {
        customerCombo.removeAllItems();
        List<Customer> list = customerDAO.getAllCustomers();
        for (Customer c : list) {
            customerCombo.addItem(c);
        }
        if (!list.isEmpty()) {
            onCustomerSelected();
        }
    }

    private void onCustomerSelected() {
        selectedCustomer = (Customer) customerCombo.getSelectedItem();
        if (selectedCustomer != null) {
            MeterReading latest = meterReadingDAO.getLatestReadingForCustomer(selectedCustomer.getCustomerId());
            double prev = (latest != null) ? latest.getCurrReading() : 1000.0;
            prevReadingTxt.setText(String.format("%.2f", prev));
            currReadingTxt.setText(String.format("%.2f", prev + 180.0));
            calculateUnitsAndPreview();
        }
    }

    private void calculateUnitsAndPreview() {
        if (selectedCustomer == null) return;
        try {
            double prev = Double.parseDouble(prevReadingTxt.getText().trim());
            double curr = Double.parseDouble(currReadingTxt.getText().trim());
            double units = Math.max(0, curr - prev);
            unitsConsumedTxt.setText(String.format("%.2f", units));

            SlabCalculatorService.BillBreakdown bd = slabCalculatorService.calculateBill(units, selectedCustomer.getCategory());

            StringBuilder html = new StringBuilder();
            html.append("<html><body style='font-family:SansSerif,sans-serif; color:#1E293B; margin:10px;'>");
            html.append("<h3>Tariff Breakdown: ").append(selectedCustomer.getCategory()).append("</h3>");
            html.append("<p>Consumer: <b>").append(selectedCustomer.getName()).append("</b> (").append(selectedCustomer.getConsumerNo()).append(")</p>");
            html.append("<p>DISCOM: <b>").append(selectedCustomer.getDiscomName()).append("</b></p>");
            html.append("<table border='1' cellspacing='0' cellpadding='6' style='width:100%; border-collapse:collapse; border-color:#CBD5E1;'>");
            html.append("<tr style='background:#F1F5F9;'><th>Slab Tier</th><th>Rate (₹)</th><th>Units</th><th>Subtotal (₹)</th></tr>");

            for (SlabCalculatorService.SlabItem item : bd.getSlabItems()) {
                html.append("<tr>")
                    .append("<td>").append(item.getRangeDisplay()).append("</td>")
                    .append("<td>₹ ").append(String.format("%.2f", item.getRatePerUnit())).append("</td>")
                    .append("<td>").append(String.format("%.1f", item.getUnitsInSlab())).append(" kWh</td>")
                    .append("<td><b>₹ ").append(String.format("%.2f", item.getSlabTotal())).append("</b></td>")
                    .append("</tr>");
            }
            html.append("</table>");

            html.append("<div style='margin-top:15px; padding:10px; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:6px;'>");
            html.append("<div>Energy Charges: <b>₹ ").append(String.format("%.2f", bd.getEnergyCharge())).append("</b></div>");
            html.append("<div>Fixed Charge: <b>₹ ").append(String.format("%.2f", bd.getFixedCharge())).append("</b></div>");
            html.append("<div>Electricity Duty / Tax (").append(bd.getTaxPercent()).append("%): <b>₹ ").append(String.format("%.2f", bd.getTaxAmount())).append("</b></div>");

            if (bd.getSubsidyAmount() > 0) {
                html.append("<div style='color:#166534;'>Govt Subsidy Benefit: <b>- ₹ ").append(String.format("%.2f", bd.getSubsidyAmount())).append("</b></div>");
            }

            html.append("<hr style='border-top:1px solid #CBD5E1;'/>");
            html.append("<h3 style='color:#0EA5E9; margin:0;'>TOTAL BILL ESTIMATE: ₹ ").append(String.format("%.2f", bd.getTotalPayable())).append("</h3>");
            html.append("</div></body></html>");

            previewEditor.setText(html.toString());
        } catch (Exception e) {
            previewEditor.setText("<html><body style='font-family:SansSerif;'><p style='color:red;'>Please enter valid numeric readings.</p></body></html>");
        }
    }

    private void submitMeterReading() {
        if (selectedCustomer == null) {
            JOptionPane.showMessageDialog(this, "Please select a consumer.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double prev = Double.parseDouble(prevReadingTxt.getText().trim());
            double curr = Double.parseDouble(currReadingTxt.getText().trim());

            if (curr < prev) {
                JOptionPane.showMessageDialog(this, "Current reading cannot be lower than previous reading!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double units = curr - prev;
            Date rDate = Date.valueOf(readingDateTxt.getText().trim());

            MeterReading mr = new MeterReading();
            mr.setCustomerId(selectedCustomer.getCustomerId());
            mr.setReadingDate(rDate);
            mr.setPrevReading(prev);
            mr.setCurrReading(curr);
            mr.setUnitsConsumed(units);
            mr.setReaderName(readerNameTxt.getText().trim());

            boolean okReading = meterReadingDAO.addReading(mr);
            if (!okReading) {
                JOptionPane.showMessageDialog(this, "Failed to save meter reading.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Calculate Bill
            SlabCalculatorService.BillBreakdown bd = slabCalculatorService.calculateBill(units, selectedCustomer.getCategory());

            Bill bill = new Bill();
            bill.setCustomerId(selectedCustomer.getCustomerId());
            bill.setReadingId(mr.getReadingId());
            bill.setBillDate(rDate);
            bill.setDueDate(Date.valueOf(rDate.toLocalDate().plusDays(18)));
            bill.setUnitsConsumed(units);
            bill.setEnergyCharge(bd.getEnergyCharge());
            bill.setFixedCharge(bd.getFixedCharge());
            bill.setTaxAmount(bd.getTaxAmount());
            bill.setSubsidyAmount(bd.getSubsidyAmount());
            bill.setTotalAmount(bd.getTotalPayable());
            bill.setPaymentStatus("UNPAID");

            billDAO.addBill(bill);

            // Run AI Anomaly Analysis
            aiConsumptionService.analyzeCustomerConsumption(selectedCustomer.getCustomerId(), units);

            JOptionPane.showMessageDialog(this, "Meter Reading recorded & Bill INV-IN-" + (bill.getBillId() + 100000) + " generated successfully!\nAI Consumption Audit updated.", "Success", JOptionPane.INFORMATION_MESSAGE);

            onCustomerSelected();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid inputs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
