package org.jsheet;

import org.jsheet.model.ExprWrapper;
import org.jsheet.model.JSheetTableModel;
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
            { "abc", "=A0+BO", null, null, "42" },
            { null, null, null, null, null },
            { null, null, null, null, null }
        };
        for (int row = 0; row < data.length; row++) {
            for (int column = 0; column < data[row].length; column++) {
                model.setValueAt(data[row][column], row, column);
            }
        }
    }

    @Test
    void testFileLoadStore() throws IOException {
        File file = File.createTempFile("test", "csv");
        file.deleteOnExit();

        JSheetTableModel.write(file, model);
        JSheetTableModel read = JSheetTableModel.read(file);

        assertNotNull(read);
        assertEquals(model.getColumnCount(), read.getColumnCount());
        assertEquals(3, read.getRowCount());
        for (int row = 0; row < read.getRowCount(); row++) {
            for (int column = 0; column < read.getColumnCount(); column++) {
                Object expected = model.getValueAt(row, column);
                Object actual = read.getValueAt(row, column);
                if (expected instanceof ExprWrapper) {
                    assertTrue(actual instanceof ExprWrapper);
                    assertEquals(
                        ((ExprWrapper) expected).eval(model),
                        ((ExprWrapper) actual).eval(read)
                    );
                } else {
                    assertEquals(expected, actual);
                }
            }
        }
    }
}