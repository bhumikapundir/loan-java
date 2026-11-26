package com.newgen.loan.console;

import com.newgen.loan.config.BusinessClock;
import com.newgen.loan.exception.ValidationException;
import com.newgen.loan.model.Customer;
import com.newgen.loan.service.CustomerService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class MainMenu {

    private final Scanner in = new Scanner(System.in);
    private final CustomerService customers = new CustomerService();

    public void run() {
        while (true) {
            System.out.println();
            System.out.println("===== MINI LOAN ORIGINATION =====");
            System.out.println("Business date: " + BusinessClock.today());
            System.out.println("1. Create customer");
            System.out.println("2. Search customer");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            String choice = in.nextLine().trim();

            try {
                switch (choice) {
                    case "1": createCustomer(); break;
                    case "2": searchCustomer(); break;
                    case "0":
                        System.out.println("Goodbye.");
                        return;
                    default:
                        System.out.println("Invalid choice, please try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void createCustomer() throws SQLException {
        System.out.println("--- Create customer ---");
        System.out.print("Name: ");
        String name = in.nextLine().trim();

        System.out.print("Date of birth (yyyy-mm-dd): ");
        LocalDate dob;
        try {
            dob = LocalDate.parse(in.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date. Use the format yyyy-mm-dd, for example 1995-05-20.");
            return;
        }

        System.out.print("Mobile (10 digits): ");
        String mobile = in.nextLine().trim();
        System.out.print("Address: ");
        String address = in.nextLine().trim();
        System.out.print("KYC details: ");
        String kyc = in.nextLine().trim();

        Customer c = new Customer(name, dob, mobile, address, kyc);
        try {
            int id = customers.create(c);
            System.out.println("Customer created with id " + id);
        } catch (ValidationException e) {
            System.out.println("Customer NOT saved. Please fix:");
            for (String error : e.getErrors()) {
                System.out.println("  - " + error);
            }
        }
    }

    private void searchCustomer() throws SQLException {
        System.out.println("--- Search customer ---");
        System.out.println("1. By customer id");
        System.out.println("2. By mobile");
        System.out.println("3. By name");
        System.out.print("Choose: ");
        String choice = in.nextLine().trim();

        switch (choice) {
            case "1":
                System.out.print("Customer id: ");
                try {
                    int id = Integer.parseInt(in.nextLine().trim());
                    show(customers.findById(id));
                } catch (NumberFormatException e) {
                    System.out.println("Customer id must be a number.");
                }
                break;
            case "2":
                System.out.print("Mobile: ");
                show(customers.findByMobile(in.nextLine().trim()));
                break;
            case "3":
                System.out.print("Name (part is fine): ");
                List<Customer> found = customers.searchByName(in.nextLine().trim());
                if (found.isEmpty()) {
                    System.out.println("No customer found.");
                }
                for (Customer c : found) {
                    show(c);
                }
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void show(Customer c) {
        if (c == null) {
            System.out.println("No customer found.");
            return;
        }
        System.out.println("Customer id : " + c.getCustomerId());
        System.out.println("Name        : " + c.getName());
        System.out.println("DOB         : " + c.getDateOfBirth());
        System.out.println("Mobile      : " + c.getMobile());
        System.out.println("Address     : " + c.getAddress());
        System.out.println("KYC         : " + c.getKycDetails());
        System.out.println("-------------------------------");
    }
}