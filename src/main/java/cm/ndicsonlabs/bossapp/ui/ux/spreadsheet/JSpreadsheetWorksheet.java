package cm.ndicsonlabs.bossapp.ui.ux.spreadsheet;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JSpreadsheetWorksheet {
    private Integer minSpareRows = 5;
    private Boolean allowInsertRow = true;
    private Boolean allowDeleteRow = true;
    private Boolean contextMenu = true;

    private String worksheetName;
    private List<List<Object>> data = new ArrayList<>();
    private List<JSpreadsheetColumn> columns = new ArrayList<>();
    private List<Integer> minDimensions = List.of(4, 4);
    private Boolean tableOverflow;
    private String tableWidth;
    private String tableHeight;
    private Boolean filters;
    private Integer freezeColumns;

    private final Map<String, Object> additionalProperties = new HashMap<>();

    public JSpreadsheetWorksheet() {
    }

    public static JSpreadsheetWorksheet ofData(List<List<Object>> data) {
        JSpreadsheetWorksheet worksheet = new JSpreadsheetWorksheet();
        worksheet.setData(data);
        return worksheet;
    }

    public String getWorksheetName() {
        return worksheetName;
    }

    public void setWorksheetName(String worksheetName) {
        this.worksheetName = worksheetName;
    }

    public List<List<Object>> getData() {
        return data;
    }

    public void setData(List<List<Object>> data) {
        this.data = data;
    }

    public List<JSpreadsheetColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<JSpreadsheetColumn> columns) {
        this.columns = columns;
    }

    public List<Integer> getMinDimensions() {
        return minDimensions;
    }

    public void setMinDimensions(List<Integer> minDimensions) {
        this.minDimensions = minDimensions;
    }

    public Boolean getTableOverflow() {
        return tableOverflow;
    }

    public void setTableOverflow(Boolean tableOverflow) {
        this.tableOverflow = tableOverflow;
    }

    public String getTableWidth() {
        return tableWidth;
    }

    public void setTableWidth(String tableWidth) {
        this.tableWidth = tableWidth;
    }

    public String getTableHeight() {
        return tableHeight;
    }

    public void setTableHeight(String tableHeight) {
        this.tableHeight = tableHeight;
    }

    public Boolean getFilters() {
        return filters;
    }

    public void setFilters(Boolean filters) {
        this.filters = filters;
    }

    public Integer getFreezeColumns() {
        return freezeColumns;
    }

    public void setFreezeColumns(Integer freezeColumns) {
        this.freezeColumns = freezeColumns;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        additionalProperties.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public Integer getMinSpareRows() {
        return minSpareRows;
    }

    public void setMinSpareRows(Integer minSpareRows) {
        this.minSpareRows = minSpareRows;
    }

    public Boolean getAllowInsertRow() {
        return allowInsertRow;
    }

    public void setAllowInsertRow(Boolean allowInsertRow) {
        this.allowInsertRow = allowInsertRow;
    }

    public Boolean getAllowDeleteRow() {
        return allowDeleteRow;
    }

    public void setAllowDeleteRow(Boolean allowDeleteRow) {
        this.allowDeleteRow = allowDeleteRow;
    }

    public Boolean getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(Boolean contextMenu) {
        this.contextMenu = contextMenu;
    }
}