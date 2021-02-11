package org.jsheet;

import org.jsheet.model.ExprWrapper;
import org.jsheet.model.JSheetTableModel;

import javax.swing.*;
import java.awt.*;

public class JSheet extends JPanel {
    public JSheet() {
        super(new GridLayout(1, 0));

        JSheetTableModel model = new JSheetTableModel();
        final JTable table = new JSheetTable(model);
        //table.setPreferredScrollableViewportSize(new Dimension(500, 500));
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);

        table.setDefaultRenderer(ExprWrapper.class, new ExpressionRenderer(model));
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
