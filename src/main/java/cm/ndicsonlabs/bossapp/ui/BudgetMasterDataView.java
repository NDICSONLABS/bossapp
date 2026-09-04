// src/main/java/com/institution/finance/ui/BudgetMasterDataView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Donor;
import cm.ndicsonlabs.bossapp.domain.Fund;
import cm.ndicsonlabs.bossapp.domain.GrantAward;
import cm.ndicsonlabs.bossapp.repository.DonorRepository;
import cm.ndicsonlabs.bossapp.repository.FundRepository;
import cm.ndicsonlabs.bossapp.repository.GrantAwardRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;

@Route(value = "budget-master-data", layout = MainLayout.class)
@PermitAll
public class BudgetMasterDataView extends VerticalLayout {

    private final Grid<Donor> donorGrid = new Grid<>(Donor.class);
    private final Grid<Fund> fundGrid = new Grid<>(Fund.class);
    private final Grid<GrantAward> grantGrid = new Grid<>(GrantAward.class);

    public BudgetMasterDataView(
            DonorRepository donorRepository,
            FundRepository fundRepository,
            GrantAwardRepository grantRepository
    ) {
        donorGrid.setColumns("code", "name", "donorType", "active");
        donorGrid.setItems(donorRepository.findByOrderByCode());

        fundGrid.addColumn(fund -> fund.getDonor() != null ? fund.getDonor().getName() : "").setHeader("Donor");
        fundGrid.setColumns("code", "name", "active");
        fundGrid.setItems(fundRepository.findByOrderByCode());

        grantGrid.addColumn(grant -> grant.getDonor().getName()).setHeader("Donor");
        grantGrid.addColumn(grant -> grant.getFund() != null ? grant.getFund().getCode() : "").setHeader("Fund");
        grantGrid.setColumns(
                "code",
                "name",
                "startDate",
                "endDate",
                "totalAmount",
                "status",
                "active"
        );
        grantGrid.setItems(grantRepository.findByOrderByCode());

        Button newDonorButton = new Button("New Donor", e -> {
            Dialog dialog = new Dialog();

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");
            TextField donorType = new TextField("Donor Type");

            Button save = new Button("Save", event -> {
                Donor donor = new Donor();
                donor.setCode(code.getValue());
                donor.setName(name.getValue());
                donor.setDonorType(donorType.getValue());
                donor.setActive(true);

                donorRepository.save(donor);
                donorGrid.setItems(donorRepository.findByOrderByCode());
                dialog.close();
            });

            FormLayout form = new FormLayout(code, name, donorType);
            dialog.add(form, save);
            dialog.open();
        });

        Button newFundButton = new Button("New Fund", e -> {
            Dialog dialog = new Dialog();

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");

            ComboBox<Donor> donorBox = new ComboBox<>("Donor");
            donorBox.setItems(donorRepository.findByOrderByCode());
            donorBox.setItemLabelGenerator(Donor::toString);

            Button save = new Button("Save", event -> {
                Fund fund = new Fund();
                fund.setCode(code.getValue());
                fund.setName(name.getValue());
                fund.setDonor(donorBox.getValue());
                fund.setActive(true);

                fundRepository.save(fund);
                fundGrid.setItems(fundRepository.findByOrderByCode());
                dialog.close();
            });

            FormLayout form = new FormLayout(code, name, donorBox);
            dialog.add(form, save);
            dialog.open();
        });

        Button newGrantButton = new Button("New Grant", e -> {
            Dialog dialog = new Dialog();

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");

            ComboBox<Donor> donorBox = new ComboBox<>("Donor");
            donorBox.setItems(donorRepository.findByOrderByCode());
            donorBox.setItemLabelGenerator(Donor::toString);

            ComboBox<Fund> fundBox = new ComboBox<>("Fund");
            fundBox.setItems(fundRepository.findByOrderByCode());
            fundBox.setItemLabelGenerator(Fund::toString);
            fundBox.setClearButtonVisible(true);

            DatePicker startDate = new DatePicker("Start Date");
            DatePicker endDate = new DatePicker("End Date");
            BigDecimalField totalAmount = new BigDecimalField("Total Amount");

            Button save = new Button("Save", event -> {
                GrantAward grant = new GrantAward();
                grant.setCode(code.getValue());
                grant.setName(name.getValue());
                grant.setDonor(donorBox.getValue());
                grant.setFund(fundBox.getValue());
                grant.setStartDate(startDate.getValue());
                grant.setEndDate(endDate.getValue());
                grant.setTotalAmount(totalAmount.getValue() != null ? totalAmount.getValue() : BigDecimal.ZERO);
                grant.setStatus("ACTIVE");
                grant.setActive(true);

                grantRepository.save(grant);
                grantGrid.setItems(grantRepository.findByOrderByCode());
                dialog.close();
            });

            FormLayout form = new FormLayout(
                    code,
                    name,
                    donorBox,
                    fundBox,
                    startDate,
                    endDate,
                    totalAmount
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e -> {
            donorGrid.setItems(donorRepository.findByOrderByCode());
            fundGrid.setItems(fundRepository.findByOrderByCode());
            grantGrid.setItems(grantRepository.findByOrderByCode());
        });

        add(
                new H2("Budget Master Data"),
                new HorizontalLayout(newDonorButton, newFundButton, newGrantButton, refreshButton),

                new H2("Donors"),
                donorGrid,

                new H2("Funds"),
                fundGrid,

                new H2("Grants"),
                grantGrid
        );
    }
}