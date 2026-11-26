package com.newgen.loan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class EmiCalculator {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public static BigDecimal calculateEmi(BigDecimal principal,
                                          BigDecimal annualRatePercent,
                                          int tenureMonths) {
        BigDecimal r = annualRatePercent.divide(TWELVE, MC).divide(HUNDRED, MC);

        if (r.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal growth = BigDecimal.ONE.add(r).pow(tenureMonths, MC);
        BigDecimal emi = principal.multiply(r, MC).multiply(growth, MC)
                .divide(growth.subtract(BigDecimal.ONE), MC);

        return emi.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateFoir(BigDecimal obligations,
                                           BigDecimal roundedEmi,
                                           BigDecimal monthlyIncome) {
        return obligations.add(roundedEmi)
                .multiply(HUNDRED)
                .divide(monthlyIncome, 2, RoundingMode.HALF_UP);
    }
}