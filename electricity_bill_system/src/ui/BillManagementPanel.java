package ui;

import dao.BillDAO;
import dao.CustomerDAO;
import model.Bill;
import model.Customer;
import service.BillExportService;
import service.PDFExportService;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class BillManagementPanel extends JPanel {
    private final BillDAO billDAO = new BillDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BillExportService billExportService = new BillExportService();
    private final PDFExportService pdfExportService = new PDFExportService();

    private JTable billTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterCombo;

    public BillManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initUI();
        refreshBillTable();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Bills, Invoices & Payment Ledger");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("View generated electricity bills, process settlement payments, & render printable receipts");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Center Card Panel
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        // Filter & Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        toolbar.setOpaque(false);

        statusFilterCombo = new JComboBox<>(new String[]{"ALL BILLS", "UNPAID", "PAID"});
        statusFilterCombo.setFont(UITheme.FONT_BODY);
        statusFilterCombo.addActionListener(e -> filterBills());

        JButton viewInvoiceBtn = UITheme.createPrimaryButton("View & Export Invoice");
        JButton markPaidBtn = UITheme.createSecondaryButton("Record Payment (Mark Paid)");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        viewInvoiceBtn.addActionListener(e -> openInvoiceDialog());
        markPaidBtn.addActionListener(e -> recordPayment());
        refreshBtn.addActionListener(e -> refreshBillTable());

        toolbar.add(new JLabel("Filter Status: "));
        toolbar.add(statusFilterCombo);
        toolbar.add(viewInvoiceBtn);
        toolbar.add(markPaidBtn);
        toolbar.add(refreshBtn);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Bill ID", "Consumer No", "Customer Name", "DISCOM", "Units (kWh)", "Energy (₹)", "Fixed (₹)", "Duty (₹)", "Subsidy (₹)", "Total (₹)", "Due Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        billTable = new JTable(tableModel);
        billTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UITheme.styleTable(billTable);

        card.add(new JScrollPane(billTable), BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void refreshBillTable() {
        statusFilterCombo.setSelectedIndex(0);
        loadBills(billDAO.getAllBills());
    }

    private void filterBills() {
        String filter = (String) statusFilterCombo.getSelectedItem();
        List<Bill> all = billDAO.getAllBills();
        if ("UNPAID".equals(filter)) {
            all.removeIf(b -> "PAID".equalsIgnoreCase(b.getPaymentStatus()));
        } else if ("PAID".equals(filter)) {
            all.removeIf(b -> !"PAID".equalsIgnoreCase(b.getPaymentStatus()));
        }
        loadBills(all);
    }

    private void loadBills(List<Bill> bills) {
        tableModel.setRowCount(0);
        for (Bill b : bills) {
            tableModel.addRow(new Object[]{
                    b.getBillId(),
                    b.getConsumerNo(),
                    b.getCustomerName(),
                    b.getDiscomName(),
                    String.format("%.1f", b.getUnitsConsumed()),
                    String.format("₹ %.2f", b.getEnergyCharge()),
                    String.format("₹ %.2f", b.getFixedCharge()),
                    String.format("₹ %.2f", b.getTaxAmount()),
                    String.format("₹ %.2f", b.getSubsidyAmount()),
                    String.format("₹ %.2f", b.getTotalAmount()),
                    b.getDueDate(),
                    b.getPaymentStatus()
            });
        }
    }

    private void openInvoiceDialog() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill from the table to view the invoice.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int billId = (Integer) tableModel.getValueAt(row, 0);
        Bill bill = billDAO.getBillById(billId);
        if (bill == null) return;

        Customer customer = customerDAO.getCustomerById(bill.getCustomerId());
        if (customer == null) return;

        String htmlInvoice = billExportService.generateHTMLInvoice(bill, customer);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Electricity Bill Invoice - INV-IN-" + (bill.getBillId() + 100000), true);
        dlg.setSize(800, 850);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(htmlInvoice);
        editorPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        dlg.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton savePdfBtn = UITheme.createPrimaryButton("Save as PDF");
        JButton printBtn = UITheme.createSecondaryButton("Print Invoice");
        JButton closeBtn = UITheme.createSecondaryButton("Close");

        savePdfBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Bill as PDF");
            fileChooser.setSelectedFile(new File("Electricity_Bill_INV_IN_" + (bill.getBillId() + 100000) + ".pdf"));
            int userSelection = fileChooser.showSaveDialog(dlg);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                }
                boolean success = pdfExportService.exportBillToPDF(bill, customer, fileToSave);
                if (success) {
                    JOptionPane.showMessageDialog(dlg, "Electricity Bill successfully saved as PDF file:\n" + fileToSave.getAbsolutePath(), "PDF Exported", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dlg, "Failed to export PDF file. Please try again.", "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        printBtn.addActionListener(e -> {
            try {
                editorPane.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeBtn.addActionListener(e -> dlg.dispose());

        btnPanel.add(savePdfBtn);
        btnPanel.add(printBtn);
        btnPanel.add(closeBtn);

        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void recordPayment() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an unpaid bill to record payment.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int billId = (Integer) tableModel.getValueAt(row, 0);
        Bill bill = billDAO.getBillById(billId);
        if (bill == null) return;

        if ("PAID".equalsIgnoreCase(bill.getPaymentStatus())) {
            JOptionPane.showMessageDialog(this, "This bill is already marked as PAID.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Record full payment of ₹ " + String.format("%.2f", bill.getTotalAmount()) + " for Consumer " + bill.getConsumerNo() + "?", "Confirm Settlement", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = billDAO.markAsPaid(billId, Date.valueOf(LocalDate.now()));
            if (ok) {
                JOptionPane.showMessageDialog(this, "Payment recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshBillTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update payment status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
