package ui;

import dao.SlabRateDAO;
import model.SlabRate;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TariffConfigPanel extends JPanel {
    private final SlabRateDAO slabRateDAO = new SlabRateDAO();

    private JTable slabTable;
    private DefaultTableModel tableModel;

    private JComboBox<String> categoryCombo;
    private JTextField minUnitsTxt;
    private JTextField maxUnitsTxt;
    private JTextField rateTxt;
    private JTextField fixedChargeTxt;
    private JTextField taxPercentTxt;
    private JTextField subsidyTxt;

    private int selectedSlabId = -1;

    public TariffConfigPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initUI();
        refreshSlabTable();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Slab Tariff Configuration Engine");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Configure Indian State Tariff Tiers, Fixed Charges (₹), Electricity Duty Taxes (%), & Subsidy Rules");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Split Pane (Left Table, Right Slab Form)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setContinuousLayout(true);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Left Table Card
        JPanel leftCard = UITheme.createCardPanel();
        leftCard.setLayout(new BorderLayout(10, 10));

        JLabel tableTitle = new JLabel("Active Category Tariff Slabs");
        tableTitle.setFont(UITheme.FONT_SUBTITLE);
        leftCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Category", "Slab Range (kWh)", "Rate (₹/Unit)", "Fixed Charge (₹)", "Tax (%)", "Subsidy (₹)"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        slabTable = new JTable(tableModel);
        slabTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UITheme.styleTable(slabTable);

        slabTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedSlabForm();
            }
        });

        leftCard.add(new JScrollPane(slabTable), BorderLayout.CENTER);

        splitPane.setLeftComponent(leftCard);

        // Right Form Card
        JPanel rightCard = UITheme.createCardPanel();
        rightCard.setLayout(new BorderLayout(10, 10));

        JLabel formTitle = new JLabel("Slab Rule Editor");
        formTitle.setFont(UITheme.FONT_SUBTITLE);
        rightCard.add(formTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        categoryCombo = new JComboBox<>(new String[]{"Residential", "Commercial", "Industrial"});
        categoryCombo.setFont(UITheme.FONT_BODY);

        minUnitsTxt = new JTextField("0");
        maxUnitsTxt = new JTextField("100");
        rateTxt = new JTextField("1.50");
        fixedChargeTxt = new JTextField("85.00");
        taxPercentTxt = new JTextField("9.00");
        subsidyTxt = new JTextField("100.00");

        int r = 0;
        addFormField(formGrid, gbc, "Category Tier:*", categoryCombo, r++);
        addFormField(formGrid, gbc, "Min Units (kWh):*", minUnitsTxt, r++);
        addFormField(formGrid, gbc, "Max Units (kWh):*", maxUnitsTxt, r++);
        addFormField(formGrid, gbc, "Rate per Unit (₹):*", rateTxt, r++);
        addFormField(formGrid, gbc, "Fixed Base Charge (₹):", fixedChargeTxt, r++);
        addFormField(formGrid, gbc, "Electricity Duty Tax (%):", taxPercentTxt, r++);
        addFormField(formGrid, gbc, "Govt Subsidy Amount (₹):", subsidyTxt, r++);

        rightCard.add(new JScrollPane(formGrid), BorderLayout.CENTER);

        // Action Buttons
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        btnBox.setOpaque(false);

        JButton clearBtn = UITheme.createSecondaryButton("New Slab");
        JButton saveBtn = UITheme.createPrimaryButton("Save Tariff Slab");
        JButton delBtn = UITheme.createSecondaryButton("Delete");

        clearBtn.addActionListener(e -> clearForm());
        saveBtn.addActionListener(e -> saveSlabRate());
        delBtn.addActionListener(e -> deleteSlabRate());

        btnBox.add(clearBtn);
        btnBox.add(saveBtn);
        btnBox.add(delBtn);

        rightCard.add(btnBox, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightCard);

        add(splitPane, BorderLayout.CENTER);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, Component comp, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        panel.add(comp, gbc);
    }

    public void refreshSlabTable() {
        tableModel.setRowCount(0);
        List<SlabRate> list = slabRateDAO.getAllSlabRates();
        for (SlabRate s : list) {
            tableModel.addRow(new Object[]{
                    s.getSlabId(),
                    s.getCategory(),
                    s.getSlabRangeDisplay(),
                    String.format("₹ %.2f", s.getRatePerUnit()),
                    String.format("₹ %.2f", s.getFixedCharge()),
                    String.format("%.1f%%", s.getTaxPercent()),
                    String.format("₹ %.2f", s.getSubsidyAmount())
            });
        }
    }

    private void loadSelectedSlabForm() {
        int row = slabTable.getSelectedRow();
        if (row >= 0) {
            int id = (Integer) tableModel.getValueAt(row, 0);
            List<SlabRate> list = slabRateDAO.getAllSlabRates();
            for (SlabRate s : list) {
                if (s.getSlabId() == id) {
                    selectedSlabId = s.getSlabId();
                    categoryCombo.setSelectedItem(s.getCategory());
                    minUnitsTxt.setText(String.valueOf(s.getMinUnits()));
                    maxUnitsTxt.setText(String.valueOf(s.getMaxUnits()));
                    rateTxt.setText(String.valueOf(s.getRatePerUnit()));
                    fixedChargeTxt.setText(String.valueOf(s.getFixedCharge()));
                    taxPercentTxt.setText(String.valueOf(s.getTaxPercent()));
                    subsidyTxt.setText(String.valueOf(s.getSubsidyAmount()));
                    break;
                }
            }
        }
    }

    private void clearForm() {
        selectedSlabId = -1;
        slabTable.clearSelection();
        categoryCombo.setSelectedIndex(0);
        minUnitsTxt.setText("0");
        maxUnitsTxt.setText("100");
        rateTxt.setText("1.50");
        fixedChargeTxt.setText("85.00");
        taxPercentTxt.setText("9.00");
        subsidyTxt.setText("0.00");
    }

    private void saveSlabRate() {
        try {
            SlabRate s = new SlabRate();
            s.setSlabId(selectedSlabId);
            s.setCategory((String) categoryCombo.getSelectedItem());
            s.setMinUnits(Integer.parseInt(minUnitsTxt.getText().trim()));
            s.setMaxUnits(Integer.parseInt(maxUnitsTxt.getText().trim()));
            s.setRatePerUnit(Double.parseDouble(rateTxt.getText().trim()));
            s.setFixedCharge(Double.parseDouble(fixedChargeTxt.getText().trim()));
            s.setTaxPercent(Double.parseDouble(taxPercentTxt.getText().trim()));
            s.setSubsidyAmount(Double.parseDouble(subsidyTxt.getText().trim()));

            boolean ok;
            if (selectedSlabId > 0) {
                ok = slabRateDAO.updateSlabRate(s);
            } else {
                ok = slabRateDAO.addSlabRate(s);
            }

            if (ok) {
                JOptionPane.showMessageDialog(this, "Tariff Slab saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshSlabTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save tariff slab.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSlabRate() {
        if (selectedSlabId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a tariff slab to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this tariff slab rule?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = slabRateDAO.deleteSlabRate(selectedSlabId);
            if (ok) {
                refreshSlabTable();
                clearForm();
            }
        }
    }
}
