package org.jsheet;

import org.jsheet.model.ExprWrapper;
import org.jsheet.model.JSheetTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static java.awt.event.ActionEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.*;

@SuppressWarnings("MagicConstant")
public class JSheet extends JPanel implements ActionListener {
    public JSheet() {
        super(new GridLayout(1, 0));

        JSheetTableModel model = new JSheetTableModel();
        final JTable table = new JSheetTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(1500, 800));
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);

        table.setDefaultRenderer(ExprWrapper.class, new ExpressionRenderer(model));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("JSheet");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JSheet newContentPane = new JSheet();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.setJMenuBar(createMenu(newContentPane));

        frame.pack();
        frame.setVisible(true);
    }

    private static JMenuBar createMenu(ActionListener listener) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getJMenu(
            listener, "File", KeyEvent.VK_F,
            new String[] {"New", "Open", "Save", "Close"},
            new int[] {VK_N, VK_O, VK_S, VK_C},
            new KeyStroke[] {
                KeyStroke.getKeyStroke(VK_N, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_O, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_S, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_Q, CTRL_MASK),
            }
        ));
        menuBar.add(getJMenu(
            listener, "Edit", KeyEvent.VK_E,
            new String[] {"Cut", "Copy", "Paste"},
            new int[] {VK_T, VK_C, VK_P},
            new KeyStroke[] {
                KeyStroke.getKeyStroke(VK_X, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_C, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_P, CTRL_MASK)
            }
        ));
        menuBar.add(getJMenu(
            listener, "Help", VK_H,
            new String[] {"About"},
            new int[] {VK_A},
            new KeyStroke[] { null }
        ));
        return menuBar;
    }

    private static JMenu getJMenu(
        ActionListener listener,
        String name, int mnemonic,
        String[] names,
        int[] mnemonics,
        KeyStroke[] accelerators)
    {
        if (names.length != mnemonics.length || mnemonics.length != accelerators.length) {
            throw new IllegalArgumentException();
        }
        JMenu menu = new JMenu(name);
        menu.setMnemonic(mnemonic);
        for (int i = 0; i < names.length; i++) {
            JMenuItem item = new JMenuItem(names[i], mnemonics[i]);
            if (accelerators[i] != null)
                item.setAccelerator(accelerators[i]);
            item.addActionListener(listener);
            menu.add(item);
        }
        return menu;
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(JSheet::createAndShowGUI);
    }
}
