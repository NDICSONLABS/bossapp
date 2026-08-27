// src/main/java/com/institution/finance/report/LedgerLine.java
package cm.ndicsonlabs.bossapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LedgerLine {

    private String entryNumber;
    private LocalDate entryDate;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;

    public LedgerLine(
            String entryNumber,
            LocalDate entryDate,
            String description,
            BigDecimal debit,
            BigDecimal credit
    ) {
        this.entryNumber = entryNumber;
        this.entryDate = entryDate;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }
}