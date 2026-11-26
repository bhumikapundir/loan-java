package com.newgen.loan;

import com.newgen.loan.config.DbConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        try (Connection conn = DbConnection.getConnection()) {
            System.out.println("Connected to database: " + conn.getCatalog());
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}