package org.jsheet;

import org.jsheet.model.ExprWrapper;
import org.jsheet.model.JSheetTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

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

        frame.setJMenuBar(createMenu());

        frame.pack();
        frame.setVisible(true);
    }

    private static JMenuBar createMenu() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();

        // "File" menu
        {
            menu = new JMenu("File");
            menu.setMnemonic(KeyEvent.VK_F);
            menuBar.add(menu);

            menuItem = new JMenuItem("New", KeyEvent.VK_N);
            menu.add(menuItem);

            menuItem = new JMenuItem("Open", KeyEvent.VK_O);
            menu.add(menuItem);

            menuItem = new JMenuItem("Save", KeyEvent.VK_S);
            menu.add(menuItem);

            menuItem = new JMenuItem("Close", KeyEvent.VK_C);
            menu.add(menuItem);
        }

        // "Edit" menu
        {
            menu = new JMenu("Edit");
            menu.setMnemonic(KeyEvent.VK_E);
            menuBar.add(menu);

            menuItem = new JMenuItem("Cut", KeyEvent.VK_T);
            menu.add(menuItem);

            menuItem = new JMenuItem("Copy", KeyEvent.VK_C);
            menu.add(menuItem);

            menuItem = new JMenuItem("Paste", KeyEvent.VK_P);
            menu.add(menuItem);
        }

        // "Help" menu
        {
            menu = new JMenu("Help");
            menu.setMnemonic(KeyEvent.VK_H);
            menuBar.add(menu);

            menuItem = new JMenuItem("About", KeyEvent.VK_A);
            menu.add(menuItem);
        }

        return menuBar;
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(JSheet::createAndShowGUI);
    }
}
