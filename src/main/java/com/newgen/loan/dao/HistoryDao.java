package com.newgen.loan.dao;

import com.newgen.loan.model.Status;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoryDao {

    public record HistoryEntry(int historyId, int applicationId, String action,
                               String oldStatus, String newStatus,
                               String performedBy, LocalDateTime performedAt,
                               String remarks) { }

    public void insert(Connection conn, int applicationId, String action,
                       Status oldStatus, Status newStatus, String performedBy,
                       LocalDateTime performedAt, String remarks) throws SQLException {
        String sql = "INSERT INTO application_history (application_id, action, "
                + "old_status, new_status, performed_by, performed_at, remarks) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.setString(2, action);
            ps.setString(3, oldStatus == null ? null : oldStatus.name());
            ps.setString(4, newStatus == null ? null : newStatus.name());
            ps.setString(5, performedBy);
            ps.setTimestamp(6, Timestamp.valueOf(performedAt));
            ps.setString(7, remarks);
            ps.executeUpdate();
        }
    }

    public List<HistoryEntry> findByApplication(Connection conn, int applicationId)
            throws SQLException {
        String sql = "SELECT history_id, application_id, action, old_status, new_status, "
                + "performed_by, performed_at, remarks FROM application_history "
                + "WHERE application_id = ? ORDER BY history_id";
        List<HistoryEntry> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new HistoryEntry(
                            rs.getInt("history_id"),
                            rs.getInt("application_id"),
                            rs.getString("action"),
                            rs.getString("old_status"),
                            rs.getString("new_status"),
                            rs.getString("performed_by"),
                            rs.getTimestamp("performed_at").toLocalDateTime(),
                            rs.getString("remarks")));
                }
            }
        }
        return result;
    }
}