package org.jsheet;

import org.jsheet.data.Value;
import org.jsheet.parser.ParseException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

class JSheetEditor extends DefaultCellEditor {
    private Value value;
    private final Component errorMessageComponent;

    public JSheetEditor(Component errorMessageComponent) {
        super(new JTextField());
        JTextField field = (JTextField) getComponent();
        field.setBorder(new LineBorder(Color.BLACK));
        this.errorMessageComponent = errorMessageComponent;
    }

    @Override
    public boolean stopCellEditing() {
        String editorValue = (String) super.getCellEditorValue();
        try {
            value = Value.parse(editorValue);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(
                errorMessageComponent,
                "The formula contains syntax errors",
                "Try again",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        return super.stopCellEditing();
    }

    @Override
    public Object getCellEditorValue() {
        return value;
    }

    @Override
    public Component getTableCellEditorComponent(
        JTable table, Object value, boolean isSelected, int row, int column)
    {
        this.value = (Value) value;
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}