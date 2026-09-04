package cm.ndicsonlabs.bossapp.ui.ux.spreadsheet;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JSpreadsheetOptions {

    private List<JSpreadsheetWorksheet> worksheets = new ArrayList<>();

    private final Map<String, Object> additionalProperties = new HashMap<>();

    public JSpreadsheetOptions() {
    }

    public static JSpreadsheetOptions withSingleWorksheet(
            JSpreadsheetWorksheet worksheet
    ) {
        JSpreadsheetOptions options = new JSpreadsheetOptions();
        options.setWorksheets(List.of(worksheet));
        return options;
    }

    public List<JSpreadsheetWorksheet> getWorksheets() {
        return worksheets;
    }

    public void setWorksheets(List<JSpreadsheetWorksheet> worksheets) {
        this.worksheets = worksheets;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        additionalProperties.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
}