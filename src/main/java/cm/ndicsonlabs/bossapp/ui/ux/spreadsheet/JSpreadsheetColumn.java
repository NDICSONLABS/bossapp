package cm.ndicsonlabs.bossapp.ui.ux.spreadsheet;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JSpreadsheetColumn {

    private String type;
    private String title;
    private Integer width;
    private List<Object> source;
    private String mask;
    private String decimal;
    private String render;
    private Boolean readOnly;

    private final Map<String, Object> additionalProperties = new HashMap<>();

    public JSpreadsheetColumn() {
    }

    public static JSpreadsheetColumn text(String title, int width) {
        JSpreadsheetColumn column = new JSpreadsheetColumn();
        column.setType("text");
        column.setTitle(title);
        column.setWidth(width);
        return column;
    }

    public static JSpreadsheetColumn numeric(String title, int width) {
        JSpreadsheetColumn column = new JSpreadsheetColumn();
        column.setType("numeric");
        column.setTitle(title);
        column.setWidth(width);
        return column;
    }

    public static JSpreadsheetColumn dropdown(
            String title,
            int width,
            List<Object> source
    ) {
        JSpreadsheetColumn column = new JSpreadsheetColumn();
        column.setType("dropdown");
        column.setTitle(title);
        column.setWidth(width);
        column.setSource(source);
        return column;
    }

    public static JSpreadsheetColumn checkbox(String title, int width) {
        JSpreadsheetColumn column = new JSpreadsheetColumn();
        column.setType("checkbox");
        column.setTitle(title);
        column.setWidth(width);
        return column;
    }

    public static JSpreadsheetColumn calendar(String title, int width) {
        JSpreadsheetColumn column = new JSpreadsheetColumn();
        column.setType("calendar");
        column.setTitle(title);
        column.setWidth(width);
        return column;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public List<Object> getSource() {
        return source;
    }

    public void setSource(List<Object> source) {
        this.source = source;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getDecimal() {
        return decimal;
    }

    public void setDecimal(String decimal) {
        this.decimal = decimal;
    }

    public String getRender() {
        return render;
    }

    public void setRender(String render) {
        this.render = render;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
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