// src/main/java/com/institution/finance/domain/PatientEncounter.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "patient_encounter")
public class PatientEncounter extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_account_id", nullable = false)
    private PatientAccount patientAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "encounter_type")
    private String encounterType;

    @Column(name = "encounter_date")
    private LocalDate encounterDate;

    private String status = "OPEN";

    public PatientAccount getPatientAccount() {
        return patientAccount;
    }

    public void setPatientAccount(PatientAccount patientAccount) {
        this.patientAccount = patientAccount;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public LocalDate getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(LocalDate encounterDate) {
        this.encounterDate = encounterDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}