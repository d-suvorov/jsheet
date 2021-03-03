package org.jsheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.jsheet.data.JSheetTableModel.*;

class DimensionDialog extends JDialog implements ActionListener, PropertyChangeListener {
    private static final String ENTER_STRING = "Enter";
    private static final String CANCEL_STRING = "Cancel";

    private final JOptionPane optionPane;
    private final JTextField textField;

    private boolean isValidDimension;
    private int rowCount;
    private int columnCount;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidDimension() {
        return isValidDimension;
    }

    public int getRowCount() {
        if (!isValidDimension())
            throw new IllegalStateException();
        return rowCount;
    }

    public int getColumnCount() {
        if (!isValidDimension())
            throw new IllegalStateException();
        return columnCount;
    }

    private boolean validateInput() {
        String input = textField.getText();
        String[] split = input.split(",");
        if (split.length != 2)
            return false;
        try {
            rowCount = Integer.parseInt(split[0].trim());
            columnCount = Integer.parseInt(split[1].trim());
            if (rowCount < MIN_ROW_COUNT || rowCount > MAX_ROW_COUNT)
                return false;
            if (columnCount < MIN_COLUMN_COUNT || columnCount > MAX_COLUMN_COUNT)
                return false;
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public DimensionDialog(Frame aFrame) {
        super(aFrame, true);
        setTitle("Set table dimension");
        textField = new JTextField(10);

        String message = String.format(
            "Please enter row count (%d - %d) and column count (%d -%d) separated by comma.",
            MIN_ROW_COUNT, MAX_ROW_COUNT,
            MIN_COLUMN_COUNT, MAX_COLUMN_COUNT
        );
        Object[] array = { message, textField };
        Object[] options = { ENTER_STRING, CANCEL_STRING };
        optionPane = new JOptionPane(
            array,
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.YES_NO_OPTION,
            null,
            options,
            options[0]
        );
        setContentPane(optionPane);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });

        textField.addActionListener(this);
        optionPane.addPropertyChangeListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(ENTER_STRING);
    }

    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (!isVisible()
            || (e.getSource() != optionPane)
            || (!JOptionPane.VALUE_PROPERTY.equals(prop) &&
            !JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)))
        {
            return;
        }

        Object value = optionPane.getValue();
        if (value == JOptionPane.UNINITIALIZED_VALUE) {
            return;
        }

        optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

        if (ENTER_STRING.equals(value)) {
            isValidDimension = validateInput();
            if (isValidDimension) {
                clearAndHide();
            } else {
                textField.selectAll();
                String message = String.format(
                    "Sorry, (%s) isn't a valid table dimension.", textField.getText());
                JOptionPane.showMessageDialog(
                    DimensionDialog.this,
                    message,
                    "Try again",
                    JOptionPane.ERROR_MESSAGE);
                textField.requestFocusInWindow();
            }
        } else { //user closed dialog or clicked cancel
            isValidDimension = false;
            clearAndHide();
        }
    }

    public void clearAndHide() {
        textField.setText(null);
        setVisible(false);
    }
}