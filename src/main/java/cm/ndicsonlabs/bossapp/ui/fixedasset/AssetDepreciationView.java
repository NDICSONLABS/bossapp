// src/main/java/com/institution/finance/ui/AssetDepreciationView.java
package cm.ndicsonlabs.bossapp.ui.fixedasset;

import cm.ndicsonlabs.bossapp.domain.fixedasset.AssetDepreciationRun;
import cm.ndicsonlabs.bossapp.domain.fixedasset.FixedAsset;
import cm.ndicsonlabs.bossapp.repository.fixedasset.AssetDepreciationRunRepository;
import cm.ndicsonlabs.bossapp.repository.fixedasset.FixedAssetRepository;
import cm.ndicsonlabs.bossapp.service.fixedasset.AssetManagementService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "asset-depreciation", layout = MainLayout.class)
@PermitAll
public class AssetDepreciationView extends VerticalLayout {

    public AssetDepreciationView(
            AssetManagementService assetService,
            AssetDepreciationRunRepository runRepo,
            FixedAssetRepository assetRepo
    ) {
        Grid<AssetDepreciationRun> runGrid = new Grid<>(AssetDepreciationRun.class);
        runGrid.setColumns("periodYear", "periodMonth", "runDate", "totalDepreciation", "status", "postedBy");
        runGrid.setItems(runRepo.findAll());

        IntegerField year = new IntegerField("Year");
        year.setValue(LocalDate.now().getYear());
        IntegerField month = new IntegerField("Month");
        month.setValue(LocalDate.now().getMonthValue());

        Button runDepreciation = new Button("Run Monthly Depreciation", e -> {
            try {
                assetService.runMonthlyDepreciation(year.getValue(), month.getValue());
                runGrid.setItems(runRepo.findAll());
                Notification.show("Depreciation calculated and posted to GL.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button disposeButton = new Button("Dispose / Write-Off Asset", e -> {
            Dialog dialog = new Dialog();
            ComboBox<FixedAsset> assetBox = new ComboBox<>("Asset");
            assetBox.setItems(assetRepo.findAll());
            assetBox.setItemLabelGenerator(a -> a.getAssetNumber() + " - " + a.getDescription());

            ComboBox<String> typeBox = new ComboBox<>("Disposal Type");
            typeBox.setItems("SOLD", "SCRAPPED", "DONATED", "WRITTEN_OFF");

            DatePicker disposalDate = new DatePicker("Disposal Date");
            disposalDate.setValue(LocalDate.now());
            BigDecimalField proceeds = new BigDecimalField("Proceeds (if sold)");
            TextArea reason = new TextArea("Reason");

            Button save = new Button("Process Disposal", ev -> {
                try {
                    assetService.disposeAsset(
                            assetBox.getValue().getId(), disposalDate.getValue(),
                            typeBox.getValue(), proceeds.getValue() != null ? proceeds.getValue() : java.math.BigDecimal.ZERO,
                            reason.getValue()
                    );
                    dialog.close();
                    Notification.show("Asset disposed and GL updated.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            dialog.add(new FormLayout(assetBox, typeBox, disposalDate, proceeds, reason), save);
            dialog.open();
        });

        add(
                new H2("Asset Depreciation & Disposal"),
                new HorizontalLayout(year, month, runDepreciation, disposeButton),
                runGrid
        );
    }
}