package com.newgen.loan.service;

import com.newgen.loan.config.BusinessClock;
import com.newgen.loan.config.DbConnection;
import com.newgen.loan.dao.ApplicationDao;
import com.newgen.loan.dao.CustomerDao;
import com.newgen.loan.dao.HistoryDao;
import com.newgen.loan.exception.ValidationException;
import com.newgen.loan.model.LoanApplication;
import com.newgen.loan.model.Status;
import com.newgen.loan.config.ProductRules;
import com.newgen.loan.model.Customer;
import com.newgen.loan.util.AmountFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationService {

    private final ApplicationDao appDao = new ApplicationDao();
    private final HistoryDao historyDao = new HistoryDao();
    private final CustomerDao customerDao = new CustomerDao();

    private List<String> validateInputs(BigDecimal amount, int tenure,
                                        BigDecimal income, BigDecimal obligations) {
        List<String> errors = new ArrayList<>();
        if (amount == null || amount.signum() <= 0) {
            errors.add("Requested amount must be positive");
        }
        if (tenure <= 0) {
            errors.add("Tenure must be a whole number of months, greater than 0");
        }
        if (income == null || income.signum() <= 0) {
            errors.add("Monthly income must be positive");
        }
        if (obligations == null || obligations.signum() < 0) {
            errors.add("Existing obligations cannot be negative");
        }
        return errors;
    }

    public LoanApplication createDraft(LoanApplication a, String userCode)
            throws ValidationException, SQLException {
        List<String> errors = validateInputs(a.getRequestedAmount(), a.getTenureMonths(),
                a.getMonthlyIncome(), a.getExistingMonthlyObligations());
        if (a.getProduct() == null) {
            errors.add("Product is required");
        }
        if (customerDao.findById(a.getCustomerId()) == null) {
            errors.add("Customer not found: id " + a.getCustomerId());
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (appDao.existsActive(conn, a.getCustomerId(), a.getProduct())) {
                    throw new ValidationException(List.of(
                            "Customer already has an active " + a.getProduct()
                                    + " application (DRAFT, UNDER_REVIEW or APPROVED)"));
                }
                a.setStatus(Status.DRAFT);
                a.setCreatedAt(BusinessClock.now());
                appDao.insert(conn, a);
                historyDao.insert(conn, a.getApplicationId(), "CREATE", null,
                        Status.DRAFT, userCode, BusinessClock.now(), "Draft created");
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
        return a;
    }

    public LoanApplication editDraft(int applicationId, BigDecimal amount, int tenure,
                                     BigDecimal income, BigDecimal obligations,
                                     String userCode)
            throws ValidationException, SQLException {
        List<String> errors = validateInputs(amount, tenure, income, obligations);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                LoanApplication a = appDao.findById(conn, applicationId);
                if (a == null) {
                    throw new ValidationException(List.of("Application not found"));
                }
                if (a.getStatus() != Status.DRAFT) {
                    throw new ValidationException(List.of(
                            "Only DRAFT applications can be edited (this one is "
                                    + a.getStatus() + ")"));
                }
                a.setRequestedAmount(amount);
                a.setTenureMonths(tenure);
                a.setMonthlyIncome(income);
                a.setExistingMonthlyObligations(obligations);

                if (appDao.updateDraft(conn, a) != 1) {
                    throw new ValidationException(List.of("Draft could not be updated"));
                }
                historyDao.insert(conn, applicationId, "EDIT", Status.DRAFT,
                        Status.DRAFT, userCode, BusinessClock.now(), "Draft edited");
                conn.commit();
                return a;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public LoanApplication findByNumber(String number) throws SQLException {
        try (Connection conn = DbConnection.getConnection()) {
            return appDao.findByNumber(conn, number);
        }
    }

    public List<HistoryDao.HistoryEntry> history(int applicationId) throws SQLException {
        try (Connection conn = DbConnection.getConnection()) {
            return historyDao.findByApplication(conn, applicationId);
        }
    }
        private boolean simulateFailure = false;

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    public LoanApplication findById(int id) throws SQLException {
        try (Connection conn = DbConnection.getConnection()) {
            return appDao.findById(conn, id);
        }
    }

    public LoanApplication submit(int applicationId, String userCode)
            throws ValidationException, SQLException {
        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                LoanApplication a = appDao.findById(conn, applicationId);
                if (a == null) {
                    throw new ValidationException(List.of("Application not found"));
                }
                if (a.getStatus() != Status.DRAFT) {
                    throw new ValidationException(List.of(
                            "Invalid transition: only DRAFT can be submitted (this one is "
                                    + a.getStatus() + ")"));
                }

                Customer customer = customerDao.findById(a.getCustomerId());
                ProductRules.Rule rule = ProductRules.forProduct(a.getProduct());
                List<String> reasons = new ArrayList<>();

                LocalDate today = BusinessClock.today();
                LocalDate dob = customer.getDateOfBirth();
                if (dob.isAfter(today)) {
                    reasons.add("Date of birth is in the future");
                } else {
                    int age = Period.between(dob, today).getYears();
                    if (age < ProductRules.minAge() || age > ProductRules.maxAge()) {
                        reasons.add("Age must be between " + ProductRules.minAge() + " and "
                                + ProductRules.maxAge() + " (applicant is " + age + ")");
                    }
                }

                if (a.getRequestedAmount().compareTo(rule.minAmount()) < 0
                        || a.getRequestedAmount().compareTo(rule.maxAmount()) > 0) {
                    reasons.add("Amount must be between " + AmountFormatter.format(rule.minAmount())
                            + " and " + AmountFormatter.format(rule.maxAmount()));
                }
                if (a.getTenureMonths() < rule.minTenure() || a.getTenureMonths() > rule.maxTenure()) {
                    reasons.add("Tenure must be between " + rule.minTenure() + " and "
                            + rule.maxTenure() + " months");
                }
                if (a.getMonthlyIncome().compareTo(rule.minIncome()) < 0) {
                    reasons.add("Monthly income must be at least "
                            + AmountFormatter.format(rule.minIncome()));
                }

                BigDecimal emi = EmiCalculator.calculateEmi(
                        a.getRequestedAmount(), rule.interestRate(), a.getTenureMonths());
                BigDecimal foir = EmiCalculator.calculateFoir(
                        a.getExistingMonthlyObligations(), emi, a.getMonthlyIncome());
                if (foir.compareTo(ProductRules.maxFoir()) > 0) {
                    reasons.add("FOIR " + foir + "% is above the maximum "
                            + ProductRules.maxFoir() + "%");
                }

                boolean eligible = reasons.isEmpty();
                Status target = eligible ? Status.UNDER_REVIEW : Status.REJECTED;
                if (!a.getStatus().canMoveTo(target)) {
                    throw new ValidationException(List.of(
                            "Invalid transition: " + a.getStatus() + " to " + target));
                }

                LocalDateTime now = BusinessClock.now();
                a.setAnnualInterestRate(rule.interestRate());
                a.setProposedEmi(emi);
                a.setFoirPercentage(foir);
                a.setEligible(eligible);
                a.setEligibilityReasons(eligible
                        ? "All eligibility rules passed" : String.join("; ", reasons));
                a.setStatus(target);
                a.setSubmittedAt(now);
                if (!eligible) {
                    a.setDecidedAt(now);
                    a.setReviewerCode("SYSTEM");
                    a.setDecisionReason(a.getEligibilityReasons());
                }

                if (appDao.updateSubmission(conn, a) != 1) {
                    throw new ValidationException(List.of("Application could not be submitted"));
                }
                if (simulateFailure) {
                    throw new SQLException("Simulated failure after update (rollback test)");
                }
                historyDao.insert(conn, applicationId, "SUBMIT", Status.DRAFT, target,
                        userCode, now, eligible
                                ? "Passed eligibility, sent for review"
                                : "Rejected: " + a.getEligibilityReasons());
                conn.commit();
                return a;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}