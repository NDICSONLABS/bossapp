package cm.ndicsonlabs.bossapp.ui.ux.spreadsheet;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Tag("jspreadsheet-flow")
@JsModule("./jspreadsheet-connector.ts")
@NpmPackage(value = "jspreadsheet-ce", version = "latest")
@NpmPackage(value = "jsuites", version = "latest")
public class JSpreadsheet extends Component implements HasSize, HasStyle {

    private final String containerId;
    private JSpreadsheetOptions options = new JSpreadsheetOptions();
    private boolean initialized;

    public JSpreadsheet() {
        this.containerId = "jspreadsheet-" + UUID.randomUUID()
                .toString()
                .replace("-", "");

        getElement().getStyle()
                .set("display", "block")
                .set("width", "100%")
                .set("height", "500px")
                .set("overflow", "auto");

        getElement().setProperty("id", containerId + "-host");
    }

    public JSpreadsheet(JSpreadsheetOptions options) {
        this();
        this.options = options;
    }

    public void setOptions(JSpreadsheetOptions options) {
        this.options = options;

        if (initialized) {
            recreate();
        }
    }

    public JSpreadsheetOptions getOptions() {
        return options;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        getElement().executeJs("""
            if (!this._jspreadsheetContainer) {
                this._jspreadsheetContainer = document.createElement('div');
                this._jspreadsheetContainer.id = $0;
                this._jspreadsheetContainer.style.width = '100%';
                this._jspreadsheetContainer.style.height = '100%';
                this.appendChild(this._jspreadsheetContainer);
            }

            this._jspreadsheetConnector =
                new window.JSpreadsheetConnector(this, this._jspreadsheetContainer);

            this._jspreadsheetConnector.create(JSON.parse($1));
            """,
            containerId,
            JSpreadsheetJson.toJson(options)
        );

        initialized = true;
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        getElement().executeJs("""
            if (this._jspreadsheetConnector) {
                this._jspreadsheetConnector.destroy();
                this._jspreadsheetConnector = undefined;
            }
            """);

        initialized = false;

        super.onDetach(detachEvent);
    }

    public void recreate() {
        getElement().executeJs("""
            if (this._jspreadsheetConnector) {
                this._jspreadsheetConnector.create(JSON.parse($0));
            }
            """, JSpreadsheetJson.toJson(options));
    }

    public CompletableFuture<JSpreadsheetWorkbook> getWorkbook() {
        return getElement()
                .executeJs("""
                    return JSON.stringify(
                        this._jspreadsheetConnector
                            ? this._jspreadsheetConnector.getWorkbook()
                            : { worksheets: [] }
                    );
                    """)
                .toCompletableFuture(String.class)
                .thenApply(json -> JSpreadsheetJson.fromJson(json, JSpreadsheetWorkbook.class));
    }

    public CompletableFuture<Void> setWorkbook(JSpreadsheetWorkbook workbook) {
        return getElement()
                .executeJs("""
                    this._jspreadsheetConnector.setWorkbook(JSON.parse($0));
                    """, JSpreadsheetJson.toJson(workbook))
                .toCompletableFuture(Void.class);
    }

    public CompletableFuture<List<List<Object>>> getData() {
        return getData(0);
    }

    public CompletableFuture<List<List<Object>>> getData(int worksheetIndex) {
        return getElement()
                .executeJs("""
                    return JSON.stringify(
                        this._jspreadsheetConnector.getData($0)
                    );
                    """, worksheetIndex)
                .toCompletableFuture(String.class)
                .thenApply(json -> JSpreadsheetJson.fromJsonToNestedList(json));
    }

    public CompletableFuture<Void> setData(List<List<Object>> data) {
        return setData(data, 0);
    }

    public CompletableFuture<Void> setData(List<List<Object>> data, int worksheetIndex) {
        return getElement()
                .executeJs("""
                    this._jspreadsheetConnector.setData(JSON.parse($0), $1);
                    """, JSpreadsheetJson.toJson(data), worksheetIndex)
                .toCompletableFuture(Void.class);
    }

    public CompletableFuture<Object> getValue(int row, int column) {
        return getValue(row, column, 0);
    }

    public CompletableFuture<Object> getValue(int row, int column, int worksheetIndex) {
        return getElement()
                .executeJs("""
                    return this._jspreadsheetConnector.getValue($0, $1, $2);
                    """, row, column, worksheetIndex)
                .toCompletableFuture(Object.class);
    }

    public CompletableFuture<Void> setValue(int row, int column, Object value) {
        return setValue(row, column, value, 0);
    }

    public CompletableFuture<Void> setValue(
            int row,
            int column,
            Object value,
            int worksheetIndex
    ) {
        return getElement()
                .executeJs("""
                    this._jspreadsheetConnector.setValue($0, $1, JSON.parse($2), $3);
                    """,
                    row,
                    column,
                    JSpreadsheetJson.toJson(value),
                    worksheetIndex
                )
                .toCompletableFuture(Void.class);
    }

    public CompletableFuture<Object> exec(
            String methodName,
            List<Object> args
    ) {
        return exec(methodName, args, 0);
    }

    public CompletableFuture<Object> exec(
            String methodName,
            List<Object> args,
            int worksheetIndex
    ) {
        return getElement()
                .executeJs("""
                    return this._jspreadsheetConnector.exec($0, JSON.parse($1), $2);
                    """,
                    methodName,
                    JSpreadsheetJson.toJson(args),
                    worksheetIndex
                )
                .toCompletableFuture(Object.class);
    }

    public CompletableFuture<Void> insertRow(int rowNumber, int amount) {
        return exec("insertRow", List.of(amount, rowNumber))
                .thenApply(ignored -> null);
    }

    public CompletableFuture<Void> deleteRow(int rowNumber, int amount) {
        return exec("deleteRow", List.of(rowNumber, amount))
                .thenApply(ignored -> null);
    }

    public CompletableFuture<Void> insertColumn(int columnNumber, int amount) {
        return exec("insertColumn", List.of(amount, columnNumber))
                .thenApply(ignored -> null);
    }

    public CompletableFuture<Void> deleteColumn(int columnNumber, int amount) {
        return exec("deleteColumn", List.of(columnNumber, amount))
                .thenApply(ignored -> null);
    }

    public CompletableFuture<Void> download() {
        return exec("download", List.of())
                .thenApply(ignored -> null);
    }

    public Registration addChangeListener(
            ComponentEventListener<JSpreadsheetChangedEvent> listener
    ) {
        return addListener(JSpreadsheetChangedEvent.class, listener);
    }

    @DomEvent("jspreadsheet-changed")
    public static class JSpreadsheetChangedEvent extends ComponentEvent<JSpreadsheet> {

        private final String json;

        public JSpreadsheetChangedEvent(
                JSpreadsheet source,
                boolean fromClient,
                @EventData("event.detail.json") String json
        ) {
            super(source, fromClient);
            this.json = json;
        }

        public String getJson() {
            return json;
        }

        public JSpreadsheetWorkbook getWorkbook() {
            return JSpreadsheetJson.fromJson(json, JSpreadsheetWorkbook.class);
        }
    }
}