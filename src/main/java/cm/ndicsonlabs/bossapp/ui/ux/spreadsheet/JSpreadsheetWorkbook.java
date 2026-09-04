package cm.ndicsonlabs.bossapp.ui.ux.spreadsheet;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JSpreadsheetWorkbook {

    private List<JSpreadsheetWorksheet> worksheets = new ArrayList<>();

    public JSpreadsheetWorkbook() {
    }

    public JSpreadsheetWorkbook(List<JSpreadsheetWorksheet> worksheets) {
        this.worksheets = worksheets;
    }

    public List<JSpreadsheetWorksheet> getWorksheets() {
        return worksheets;
    }

    public void setWorksheets(List<JSpreadsheetWorksheet> worksheets) {
        this.worksheets = worksheets;
    }
}