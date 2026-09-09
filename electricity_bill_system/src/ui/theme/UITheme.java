package ui.theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UITheme {
    // Professional Dark & Light Palette
    public static final Color NAV_DARK = new Color(15, 23, 42);       // #0F172A slate-900
    public static final Color NAV_HOVER = new Color(30, 41, 59);     // #1E293B slate-800
    public static final Color PRIMARY = new Color(14, 165, 233);      // #0EA5E9 sky-500
    public static final Color PRIMARY_DARK = new Color(3, 105, 161);  // #0369A1 sky-700
    public static final Color BG_LIGHT = new Color(248, 250, 252);    // #F8FAFC slate-50
    public static final Color CARD_BG = new Color(255, 255, 255);     // White card
    public static final Color TEXT_DARK = new Color(15, 23, 42);      // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate 500
    public static final Color BORDER_COLOR = new Color(226, 232, 240);// Slate 200

    public static final Color COLOR_SUCCESS = new Color(16, 185, 129); // Emerald 500
    public static final Color COLOR_WARNING = new Color(245, 158, 11); // Amber 500
    public static final Color COLOR_DANGER = new Color(239, 68, 68);   // Red 500

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_CARD_VAL = new Font("SansSerif", Font.BOLD, 22);

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_HEADER);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY_DARK);
        btn.setUI(new BasicButtonUI()); // Ensures custom color & crisp text rendering across all OS L&Fs
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_DARK.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_DARK);
            }
        });
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_HEADER);
        btn.setForeground(TEXT_DARK);
        btn.setBackground(new Color(241, 245, 249)); // Light slate
        btn.setUI(new BasicButtonUI());
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(226, 232, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(241, 245, 249));
            }
        });
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    public static JPanel createMetricCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(5, 5));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(FONT_CARD_VAL);
        valLbl.setForeground(accentColor != null ? accentColor : TEXT_DARK);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(224, 242, 254)); // Sky 100
        table.setSelectionForeground(TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(new Color(241, 245, 249)); // Slate 100
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    }
}
