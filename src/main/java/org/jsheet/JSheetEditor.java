package org.jsheet;

import org.jsheet.model.Value;
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
                "Parse error",
                "Error",
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
        String editorValue = toEditorValue(this.value);
        return super.getTableCellEditorComponent(table, editorValue, isSelected, row, column);
    }

    private String toEditorValue(Value value) {
        if (value == null)
            return null;
        switch (value.getTag()) {
            case BOOLEAN: return value.getAsBoolean().toString();
            case DOUBLE: return value.getAsDouble().toString();
            case STRING: return value.getAsString();
            case FORMULA: return value.getAsFormula().originalDefinition;
            /* Range value is never shown in the editor
               since ranges only occur inside formulae */
            case RANGE: throw new AssertionError();
        }
        throw new AssertionError();
    }
}