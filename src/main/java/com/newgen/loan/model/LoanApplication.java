package com.newgen.loan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanApplication {

    private int applicationId;
    private String applicationNumber;
    private int customerId;
    private Product product;
    private String branchCode;
    private String branchName;
    private BigDecimal requestedAmount;
    private int tenureMonths;
    private BigDecimal monthlyIncome;
    private BigDecimal existingMonthlyObligations;
    private BigDecimal annualInterestRate;
    private BigDecimal proposedEmi;
    private BigDecimal foirPercentage;
    private Boolean eligible;
    private String eligibilityReasons;
    private Status status = Status.DRAFT;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime decidedAt;
    private String reviewerCode;
    private String decisionReason;

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int v) { this.applicationId = v; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String v) { this.applicationNumber = v; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int v) { this.customerId = v; }

    public Product getProduct() { return product; }
    public void setProduct(Product v) { this.product = v; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String v) { this.branchCode = v; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String v) { this.branchName = v; }

    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal v) { this.requestedAmount = v; }

    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int v) { this.tenureMonths = v; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal v) { this.monthlyIncome = v; }

    public BigDecimal getExistingMonthlyObligations() { return existingMonthlyObligations; }
    public void setExistingMonthlyObligations(BigDecimal v) { this.existingMonthlyObligations = v; }

    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(BigDecimal v) { this.annualInterestRate = v; }

    public BigDecimal getProposedEmi() { return proposedEmi; }
    public void setProposedEmi(BigDecimal v) { this.proposedEmi = v; }

    public BigDecimal getFoirPercentage() { return foirPercentage; }
    public void setFoirPercentage(BigDecimal v) { this.foirPercentage = v; }

    public Boolean getEligible() { return eligible; }
    public void setEligible(Boolean v) { this.eligible = v; }

    public String getEligibilityReasons() { return eligibilityReasons; }
    public void setEligibilityReasons(String v) { this.eligibilityReasons = v; }

    public Status getStatus() { return status; }
    public void setStatus(Status v) { this.status = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime v) { this.submittedAt = v; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime v) { this.decidedAt = v; }

    public String getReviewerCode() { return reviewerCode; }
    public void setReviewerCode(String v) { this.reviewerCode = v; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String v) { this.decisionReason = v; }

    @Override
    public String toString() {
        return "LoanApplication{" + applicationNumber + ", customer=" + customerId
                + ", " + product + ", amount=" + requestedAmount
                + ", months=" + tenureMonths + ", status=" + status + "}";
    }
}