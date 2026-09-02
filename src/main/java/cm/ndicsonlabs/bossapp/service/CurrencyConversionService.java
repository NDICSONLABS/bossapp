// src/main/java/com/institution/finance/service/CurrencyConversionService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class CurrencyConversionService {

    private final ExchangeRateRepository exchangeRateRepository;

    public CurrencyConversionService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public BigDecimal convert(
            BigDecimal amount,
            String fromCurrency,
            String toCurrency,
            LocalDate rateDate
    ) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Currency is required for conversion.");
        }

        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        return exchangeRateRepository
                .findTopByFromCurrencyAndToCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(
                        fromCurrency,
                        toCurrency,
                        rateDate
                )
                .map(rate -> amount.multiply(rate.getRate()).setScale(4, RoundingMode.HALF_UP))
                .orElseThrow(() -> new IllegalStateException(
                        "No exchange rate found for " + fromCurrency + " to " + toCurrency + " on or before " + rateDate
                ));
    }
}