package org.jsheet;

import com.opencsv.exceptions.CsvValidationException;
import org.jsheet.model.ExprWrapper;
import org.jsheet.model.JSheetTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import static java.awt.event.ActionEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.*;

@SuppressWarnings("MagicConstant")
public class JSheet extends JFrame {
    public static final String NAME = "jsheet";
    public static final String VERSION = "1.0-SNAPSHOT";
    public static final String ABOUT_DIALOG_TITLE = "About";
    public static final String ABOUT_DIALOG_TEXT = NAME + " is a neat spreadsheet editor\n" +
        "Version: " + VERSION;

    private JSheetTableModel model;
    private JTable table;

    // File menu
    private final ActionListener newActionListener = event -> {
        throw new AssertionError("unimplemented");
    };

    private final ActionListener openActionListener = event -> {
        open();
    };

    private final ActionListener saveActionListener = event -> {
        save();
    };

    private final ActionListener closeActionListener = event -> {
        throw new AssertionError("unimplemented");
    };

    // Edit menu
    private final ActionListener cutActionListener = event -> {
        throw new AssertionError("unimplemented");
    };

    private final ActionListener copyActionListener = event -> {
        throw new AssertionError("unimplemented");
    };

    private final ActionListener pasteActionListener = event -> {
        throw new AssertionError("unimplemented");
    };

    // About menu
    private final ActionListener aboutActionListener = event -> JOptionPane.showMessageDialog(
        this,
        ABOUT_DIALOG_TEXT,
        ABOUT_DIALOG_TITLE,
        JOptionPane.INFORMATION_MESSAGE
    );

    private void save() {
        File file = new File("test");
        try {
            JSheetTableModel.write(file, model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void open() {
        File file = new File("test");
        JSheetTableModel newModel = null;
        try {
            newModel = JSheetTableModel.read(file);
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        table.setModel(newModel);
    }

    private class JSheetPanel extends JPanel {
        public JSheetPanel() {
            super(new GridLayout(1, 0));

            model = new JSheetTableModel();
            table = new JSheetTable(model);
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
            new String[] { "New", "Open", "Save", "Close" },
            new int[] { VK_N, VK_O, VK_S, VK_C },
            new KeyStroke[] {
                KeyStroke.getKeyStroke(VK_N, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_O, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_S, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_Q, CTRL_MASK),
            },
            new ActionListener[] {
                newActionListener,
                openActionListener,
                saveActionListener,
                closeActionListener
            }
        ));
        menuBar.add(getJMenu(
            "Edit", KeyEvent.VK_E,
            new String[] { "Cut", "Copy", "Paste" },
            new int[] { VK_T, VK_C, VK_P },
            new KeyStroke[] {
                KeyStroke.getKeyStroke(VK_X, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_C, CTRL_MASK),
                KeyStroke.getKeyStroke(VK_P, CTRL_MASK)
            },
            new ActionListener[] {
                cutActionListener,
                copyActionListener,
                pasteActionListener
            }
        ));
        menuBar.add(getJMenu(
            "Help", VK_H,
            new String[] { "About" },
            new int[] { VK_A },
            new KeyStroke[] { null },
            new ActionListener[] { aboutActionListener }
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
