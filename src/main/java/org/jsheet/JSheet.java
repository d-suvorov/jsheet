package org.jsheet;

import org.jsheet.expr.Expr;

import javax.swing.*;
import java.awt.*;

public class JSheet extends JPanel {
    public JSheet() {
        super(new GridLayout(1, 0));

        final JTable table = new JSheetTable(new JSheetTableModel());
        //table.setPreferredScrollableViewportSize(new Dimension(500, 500));
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);

        table.setDefaultRenderer(Expr.class, new ExpressionRenderer());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("JSheet");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JSheet newContentPane = new JSheet();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(JSheet::createAndShowGUI);
    }
}
