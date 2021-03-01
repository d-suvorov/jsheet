package org.jsheet;

import org.jsheet.model.Formula;
import org.jsheet.model.Value;
import org.jsheet.parser.ParseException;
import org.jsheet.parser.ParserUtils;

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
            value = getModelValue(editorValue);
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
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    private Value getModelValue(String strValue) throws ParseException {
        if (strValue.startsWith("=")) {
            Formula formula = ParserUtils.parse(strValue);
            return Value.of(formula);
        } else {
            return getLiteral(strValue);
        }
    }

    private Value getLiteral(String strValue) {
        // Boolean
        if (strValue.equals("false")) return Value.of(false);
        if (strValue.equals("true")) return Value.of(true);

        // Number
        try {
            return Value.of(Double.parseDouble(strValue));
        } catch (NumberFormatException ignored) {}

        // String
        return Value.of(strValue);
    }
}