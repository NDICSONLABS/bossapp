// src/main/java/com/institution/finance/ui/AcademicCalendarView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AcademicTerm;
import cm.ndicsonlabs.bossapp.domain.AcademicYear;
import cm.ndicsonlabs.bossapp.repository.AcademicTermRepository;
import cm.ndicsonlabs.bossapp.repository.AcademicYearRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "academic-calendar", layout = MainLayout.class)
@PermitAll
public class AcademicCalendarView extends VerticalLayout {

    private final Grid<AcademicYear> yearGrid = new Grid<>(AcademicYear.class);
    private final Grid<AcademicTerm> termGrid = new Grid<>(AcademicTerm.class);

    public AcademicCalendarView(
            AcademicYearRepository yearRepository,
            AcademicTermRepository termRepository
    ) {
        yearGrid.setColumns("code", "name", "startDate", "endDate", "active");
        yearGrid.setItems(yearRepository.findAll());

        termGrid.addColumn(term -> term.getAcademicYear().getName()).setHeader("Year");
        termGrid.setColumns("code", "name", "startDate", "endDate", "active");
        termGrid.setItems(termRepository.findAll());

        Button addYearButton = new Button("New Academic Year", e -> {
            Dialog dialog = new Dialog();

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");
            DatePicker startDate = new DatePicker("Start Date");
            DatePicker endDate = new DatePicker("End Date");

            Button save = new Button("Save", event -> {
                AcademicYear year = new AcademicYear();
                year.setCode(code.getValue());
                year.setName(name.getValue());
                year.setStartDate(startDate.getValue());
                year.setEndDate(endDate.getValue());
                year.setActive(true);

                yearRepository.save(year);
                yearGrid.setItems(yearRepository.findAll());
                dialog.close();
            });

            FormLayout form = new FormLayout(code, name, startDate, endDate);
            dialog.add(form, save);
            dialog.open();
        });

        Button addTermButton = new Button("New Term", e -> {
            Dialog dialog = new Dialog();

            ComboBox<AcademicYear> yearBox = new ComboBox<>("Academic Year");
            yearBox.setItems(yearRepository.findAll());
            yearBox.setItemLabelGenerator(AcademicYear::getName);

            TextField code = new TextField("Code");
            TextField name = new TextField("Name");
            DatePicker startDate = new DatePicker("Start Date");
            DatePicker endDate = new DatePicker("End Date");

            Button save = new Button("Save", event -> {
                if (yearBox.getValue() == null) {
                    Notification.show("Select academic year.");
                    return;
                }

                AcademicTerm term = new AcademicTerm();
                term.setAcademicYear(yearBox.getValue());
                term.setCode(code.getValue());
                term.setName(name.getValue());
                term.setStartDate(startDate.getValue());
                term.setEndDate(endDate.getValue());
                term.setActive(true);

                termRepository.save(term);
                termGrid.setItems(termRepository.findAll());
                dialog.close();
            });

            FormLayout form = new FormLayout(yearBox, code, name, startDate, endDate);
            dialog.add(form, save);
            dialog.open();
        });

        add(
                new H2("Academic Calendar"),
                new HorizontalLayout(addYearButton, addTermButton),
                new H2("Academic Years"),
                yearGrid,
                new H2("Academic Terms"),
                termGrid
        );
    }
}