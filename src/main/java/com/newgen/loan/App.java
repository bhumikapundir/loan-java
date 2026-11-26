package com.newgen.loan;

import com.newgen.loan.dao.CustomerDao;
import com.newgen.loan.model.Customer;
import java.sql.SQLException;
import java.time.LocalDate;

public class App {
    public static void main(String[] args) {
        CustomerDao dao = new CustomerDao();
        try {
            if (dao.findByMobile("9876543210") == null) {
                Customer c = new Customer("Rahul Sharma", LocalDate.of(1995, 5, 20),
                        "9876543210", "Meerut, UP", "Aadhaar XXXX-1234");
                int id = dao.save(c);
                System.out.println("Saved with id " + id);
            } else {
                System.out.println("Already saved, skipping insert");
            }
            Customer byMobile = dao.findByMobile("9876543210");
            System.out.println("By mobile : " + byMobile);
            System.out.println("By id     : " + dao.findById(byMobile.getCustomerId()));
            System.out.println("By name   : " + dao.searchByName("rah"));
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}