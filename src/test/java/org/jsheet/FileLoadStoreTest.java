package org.jsheet;

import com.opencsv.exceptions.CsvValidationException;
import org.jsheet.model.Cell;
import org.jsheet.model.JSheetTableModel;
import org.jsheet.model.Result;
import org.jsheet.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileLoadStoreTest {
    static JSheetTableModel model;

    @BeforeAll
    static void setUp() throws ParseException {
        model = new JSheetTableModel(5, 5);
        String[][] data = {
            { "1", "2", "3", "4", "5" },
            { null, null, null, null, null },
            { "a,bc", "=A0+BX", "ab\nc", null, "42" },
            { null, null, "=A0+B0", null, null },
            { null, null, null, null, null }
        };
        for (int row = 0; row < data.length; row++) {
            for (int column = 0; column < data[row].length; column++) {
                TestUtils.setValue(model, data[row][column], row, column);
            }
        }
    }

    @Test
    void fileLoadStore() throws IOException, CsvValidationException, ParseException {
        File file = File.createTempFile("test", ".csv");
        file.deleteOnExit();

        JSheetTableModel.write(file, model);
        JSheetTableModel read = JSheetTableModel.read(file);

        assertNotNull(read);
        assertEquals(model.getColumnCount(), read.getColumnCount());
        assertEquals(model.getRowCount(), read.getRowCount());
        for (int row = 0; row < read.getRowCount(); row++) {
            for (int column = 0; column < read.getColumnCount(); column++) {
                Result expected = model.getResultAt(new Cell(row, column));
                Result actual = read.getResultAt(new Cell(row, column));
                assertEquals(expected, actual);
            }
        }
    }
}
