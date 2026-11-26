package com.newgen.loan.config;

import com.newgen.loan.model.Product;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class ProductRules {

    public record Rule(BigDecimal minAmount, BigDecimal maxAmount,
                       int minTenure, int maxTenure,
                       BigDecimal minIncome, BigDecimal interestRate) { }

    private static final Properties props = new Properties();
    private static final Map<Product, Rule> rules = new EnumMap<>(Product.class);

    private static int minAge;
    private static int maxAge;
    private static BigDecimal maxFoir;
    private static int overdueDays;

    static {
        try (InputStream in = ProductRules.class.getClassLoader()
                .getResourceAsStream("rules.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "rules.properties not found in src/main/resources");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read rules.properties", e);
        }

        for (Product p : Product.values()) {
            String k = p.name().toLowerCase() + ".";
            rules.put(p, new Rule(
                    dec(k + "minAmount"), dec(k + "maxAmount"),
                    integer(k + "minTenure"), integer(k + "maxTenure"),
                    dec(k + "minIncome"), dec(k + "interestRate")));
        }
        minAge = integer("age.min");
        maxAge = integer("age.max");
        maxFoir = dec("foir.max");
        overdueDays = integer("overdue.days");
    }

    private static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing key in rules.properties: " + key);
        }
        return value.trim();
    }

    private static BigDecimal dec(String key) {
        return new BigDecimal(get(key));
    }

    private static int integer(String key) {
        return Integer.parseInt(get(key));
    }

    public static Rule forProduct(Product product) { return rules.get(product); }
    public static int minAge() { return minAge; }
    public static int maxAge() { return maxAge; }
    public static BigDecimal maxFoir() { return maxFoir; }
    public static int overdueDays() { return overdueDays; }
}