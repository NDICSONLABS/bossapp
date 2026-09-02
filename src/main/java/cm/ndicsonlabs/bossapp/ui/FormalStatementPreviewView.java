package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.service.FormalStatementService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

@Route(value = "formal-statements", layout = MainLayout.class)
@PermitAll
public class FormalStatementPreviewView extends VerticalLayout {

    private final ComboBox<String> reportType = new ComboBox<>("Statement Type");
    private final ComboBox<Department> departmentBox = new ComboBox<>("Department");
    private final DatePicker asOf = new DatePicker("As Of");
    private final DatePicker from = new DatePicker("From");
    private final DatePicker to = new DatePicker("To");

    private final Anchor previewAnchor = new Anchor();
    private final Anchor downloadAnchor = new Anchor();

    public FormalStatementPreviewView(
            DepartmentRepository departmentRepository,
            FormalStatementService formalStatementService
    ) {
        reportType.setItems("BALANCE_SHEET", "ACTIVITY");
        reportType.setValue("BALANCE_SHEET");

        departmentBox.setItems(departmentRepository.findAll());
        departmentBox.setItemLabelGenerator(Department::getName);
        departmentBox.setClearButtonVisible(true);

        asOf.setValue(LocalDate.now());
        from.setValue(LocalDate.now().withDayOfYear(1));
        to.setValue(LocalDate.now());

        previewAnchor.add(new Button("Preview PDF"));
        previewAnchor.getElement().setAttribute("target", "_blank");

        downloadAnchor.add(new Button("Download PDF"));
        downloadAnchor.getElement().setAttribute("download", true);

        reportType.addValueChangeListener(e -> updateAnchors(formalStatementService));
        departmentBox.addValueChangeListener(e -> updateAnchors(formalStatementService));
        asOf.addValueChangeListener(e -> updateAnchors(formalStatementService));
        from.addValueChangeListener(e -> updateAnchors(formalStatementService));
        to.addValueChangeListener(e -> updateAnchors(formalStatementService));

        updateAnchors(formalStatementService);

        add(
                new H2("Formal Financial Statements"),
                new HorizontalLayout(reportType, departmentBox, asOf, from, to),
                new HorizontalLayout(previewAnchor, downloadAnchor)
        );
    }

    private void updateAnchors(FormalStatementService formalStatementService) {
        String type = reportType.getValue();

        StreamResource previewResource = new StreamResource(type.toLowerCase() + "_preview.pdf",
                () -> new ByteArrayInputStream(
                        formalStatementService.generate(
                                type,
                                departmentBox.getValue() != null ? departmentBox.getValue().getId() : null,
                                asOf.getValue(),
                                from.getValue(),
                                to.getValue(),
                                "PDF"
                        )
                ));

        StreamResource downloadResource = new StreamResource(type.toLowerCase() + ".pdf",
                () -> new ByteArrayInputStream(
                        formalStatementService.generate(
                                type,
                                departmentBox.getValue() != null ? departmentBox.getValue().getId() : null,
                                asOf.getValue(),
                                from.getValue(),
                                to.getValue(),
                                "PDF"
                        )
                ));

        previewAnchor.setHref(previewResource);
        downloadAnchor.setHref(downloadResource);
    }
}