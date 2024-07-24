package WorkBook;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import java.io.FileInputStream;
import java.io.IOException;

public class WorkbookWrapper {
    private Workbook workbook;

    // Constructor to initialize the WorkbookWrapper with a FileInputStream
    public WorkbookWrapper(FileInputStream inputStream) throws IOException, BiffException {
        this.workbook = Workbook.getWorkbook(inputStream);
    }

    // Method to get a sheet at a specified index
    public Sheet getSheetAt(int index) {
        return workbook.getSheet(index);
    }

    // Method to close the workbook
    public void close() throws IOException {
        workbook.close();
    }
}
