// src/main/java/com/institution/finance/ui/ReportControlView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.ReportTemplate;
import cm.ndicsonlabs.bossapp.repository.ReportTemplateRepository;
import cm.ndicsonlabs.bossapp.service.ReportControlService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;


@Route(value = "reports-control", layout = MainLayout.class)
@PermitAll
public class ReportControlView extends VerticalLayout {

    StreamResourceRegistry registry = VaadinSession.getCurrent().getResourceRegistry();

    private enum DownloadFormat {
        PDF("PDF", "pdf"),
        CSV("CSV", "csv"),
        XLSX("XLSX", "xlsx");

        private final String serviceFormat;
        private final String fileExtension;

        DownloadFormat(String serviceFormat, String fileExtension) {
            this.serviceFormat = serviceFormat;
            this.fileExtension = fileExtension;
        }
    }

    private record DownloadControl(
            DownloadFormat format,
            Anchor anchor
    ) {
    }

    private final ComboBox<ReportTemplate> templateBox =
            new ComboBox<>("Report Template");

    private final DatePicker asOf =
            new DatePicker("As Of");

    private final List<DownloadControl> downloads;

    public ReportControlView(
            ReportTemplateRepository reportTemplateRepository,
            ReportControlService reportControlService
    ) {
        downloads = List.of(
                createDownloadControl(DownloadFormat.PDF),
                createDownloadControl(DownloadFormat.CSV),
                createDownloadControl(DownloadFormat.XLSX)
        );

        templateBox.setItems(
                reportTemplateRepository.findByActiveTrueOrderByCode()
        );
        templateBox.setItemLabelGenerator(ReportTemplate::getName);

        asOf.setValue(LocalDate.now());

        templateBox.addValueChangeListener(event ->
                updateDownloads(reportControlService)
        );

        asOf.addValueChangeListener(event ->
                updateDownloads(reportControlService)
        );

        add(
                new H2("Reporting and Control"),
                new HorizontalLayout(templateBox, asOf),
                new HorizontalLayout(
                        downloads.stream()
                                .map(DownloadControl::anchor)
                                .toArray(Anchor[]::new)
                )
        );

        updateDownloads(reportControlService);
    }

    private DownloadControl createDownloadControl(
            DownloadFormat format
    ) {
        Button button = new Button("Download " + format.serviceFormat);
        Anchor anchor = new Anchor();
        anchor.add(button);

        anchor.getElement().setAttribute("download", true);
        anchor.setEnabled(false);

        return new DownloadControl(format, anchor);
    }

    private void updateDownloads(
            ReportControlService reportControlService
    ) {
        ReportTemplate template = templateBox.getValue();
        LocalDate reportDate = asOf.getValue();

        boolean enabled = template != null && reportDate != null;

        downloads.forEach(download -> {
            Anchor anchor = download.anchor();

            anchor.setEnabled(enabled);

            if (!enabled) {
                anchor.setHref("");
                return;
            }

            StreamResource streamResource = createResource(
                    template,
                    reportDate,
                    download.format(),
                    reportControlService
            );
            StreamRegistration streamRegistration = registry.registerResource(streamResource);
            anchor.setHref(streamRegistration.getResourceUri().toString());
        });
    }

    private StreamResource createResource(
            ReportTemplate template,
            LocalDate reportDate,
            DownloadFormat format,
            ReportControlService reportControlService
    ) {
        String baseName = template.getCode()
                .toLowerCase(Locale.ROOT);

        String fileName = baseName + "." + format.fileExtension;

        return new StreamResource(fileName, () ->
                new ByteArrayInputStream(
                        reportControlService.generate(
                                template,
                                format.serviceFormat,
                                reportDate
                        )
                )
        );
    }
}

//public class ReportControlView extends VerticalLayout {
//
//    private final ComboBox<ReportTemplate> templateBox = new ComboBox<>("Report Template");
//    private final DatePicker asOf = new DatePicker("As Of");
//
//    private final Anchor pdfAnchor = new Anchor();
//    private final Anchor csvAnchor = new Anchor();
//    private final Anchor xlsxAnchor = new Anchor();
//
//    private final Button pdfButton = new Button("Download PDF");
//    private final Button csvButton = new Button("Download CSV");
//    private final Button xlsxButton = new Button("Download XLSX");
//
//    public ReportControlView(
//            ReportTemplateRepository reportTemplateRepository,
//            ReportControlService reportControlService
//    ) {
//        templateBox.setItems(reportTemplateRepository.findByActiveTrueOrderByCode());
//        templateBox.setItemLabelGenerator(ReportTemplate::getName);
//
//        asOf.setValue(LocalDate.now());
//
//        pdfAnchor.add(pdfButton);
//        csvAnchor.add(csvButton);
//        xlsxAnchor.add(xlsxButton);
//
//        pdfAnchor.getElement().setAttribute("download", true);
//        csvAnchor.getElement().setAttribute("download", true);
//        xlsxAnchor.getElement().setAttribute("download", true);
//
//        templateBox.addValueChangeListener(e -> updateDownloads(reportControlService));
//        asOf.addValueChangeListener(e -> updateDownloads(reportControlService));
//
//        updateDownloads(reportControlService);
//
//        add(
//                new H2("Reporting and Control"),
//                new HorizontalLayout(templateBox, asOf),
//                new HorizontalLayout(pdfAnchor, csvAnchor, xlsxAnchor)
//        );
//    }
//
//    private void updateDownloads(ReportControlService reportControlService) {
//        ReportTemplate template = templateBox.getValue();
//        boolean enabled = template != null;
//
//        pdfAnchor.setEnabled(enabled);
//        csvAnchor.setEnabled(enabled);
//        xlsxAnchor.setEnabled(enabled);
//
//        if (!enabled) {
//            pdfAnchor.setHref("");
//            csvAnchor.setHref("");
//            xlsxAnchor.setHref("");
//            return;
//        }
//
//        String baseName = template.getCode().toLowerCase();
//
//        pdfAnchor.setsetStreamResource(new StreamResource(baseName + ".pdf",
//                () -> new ByteArrayInputStream(
//                        reportControlService.generate(template, "PDF", asOf.getValue())
//                )));
//
//        csvAnchor.setsetStreamResource(new StreamResource(baseName + ".csv",
//                () -> new ByteArrayInputStream(
//                        reportControlService.generate(template, "CSV", asOf.getValue())
//                )));
//
//        xlsxAnchor.setsetStreamResource(new StreamResource(baseName + ".xlsx",
//                () -> new ByteArrayInputStream(
//                        reportControlService.generate(template, "XLSX", asOf.getValue())
//                )));
//    }
//}