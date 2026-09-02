// src/main/java/com/institution/finance/repository/ExchangeRateRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    Optional<ExchangeRate> findTopByFromCurrencyAndToCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(
            String fromCurrency,
            String toCurrency,
            LocalDate date
    );
}