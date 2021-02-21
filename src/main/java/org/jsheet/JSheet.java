package org.jsheet;

import com.opencsv.exceptions.CsvValidationException;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Value;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

import static java.awt.event.ActionEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.*;
import static javax.swing.KeyStroke.getKeyStroke;

@SuppressWarnings("MagicConstant")
public class JSheet extends JFrame {
    public static final String NAME = "jsheet";
    public static final String VERSION = "1.0-SNAPSHOT";

    public static final String ABOUT_DIALOG_TITLE = "About";
    public static final String ABOUT_DIALOG_TEXT = NAME + " is a neat spreadsheet editor\n"
        + "Version: " + VERSION;

    public static final String SAVE_CHANGES_DIALOG_TITLE = "Save changes?";
    public static final String SAVE_CHANGES_DIALOG_TEXT
        = "Your changes will be lost if you don't save them";

    public static final String ERROR_MESSAGE_TITLE = "Error";

    private JSheetTableModel model;
    private JTable table;

    private File currentDirectory = Paths.get("").toFile();
    private File currentFile = null;

    // File menu

    private final ActionListener newActionListener = event -> {
        if (saveChanged()) return;
        currentFile = null;
        model = new JSheetTableModel();
        table.setModel(model);
    };

    private final ActionListener openActionListener = event -> {
        if (saveChanged()) return;
        File file = askForFile();
        if (file == null)
            return;
        try {
            model = JSheetTableModel.read(file);
            table.setModel(model);
            updateCurrentFile(file);
        } catch (IOException | CsvValidationException e) {
            JOptionPane.showMessageDialog(this,
                String.format("Cannot read %s: %s", file.getName(), e.getMessage()),
                ERROR_MESSAGE_TITLE,
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    };

    private final ActionListener saveActionListener = event -> save();

    private final ActionListener saveAsActionListener = event -> {
        File file = askForFile();
        saveTo(file);
    };

    private final ActionListener quitActionListener = event -> quit();

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

    private boolean save() {
        File file = currentFile != null ? currentFile : askForFile();
        return saveTo(file);
    }

    private boolean saveTo(File file) {
        if (file == null)
            return true;
        try {
            JSheetTableModel.write(file, model);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                String.format("Cannot write %s: %s", file.getName(), e.getMessage()),
                ERROR_MESSAGE_TITLE,
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return true;
        }
        updateCurrentFile(file);
        model.setModified(false);
        return false;
    }

    private File askForFile() {
        JFileChooser chooser = new JFileChooser(currentDirectory);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return chooser.getSelectedFile();
    }

    private void updateCurrentFile(File file) {
        if (!Objects.equals(currentFile, file)) {
            currentFile = file;
            currentDirectory = currentFile.getParentFile();
        }
    }

    /**
     * If the current sheet has changed, ask user whether they want
     * to save it before closing and save if necessary.
     * @return {@code true} if the user wants to abort current action or {@code false} otherwise.
     */
    private boolean saveChanged() {
        if (!model.isModified())
            return false;
        int option = JOptionPane.showConfirmDialog(this,
            SAVE_CHANGES_DIALOG_TEXT,
            SAVE_CHANGES_DIALOG_TITLE,
            JOptionPane.YES_NO_CANCEL_OPTION);
        switch (option) {
            case JOptionPane.YES_OPTION:
                return save();
            case JOptionPane.NO_OPTION:
                return false;
            case JOptionPane.CANCEL_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return true;
            default:
                throw new AssertionError();
        }
    }

    private void quit() {
        if (saveChanged()) return;
        System.exit(0);
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

            table.setDefaultRenderer(Value.class, new ExpressionRenderer());
        }
    }

    public JSheet(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                quit();
            }
        });
        JSheetPanel newContentPane = new JSheetPanel();
        newContentPane.setOpaque(true);
        setContentPane(newContentPane);
        setJMenuBar(createMenu());
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getJMenu(
            "File", KeyEvent.VK_F,
            new String[] { "New", "Open", "Save", "Save As", "Quit" },
            new int[] { VK_N, VK_O, VK_S, VK_A, VK_Q },
            new KeyStroke[] {
                getKeyStroke(VK_N, CTRL_MASK),
                getKeyStroke(VK_O, CTRL_MASK),
                getKeyStroke(VK_S, CTRL_MASK),
                null,
                getKeyStroke(VK_Q, CTRL_MASK),
            },
            new ActionListener[] {
                newActionListener,
                openActionListener,
                saveActionListener,
                saveAsActionListener,
                quitActionListener
            }
        ));
        menuBar.add(getJMenu(
            "Edit", KeyEvent.VK_E,
            new String[] { "Cut", "Copy", "Paste" },
            new int[] { VK_T, VK_C, VK_P },
            new KeyStroke[] {
                getKeyStroke(VK_X, CTRL_MASK),
                getKeyStroke(VK_C, CTRL_MASK),
                getKeyStroke(VK_P, CTRL_MASK)
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
