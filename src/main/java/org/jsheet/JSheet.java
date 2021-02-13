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
public class JSheet extends JFrame implements ActionListener {
    public static final String NAME = "jsheet";
    public static final String VERSION = "1.0-SNAPSHOT";
    public static final String ABOUT_DIALOG_TITLE = "About";
    public static final String ABOUT_DIALOG_TEXT = NAME + " is a neat spreadsheet editor\n" +
        "Version: " + VERSION;

    private final ActionListener aboutActionListener = event -> JOptionPane.showMessageDialog(
        this,
        ABOUT_DIALOG_TEXT,
        ABOUT_DIALOG_TITLE,
        JOptionPane.INFORMATION_MESSAGE
    );

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e);
    }

    private static class JSheetPanel extends JPanel {
        public JSheetPanel() {
            super(new GridLayout(1, 0));

            JSheetTableModel model = new JSheetTableModel();
            final JTable table = new JSheetTable(model);
            table.setPreferredScrollableViewportSize(new Dimension(1500, 800));
            table.setFillsViewportHeight(true);

            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane);

            table.setDefaultRenderer(ExprWrapper.class, new ExpressionRenderer(model));
        }
    }

    public JSheet(String title) {
        super(title);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JSheetPanel newContentPane = new JSheetPanel();
        newContentPane.setOpaque(true);
        setContentPane(newContentPane);

        setJMenuBar(createMenu());
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getJMenu(
            "File", KeyEvent.VK_F,
            new String[] {"New", "Open", "Save", "Close"},
            new int[] {VK_N, VK_O, VK_S, VK_C},
            new KeyStroke[] {
                KeyStroke.getKeyStroke(VK_N, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_O, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_S, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_Q, CTRL_MASK),
            },
            new ActionListener[] {this, this, this, this}
        ));
        menuBar.add(getJMenu(
            "Edit", KeyEvent.VK_E,
            new String[] {"Cut", "Copy", "Paste"},
            new int[] {VK_T, VK_C, VK_P},
            new KeyStroke[] {
                KeyStroke.getKeyStroke(VK_X, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_C, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_P, CTRL_MASK)
            },
            new ActionListener[] {this, this, this}
        ));
        menuBar.add(getJMenu(
            "Help", VK_H,
            new String[] {"About"},
            new int[] {VK_A},
            new KeyStroke[] { null },
            new ActionListener[] {aboutActionListener}
        ));
        return menuBar;
    }

    private static JMenu getJMenu(
        String name, int mnemonic,
        String[] names,
        int[] mnemonics,
        KeyStroke[] accelerators,
        ActionListener[] listeners
    )
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
            item.addActionListener(listeners[i]);
            menu.add(item);
        }
        return menu;
    }

    private static void createAndShowGUI() {
        JFrame frame = new JSheet(NAME);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(JSheet::createAndShowGUI);
    }
}
