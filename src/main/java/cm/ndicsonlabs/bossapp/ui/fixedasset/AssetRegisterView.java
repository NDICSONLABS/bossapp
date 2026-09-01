// src/main/java/com/institution/finance/ui/AssetRegisterView.java
package cm.ndicsonlabs.bossapp.ui.fixedasset;

import cm.ndicsonlabs.bossapp.ui.MainLayout;
import cm.ndicsonlabs.bossapp.domain.fixedasset.FixedAsset;
import cm.ndicsonlabs.bossapp.repository.fixedasset.FixedAssetRepository;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "asset-register", layout = MainLayout.class)
@PermitAll
public class AssetRegisterView extends VerticalLayout {

    public AssetRegisterView(FixedAssetRepository assetRepository) {
        Grid<FixedAsset> grid = new Grid<>(FixedAsset.class);
        grid.addColumn(a -> a.getCategory().getName()).setHeader("Category");
        grid.addColumn(a -> a.getDepartment().getName()).setHeader("Department");
        grid.addColumn(a -> a.getCustodianEmployee() != null ? a.getCustodianEmployee().getFullName() : "Unassigned").setHeader("Custodian");
        grid.setColumns(
                "assetNumber", "description", "serialNumber", "acquisitionDate",
                "originalCost", "accumulatedDepreciation", "netBookValue", "status"
        );
        grid.setItems(assetRepository.findAll());

        add(new H2("Fixed Asset Register"), grid);
    }
}