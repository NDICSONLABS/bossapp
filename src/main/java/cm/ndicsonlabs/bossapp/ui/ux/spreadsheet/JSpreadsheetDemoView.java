package cm.ndicsonlabs.bossapp.ui.ux.spreadsheet;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("demo")
public class JSpreadsheetDemoView extends VerticalLayout {

    public JSpreadsheetDemoView() {
        setSizeFull();

        JSpreadsheetWorksheet worksheet = new JSpreadsheetWorksheet();
        worksheet.setWorksheetName("Cars");
        worksheet.setData(List.of(
                List.of("Jazz", "Honda", "2019-02-12", true, "$ 2.000,00"),
                List.of("Civic", "Honda", "2018-07-11", true, "$ 4.000,01")
        ));

        worksheet.setColumns(List.of(
                JSpreadsheetColumn.text("Car", 120),
                JSpreadsheetColumn.dropdown(
                        "Make",
                        200,
                        List.of("Alfa Romeo", "Audi", "BMW", "Honda")
                ),
                JSpreadsheetColumn.calendar("Available", 160),
                JSpreadsheetColumn.checkbox("Stock", 80),
                JSpreadsheetColumn.numeric("Price", 120)
        ));

        worksheet.setTableOverflow(true);
        worksheet.setTableWidth("100%");
        worksheet.setTableHeight("400px");
        worksheet.setFilters(true);
        worksheet.setFreezeColumns(1);

        JSpreadsheetOptions options =
                JSpreadsheetOptions.withSingleWorksheet(worksheet);

        JSpreadsheet spreadsheet = new JSpreadsheet(options);
        spreadsheet.setWidthFull();
        spreadsheet.setHeight("450px");

        Pre output = new Pre();
        output.setWidthFull();

        spreadsheet.addChangeListener(event -> {
            output.setText("Changed:\n" + event.getJson());
        });

        Button read = new Button("Read workbook", event -> {
            spreadsheet.getWorkbook().thenAccept(workbook ->
                    getUI().ifPresent(ui -> ui.access(() ->
                            output.setText(
                                    "Worksheets: " +
                                    workbook.getWorksheets().size()
                            )
                    ))
            );
        });

        Button setA1 = new Button("Set A1", event -> {
            spreadsheet.setValue(0, 0, "Updated from Vaadin");
        });

        Button addRow = new Button("Insert row", event -> {
            spreadsheet.insertRow(1, 1);
        });

        Button deleteRow = new Button("Delete row", event -> {
            spreadsheet.deleteRow(1, 1);
        });

        Button download = new Button("Download", event -> {
            spreadsheet.download();
        });

        add(
                spreadsheet,
                new HorizontalLayout(read, setA1, addRow, deleteRow, download),
                output
        );

        expand(spreadsheet);
    }
}