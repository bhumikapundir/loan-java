package com.newgen.loan.dao;

import com.newgen.loan.config.DbConnection;
import com.newgen.loan.model.Customer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    private static final String COLUMNS =
            "customer_id, name, date_of_birth, mobile, address, kyc_details";

    public int save(Customer c) throws SQLException {
        String sql = "INSERT INTO customer "
                + "(name, date_of_birth, mobile, address, kyc_details) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setDate(2, Date.valueOf(c.getDateOfBirth()));
            ps.setString(3, c.getMobile());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getKycDetails());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    c.setCustomerId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Customer insert did not return an id");
    }

    public Customer findById(int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM customer WHERE customer_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Customer findByMobile(String mobile) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM customer WHERE mobile = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mobile);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Customer> searchByName(String namePart) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM customer WHERE name LIKE ? ORDER BY name";
        List<Customer> result = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + namePart + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setName(rs.getString("name"));
        c.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        c.setMobile(rs.getString("mobile"));
        c.setAddress(rs.getString("address"));
        c.setKycDetails(rs.getString("kyc_details"));
        return c;
    }
}