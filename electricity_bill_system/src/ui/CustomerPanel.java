package ui;

import dao.CustomerDAO;
import model.Customer;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class CustomerPanel extends JPanel {
    private final CustomerDAO customerDAO = new CustomerDAO();

    private JTable customerTable;
    private DefaultTableModel tableModel;

    private JTextField searchTxt;
    private JTextField consumerNoTxt;
    private JTextField nameTxt;
    private JTextField emailTxt;
    private JTextField phoneTxt;
    private JTextArea addressTxt;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> discomCombo;
    private JTextField meterNoTxt;
    private JTextField connDateTxt;

    private int selectedCustomerId = -1;

    public CustomerPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initUI();
        refreshCustomerTable();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Consumer & Meter Management");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Main Split Pane (Left: Table + Search, Right: Add/Edit Form)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setContinuousLayout(true);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Left Panel (Table)
        JPanel leftCard = UITheme.createCardPanel();
        leftCard.setLayout(new BorderLayout(10, 10));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchTxt = new JTextField();
        searchTxt.setFont(UITheme.FONT_BODY);
        JButton searchBtn = UITheme.createSecondaryButton("Search");
        searchBtn.addActionListener(e -> filterCustomers());
        searchPanel.add(new JLabel("Search Consumer: "), BorderLayout.WEST);
        searchPanel.add(searchTxt, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        leftCard.add(searchPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Consumer No", "Name", "Category", "DISCOM", "Meter No", "Phone"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        customerTable = new JTable(tableModel);
        customerTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UITheme.styleTable(customerTable);

        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedCustomerForm();
            }
        });

        leftCard.add(new JScrollPane(customerTable), BorderLayout.CENTER);

        splitPane.setLeftComponent(leftCard);

        // Right Panel (Form)
        JPanel rightCard = UITheme.createCardPanel();
        rightCard.setLayout(new BorderLayout(10, 10));

        JLabel formTitle = new JLabel("Consumer Details Form");
        formTitle.setFont(UITheme.FONT_SUBTITLE);
        rightCard.add(formTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        consumerNoTxt = new JTextField();
        consumerNoTxt.setEditable(false);
        nameTxt = new JTextField();
        emailTxt = new JTextField();
        phoneTxt = new JTextField();
        addressTxt = new JTextArea(3, 15);
        addressTxt.setLineWrap(true);
        categoryCombo = new JComboBox<>(new String[]{"Residential", "Commercial", "Industrial"});
        discomCombo = new JComboBox<>(new String[]{"MSEDCL", "BESCOM", "TATA Power", "TPDDL", "CESC", "TNEB", "UPVCL"});
        meterNoTxt = new JTextField();
        connDateTxt = new JTextField("2026-01-01");

        addFormField(formGrid, gbc, "Consumer Account No:", consumerNoTxt, 0);
        addFormField(formGrid, gbc, "Full Name / Organization:*", nameTxt, 1);
        addFormField(formGrid, gbc, "Tariff Category:*", categoryCombo, 2);
        addFormField(formGrid, gbc, "DISCOM Provider:*", discomCombo, 3);
        addFormField(formGrid, gbc, "Meter Serial No:*", meterNoTxt, 4);
        addFormField(formGrid, gbc, "Phone (+91):", phoneTxt, 5);
        addFormField(formGrid, gbc, "Email Address:", emailTxt, 6);

        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.3;
        formGrid.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formGrid.add(new JScrollPane(addressTxt), gbc);

        addFormField(formGrid, gbc, "Connection Date (YYYY-MM-DD):", connDateTxt, 8);

        rightCard.add(new JScrollPane(formGrid), BorderLayout.CENTER);

        // Form Action Buttons
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        btnBox.setOpaque(false);

        JButton newBtn = UITheme.createSecondaryButton("Clear / New");
        JButton saveBtn = UITheme.createPrimaryButton("Save Consumer");
        JButton deleteBtn = UITheme.createSecondaryButton("Delete");

        newBtn.addActionListener(e -> clearForm());
        saveBtn.addActionListener(e -> saveCustomer());
        deleteBtn.addActionListener(e -> deleteCustomer());

        btnBox.add(newBtn);
        btnBox.add(saveBtn);
        btnBox.add(deleteBtn);

        rightCard.add(btnBox, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightCard);

        add(splitPane, BorderLayout.CENTER);

        clearForm();
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, Component comp, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    public void refreshCustomerTable() {
        tableModel.setRowCount(0);
        List<Customer> list = customerDAO.getAllCustomers();
        for (Customer c : list) {
            tableModel.addRow(new Object[]{
                    c.getCustomerId(),
                    c.getConsumerNo(),
                    c.getName(),
                    c.getCategory(),
                    c.getDiscomName(),
                    c.getMeterNo(),
                    c.getPhone()
            });
        }
    }

    private void filterCustomers() {
        String q = searchTxt.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        List<Customer> list = customerDAO.getAllCustomers();
        for (Customer c : list) {
            if (q.isEmpty() || c.getName().toLowerCase().contains(q) || c.getConsumerNo().toLowerCase().contains(q) || c.getMeterNo().toLowerCase().contains(q)) {
                tableModel.addRow(new Object[]{
                        c.getCustomerId(),
                        c.getConsumerNo(),
                        c.getName(),
                        c.getCategory(),
                        c.getDiscomName(),
                        c.getMeterNo(),
                        c.getPhone()
                });
            }
        }
    }

    private void loadSelectedCustomerForm() {
        int row = customerTable.getSelectedRow();
        if (row >= 0) {
            int cid = (Integer) tableModel.getValueAt(row, 0);
            Customer c = customerDAO.getCustomerById(cid);
            if (c != null) {
                selectedCustomerId = c.getCustomerId();
                consumerNoTxt.setText(c.getConsumerNo());
                nameTxt.setText(c.getName());
                emailTxt.setText(c.getEmail());
                phoneTxt.setText(c.getPhone());
                addressTxt.setText(c.getAddress());
                categoryCombo.setSelectedItem(c.getCategory());
                discomCombo.setSelectedItem(c.getDiscomName());
                meterNoTxt.setText(c.getMeterNo());
                connDateTxt.setText(c.getConnectionDate() != null ? c.getConnectionDate().toString() : "");
            }
        }
    }

    private void clearForm() {
        selectedCustomerId = -1;
        customerTable.clearSelection();
        consumerNoTxt.setText(customerDAO.generateNextConsumerNo());
        nameTxt.setText("");
        emailTxt.setText("");
        phoneTxt.setText("");
        addressTxt.setText("");
        categoryCombo.setSelectedIndex(0);
        discomCombo.setSelectedIndex(0);
        meterNoTxt.setText("MTR-" + (int)(Math.random() * 90000 + 10000));
        connDateTxt.setText(new Date(System.currentTimeMillis()).toString());
    }

    private void saveCustomer() {
        String name = nameTxt.getText().trim();
        String meter = meterNoTxt.getText().trim();
        if (name.isEmpty() || meter.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Meter Serial Number are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Customer c = new Customer();
        c.setCustomerId(selectedCustomerId);
        c.setConsumerNo(consumerNoTxt.getText());
        c.setName(name);
        c.setEmail(emailTxt.getText().trim());
        c.setPhone(phoneTxt.getText().trim());
        c.setAddress(addressTxt.getText().trim());
        c.setCategory((String) categoryCombo.getSelectedItem());
        c.setDiscomName((String) discomCombo.getSelectedItem());
        c.setMeterNo(meter);

        try {
            c.setConnectionDate(Date.valueOf(connDateTxt.getText().trim()));
        } catch (Exception e) {
            c.setConnectionDate(new Date(System.currentTimeMillis()));
        }

        boolean ok;
        if (selectedCustomerId > 0) {
            ok = customerDAO.updateCustomer(c);
        } else {
            ok = customerDAO.addCustomer(c);
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "Customer saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshCustomerTable();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save customer. Meter or Consumer No may be duplicate.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer() {
        if (selectedCustomerId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a consumer to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete consumer " + consumerNoTxt.getText() + "? All associated readings & bills will be removed.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            boolean ok = customerDAO.deleteCustomer(selectedCustomerId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Customer deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshCustomerTable();
                clearForm();
            }
        }
    }
}
