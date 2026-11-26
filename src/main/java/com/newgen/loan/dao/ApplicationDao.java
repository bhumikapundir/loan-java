
package com.newgen.loan.dao;

import com.newgen.loan.model.LoanApplication;
import com.newgen.loan.model.Product;
import com.newgen.loan.model.Status;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDao {

    private static final String COLUMNS =
            "application_id, application_number, customer_id, product, "
            + "branch_code, branch_name, requested_amount, tenure_months, "
            + "monthly_income, existing_monthly_obligations, annual_interest_rate, "
            + "proposed_emi, foir_percentage, eligible, eligibility_reasons, status, "
            + "created_at, submitted_at, decided_at, reviewer_code, decision_reason";

    public int insert(Connection conn, LoanApplication a) throws SQLException {
        String sql = "INSERT INTO loan_application (application_number, customer_id, "
                + "product, branch_code, branch_name, requested_amount, tenure_months, "
                + "monthly_income, existing_monthly_obligations, status, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int id;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "TMP-" + System.nanoTime());
            ps.setInt(2, a.getCustomerId());
            ps.setString(3, a.getProduct().name());
            ps.setString(4, a.getBranchCode());
            ps.setString(5, a.getBranchName());
            ps.setBigDecimal(6, a.getRequestedAmount());
            ps.setInt(7, a.getTenureMonths());
            ps.setBigDecimal(8, a.getMonthlyIncome());
            ps.setBigDecimal(9, a.getExistingMonthlyObligations());
            ps.setString(10, a.getStatus().name());
            ps.setTimestamp(11, Timestamp.valueOf(a.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Application insert did not return an id");
                }
                id = keys.getInt(1);
            }
        }

        String number = String.format("LN%tY%<tm%<td-%05d", a.getCreatedAt(), id);
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE loan_application SET application_number = ? WHERE application_id = ?")) {
            ps.setString(1, number);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
        a.setApplicationId(id);
        a.setApplicationNumber(number);
        return id;
    }

    public int updateDraft(Connection conn, LoanApplication a) throws SQLException {
        String sql = "UPDATE loan_application SET product = ?, branch_code = ?, "
                + "branch_name = ?, requested_amount = ?, tenure_months = ?, "
                + "monthly_income = ?, existing_monthly_obligations = ? "
                + "WHERE application_id = ? AND status = 'DRAFT'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getProduct().name());
            ps.setString(2, a.getBranchCode());
            ps.setString(3, a.getBranchName());
            ps.setBigDecimal(4, a.getRequestedAmount());
            ps.setInt(5, a.getTenureMonths());
            ps.setBigDecimal(6, a.getMonthlyIncome());
            ps.setBigDecimal(7, a.getExistingMonthlyObligations());
            ps.setInt(8, a.getApplicationId());
            return ps.executeUpdate();
        }
    }

    public LoanApplication findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM loan_application WHERE application_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public LoanApplication findByNumber(Connection conn, String number) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM loan_application WHERE application_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, number);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<LoanApplication> findByCustomer(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT " + COLUMNS
                + " FROM loan_application WHERE customer_id = ? ORDER BY application_id";
        List<LoanApplication> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    public boolean existsActive(Connection conn, int customerId, Product product) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loan_application WHERE customer_id = ? "
                + "AND product = ? AND status IN ('DRAFT', 'UNDER_REVIEW', 'APPROVED')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setString(2, product.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
        public int updateSubmission(Connection conn, LoanApplication a) throws SQLException {
        String sql = "UPDATE loan_application SET annual_interest_rate = ?, proposed_emi = ?, "
                + "foir_percentage = ?, eligible = ?, eligibility_reasons = ?, status = ?, "
                + "submitted_at = ?, decided_at = ?, reviewer_code = ?, decision_reason = ? "
                + "WHERE application_id = ? AND status = 'DRAFT'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, a.getAnnualInterestRate());
            ps.setBigDecimal(2, a.getProposedEmi());
            ps.setBigDecimal(3, a.getFoirPercentage());
            ps.setBoolean(4, a.getEligible());
            ps.setString(5, a.getEligibilityReasons());
            ps.setString(6, a.getStatus().name());
            ps.setTimestamp(7, Timestamp.valueOf(a.getSubmittedAt()));
            ps.setTimestamp(8, a.getDecidedAt() == null
                    ? null : Timestamp.valueOf(a.getDecidedAt()));
            ps.setString(9, a.getReviewerCode());
            ps.setString(10, a.getDecisionReason());
            ps.setInt(11, a.getApplicationId());
            return ps.executeUpdate();
        }
    }


    private LoanApplication map(ResultSet rs) throws SQLException {
        LoanApplication a = new LoanApplication();
        a.setApplicationId(rs.getInt("application_id"));
        a.setApplicationNumber(rs.getString("application_number"));
        a.setCustomerId(rs.getInt("customer_id"));
        a.setProduct(Product.valueOf(rs.getString("product")));
        a.setBranchCode(rs.getString("branch_code"));
        a.setBranchName(rs.getString("branch_name"));
        a.setRequestedAmount(rs.getBigDecimal("requested_amount"));
        a.setTenureMonths(rs.getInt("tenure_months"));
        a.setMonthlyIncome(rs.getBigDecimal("monthly_income"));
        a.setExistingMonthlyObligations(rs.getBigDecimal("existing_monthly_obligations"));
        a.setAnnualInterestRate(rs.getBigDecimal("annual_interest_rate"));
        a.setProposedEmi(rs.getBigDecimal("proposed_emi"));
        a.setFoirPercentage(rs.getBigDecimal("foir_percentage"));
        boolean eligible = rs.getBoolean("eligible");
        a.setEligible(rs.wasNull() ? null : eligible);
        a.setEligibilityReasons(rs.getString("eligibility_reasons"));
        a.setStatus(Status.valueOf(rs.getString("status")));
        a.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
        a.setSubmittedAt(toLdt(rs.getTimestamp("submitted_at")));
        a.setDecidedAt(toLdt(rs.getTimestamp("decided_at")));
        a.setReviewerCode(rs.getString("reviewer_code"));
        a.setDecisionReason(rs.getString("decision_reason"));
        return a;
    }

    private LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}