package com.newgen.loan.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AmountFormatter {

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        BigDecimal value = amount.setScale(2, RoundingMode.HALF_UP);
        boolean negative = value.signum() < 0;
        String plain = value.abs().toPlainString();

        int dot = plain.indexOf('.');
        String intPart = plain.substring(0, dot);
        String fraction = plain.substring(dot + 1);

        String grouped;
        if (intPart.length() <= 3) {
            grouped = intPart;
        } else {
            String lastThree = intPart.substring(intPart.length() - 3);
            String rest = intPart.substring(0, intPart.length() - 3);

            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (int i = rest.length() - 1; i >= 0; i--) {
                sb.append(rest.charAt(i));
                count++;
                if (count % 2 == 0 && i != 0) {
                    sb.append(',');
                }
            }
            grouped = sb.reverse() + "," + lastThree;
        }
        return (negative ? "-" : "") + grouped + "." + fraction;
    }
}