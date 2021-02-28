package org.jsheet;

import com.opencsv.exceptions.CsvValidationException;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Type;
import org.jsheet.model.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileLoadStoreTest {
    static JSheetTableModel model;

    @BeforeAll
    static void setUp() {
        model = new JSheetTableModel(5, 5);
        Object[][] data = {
            { "1", "2", "3", "4", "5" },
            { null, null, null, null, null },
            { "a,bc", "=A0+BX", "ab\nc", null, "42" },
            { null, null, "=A0+B0", null, null },
            { null, null, null, null, null }
        };
        for (int row = 0; row < data.length; row++) {
            for (int column = 0; column < data[row].length; column++) {
                model.setValueAt(data[row][column], row, column);
            }
        }
    }

    @Test
    void fileLoadStore() throws IOException, CsvValidationException {
        File file = File.createTempFile("test", ".csv");
        file.deleteOnExit();

        JSheetTableModel.write(file, model);
        JSheetTableModel read = JSheetTableModel.read(file);

        assertNotNull(read);
        assertEquals(model.getColumnCount(), read.getColumnCount());
        assertEquals(model.getRowCount(), read.getRowCount());
        for (int row = 0; row < read.getRowCount(); row++) {
            for (int column = 0; column < read.getColumnCount(); column++) {
                Value expected = model.getValueAt(row, column);
                Value actual = read.getValueAt(row, column);
                if (expected != null && expected.getTag() == Type.FORMULA) {
                    assertNotNull(actual);
                    assertSame(Type.FORMULA, actual.getTag());
                    assertEquals(
                        expected.getAsFormula().eval(model),
                        actual.getAsFormula().eval(read)
                    );
                } else {
                    assertEquals(expected, actual);
                }
            }
        }
    }
}
