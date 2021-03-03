package org.jsheet;

import com.opencsv.exceptions.CsvValidationException;
import org.jsheet.data.JSheetTableModel;
import org.jsheet.data.Value;
import org.jsheet.parser.ParseException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
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
    private JSheetTable table;

    private final JFileChooser chooser = new JFileChooser();
    private File currentFile = null;

    private final DimensionDialog dimensionDialog = new DimensionDialog(this);
    {
        dimensionDialog.pack();
    }

    // File menu

    private final ActionListener newActionListener = event -> {
        if (saveChanged()) return;

        dimensionDialog.setLocationRelativeTo(this);
        dimensionDialog.setVisible(true);
        if (!dimensionDialog.isValidDimension())
            return;

        int rowCount = dimensionDialog.getRowCount();
        int columnCount = dimensionDialog.getColumnCount();
        model = new JSheetTableModel(rowCount, columnCount);
        currentFile = null;
        table.setModel(model);
    };

    private final ActionListener openActionListener = event -> {
        if (saveChanged()) return;
        File file = askForOpenFile();
        if (file == null)
            return;
        try {
            model = JSheetTableModel.read(file);
            table.setModel(model);
            updateCurrentFile(file);
        } catch (IOException | CsvValidationException | ParseException e) {
            JOptionPane.showMessageDialog(this,
                String.format("Cannot read %s: %s", file.getName(), e.getMessage()),
                ERROR_MESSAGE_TITLE,
                JOptionPane.ERROR_MESSAGE);
        }
    };

    private final ActionListener saveActionListener = event -> save();

    private final ActionListener saveAsActionListener = event -> {
        File file = askForSaveFile();
        saveTo(file);
    };

    private final ActionListener quitActionListener = event -> quit();

    // Edit menu

    private final ActionListener cutActionListener = event -> table.cut();

    private final ActionListener copyActionListener = event -> table.copy();

    private final ActionListener pasteActionListener = event -> table.paste();

    private final ActionListener deleteActionListener = event -> table.delete();

    // About menu

    private final ActionListener aboutActionListener = event -> JOptionPane.showMessageDialog(
        this,
        ABOUT_DIALOG_TEXT,
        ABOUT_DIALOG_TITLE,
        JOptionPane.INFORMATION_MESSAGE
    );

    private boolean save() {
        File file = currentFile != null ? currentFile : askForSaveFile();
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
            return true;
        }
        updateCurrentFile(file);
        model.setModified(false);
        return false;
    }

    private File askForOpenFile() {
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return null;
        return chooser.getSelectedFile();
    }

    private File askForSaveFile() {
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return null;
        return chooser.getSelectedFile();
    }

    private void updateCurrentFile(File file) {
        if (!Objects.equals(currentFile, file)) {
            currentFile = file;
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
            table.setDefaultEditor(Object.class, new JSheetEditor(JSheet.this));
            table.setDefaultRenderer(Value.class, new ExpressionRenderer());
            table.setPreferredScrollableViewportSize(new Dimension(1500, 800));
            table.setFillsViewportHeight(false);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            JTable rowHeader = new JTable(new AbstractTableModel() {
                @Override
                public int getRowCount() {
                    return model.getRowCount();
                }

                @Override
                public int getColumnCount() {
                    return 1;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    return rowIndex;
                }
            });
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column)
                {
                    setBackground(table.getTableHeader().getBackground());
                    setFont(table.getTableHeader().getFont());
                    setHorizontalAlignment(CENTER);
                    setText(value.toString());
                    return this;
                }
            };
            rowHeader.getColumnModel().getColumn(0).setCellRenderer(renderer);
            rowHeader.getColumnModel().getColumn(0).setPreferredWidth(30);
            rowHeader.setPreferredScrollableViewportSize(rowHeader.getPreferredSize());

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setViewportView(table);
            scrollPane.setRowHeaderView(rowHeader);

            add(scrollPane);
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
            new String[] { "Cut", "Copy", "Paste", "Delete" },
            new int[] { VK_T, VK_C, VK_P, VK_D },
            new KeyStroke[] { null, null, null, null },
            new ActionListener[] {
                cutActionListener,
                copyActionListener,
                pasteActionListener,
                deleteActionListener
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
        ActionListener[] listeners)
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
