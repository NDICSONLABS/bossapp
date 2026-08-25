// src/main/java/com/institution/finance/seed/DevDataSeeder.java
package cm.ndicsonlabs.bossapp.config;

import cm.ndicsonlabs.bossapp.domain.*;
import cm.ndicsonlabs.bossapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Component
//@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final StudentChargeRepository studentChargeRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final InsuranceProviderRepository insuranceProviderRepository;
    private final PatientAccountRepository patientAccountRepository;
    private final PatientEncounterRepository patientEncounterRepository;
    private final PatientChargeRepository patientChargeRepository;
    private final InsuranceClaimRepository insuranceClaimRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            InstitutionRepository institutionRepository,
            DepartmentRepository departmentRepository,
            StudentRepository studentRepository,
            StudentChargeRepository studentChargeRepository,
            SupplierRepository supplierRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            InsuranceProviderRepository insuranceProviderRepository,
            PatientAccountRepository patientAccountRepository,
            PatientEncounterRepository patientEncounterRepository,
            PatientChargeRepository patientChargeRepository,
            InsuranceClaimRepository insuranceClaimRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.studentChargeRepository = studentChargeRepository;
        this.supplierRepository = supplierRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.insuranceProviderRepository = insuranceProviderRepository;
        this.patientAccountRepository = patientAccountRepository;
        this.patientEncounterRepository = patientEncounterRepository;
        this.patientChargeRepository = patientChargeRepository;
        this.insuranceClaimRepository = insuranceClaimRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedAdminUser();
        seedBaseData();
        seedHealthcareData();
    }

    private void seedAdminUser() {
        Role adminRole = roleRepository.findByName("SYSTEM_ADMINISTRATOR")
                .orElseThrow(() -> new IllegalStateException("SYSTEM_ADMINISTRATOR role not seeded"));

        if (userRepository.findByUsername("admin").isEmpty()) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin"));
            admin.setFullName("System Administrator");
            admin.setActive(true);
            admin.setRoles(new HashSet<>(Set.of(adminRole)));
            userRepository.save(admin);
        }
    }

    private void seedBaseData() {
        if (institutionRepository.count() > 0) {
            return;
        }

        Institution institution = new Institution();
        institution.setName("National Institution");
        institution.setCode("HQ");
        institution.setActive(true);
        institutionRepository.save(institution);

        Department headquarters = new Department();
        headquarters.setInstitution(institution);
        headquarters.setCode("HQ-001");
        headquarters.setName("Headquarters");
        headquarters.setType("HEADQUARTERS");
        headquarters.setActive(true);
        departmentRepository.save(headquarters);

        Department school = new Department();
        school.setInstitution(institution);
        school.setCode("SCH-001");
        school.setName("City Primary School");
        school.setType("SCHOOL");
        school.setActive(true);
        departmentRepository.save(school);

        Department hospital = new Department();
        hospital.setInstitution(institution);
        hospital.setCode("HOSP-001");
        hospital.setName("City Hospital");
        hospital.setType("HOSPITAL");
        hospital.setActive(true);
        departmentRepository.save(hospital);

        Student student = new Student();
        student.setStudentNumber("STU-0001");
        student.setFullName("Student One");
        student.setDepartment(school);
        studentRepository.save(student);

        StudentCharge studentCharge = new StudentCharge();
        studentCharge.setStudent(student);
        studentCharge.setDepartment(school);
        studentCharge.setChargeDate(LocalDate.now());
        studentCharge.setDueDate(LocalDate.now().plusDays(30));
        studentCharge.setAmount(new BigDecimal("150000.0000"));
        studentCharge.setPaidAmount(BigDecimal.ZERO);
        studentCharge.setStatus("POSTED");
        studentChargeRepository.save(studentCharge);

        Supplier supplier = new Supplier();
        supplier.setCode("SUP-0001");
        supplier.setName("National Medical Supplies");
        supplier.setActive(true);
        supplierRepository.save(supplier);

        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setSupplier(supplier);
        invoice.setDepartment(hospital);
        invoice.setInvoiceNumber("INV-0001");
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(45));
        invoice.setTotalAmount(new BigDecimal("5000000.0000"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setStatus("POSTED");
        supplierInvoiceRepository.save(invoice);
    }

    private void seedHealthcareData() {
        if (insuranceProviderRepository.count() == 0) {
            InsuranceProvider provider = new InsuranceProvider();
            provider.setCode("INS-0001");
            provider.setName("National Health Insurance");
            provider.setActive(true);
            insuranceProviderRepository.save(provider);
        }

        if (patientAccountRepository.count() > 0) {
            return;
        }

        Department hospital = departmentRepository.findByCode("HOSP-001")
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        if (hospital == null) {
            return;
        }

        InsuranceProvider provider = insuranceProviderRepository.findAll().stream()
                .findFirst()
                .orElse(null);

        PatientAccount patient = new PatientAccount();
        patient.setPatientNumber("PAT-0001");
        patient.setFullName("Patient One");
        patient.setDepartment(hospital);
        patient.setInsuranceProvider(provider);
        patient.setActive(true);
        patientAccountRepository.save(patient);

        PatientEncounter encounter = new PatientEncounter();
        encounter.setPatientAccount(patient);
        encounter.setDepartment(hospital);
        encounter.setEncounterType("OUTPATIENT");
        encounter.setEncounterDate(LocalDate.now());
        encounter.setStatus("COMPLETED");
        patientEncounterRepository.save(encounter);

        PatientCharge charge = new PatientCharge();
        charge.setPatientAccount(patient);
        charge.setPatientEncounter(encounter);
        charge.setDepartment(hospital);
        charge.setServiceCategory("CONSULTATION");
        charge.setChargeDate(LocalDate.now());
        charge.setDueDate(LocalDate.now().plusDays(15));
        charge.setAmount(new BigDecimal("250000.0000"));
        charge.setPaidAmount(BigDecimal.ZERO);
        charge.setStatus("POSTED");
        patientChargeRepository.save(charge);

        InsuranceClaim claim = new InsuranceClaim();
        claim.setClaimNumber("CLM-0001");
        claim.setPatientAccount(patient);
        claim.setPatientEncounter(encounter);
        claim.setInsuranceProvider(provider);
        claim.setClaimDate(LocalDate.now());
        claim.setAmount(new BigDecimal("200000.0000"));
        claim.setApprovedAmount(new BigDecimal("180000.0000"));
        claim.setPaidAmount(BigDecimal.ZERO);
        claim.setStatus("APPROVED");
        insuranceClaimRepository.save(claim);
    }
}