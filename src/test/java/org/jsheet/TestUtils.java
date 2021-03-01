package org.jsheet;

import org.jsheet.model.Value;
import org.jsheet.parser.ParseException;

import javax.swing.table.TableModel;

public class TestUtils {
    public static void setValue(TableModel model, String strValue, int row, int column)
        throws ParseException
    {
        Value value = strValue == null ? null : Value.parse(strValue);
        model.setValueAt(value, row, column);
    }
}
