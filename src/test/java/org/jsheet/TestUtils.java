package org.jsheet;

import org.jsheet.expression.evaluation.Value;
import org.jsheet.parser.ParseException;
import org.jsheet.parser.ParserUtils;

import javax.swing.table.TableModel;

public class TestUtils {
    public static void setValue(TableModel model, String strValue, int row, int column)
        throws ParseException
    {
        Value value = strValue == null ? null : ParserUtils.parseValue(strValue);
        model.setValueAt(value, row, column);
    }
}
