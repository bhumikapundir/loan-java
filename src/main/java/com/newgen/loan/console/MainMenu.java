package com.newgen.loan.console;

import com.newgen.loan.config.BusinessClock;
import com.newgen.loan.dao.HistoryDao;
import com.newgen.loan.exception.ValidationException;
import com.newgen.loan.model.Customer;
import com.newgen.loan.model.LoanApplication;
import com.newgen.loan.model.Product;
import com.newgen.loan.service.ApplicationService;
import com.newgen.loan.service.CustomerService;
import com.newgen.loan.util.AmountFormatter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class MainMenu {

    private static final String USER = "CLERK01";

    private final Scanner in = new Scanner(System.in);
    private final CustomerService customers = new CustomerService();
    private final ApplicationService applications = new ApplicationService();

    public void run() {
        while (true) {
            System.out.println();
            System.out.println("===== MINI LOAN ORIGINATION =====");
            System.out.println("Business date: " + BusinessClock.today());
            System.out.println("1. Create customer");
            System.out.println("2. Search customer");
            System.out.println("3. Loan applications");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            String choice = in.nextLine().trim();

            try {
                switch (choice) {
                    case "1": createCustomer(); break;
                    case "2": searchCustomer(); break;
                    case "3": applicationMenu(); break;
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

    // ---------------------------------------------------------------- customers

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
            printErrors("Customer NOT saved. Please fix:", e);
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

    // ------------------------------------------------------------- applications

    private void applicationMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- Loan applications ---");
            System.out.println("1. Create draft");
            System.out.println("2. Edit draft");
            System.out.println("3. Submit application");
            System.out.println("4. View application details");
            System.out.println("5. View application history");
            System.out.println("6. List applications of a customer");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            String choice = in.nextLine().trim();

            try {
                switch (choice) {
                    case "1": createDraft(); break;
                    case "2": editDraft(); break;
                    case "3": submitApplication(); break;
                    case "4": viewDetails(); break;
                    case "5": viewHistory(); break;
                    case "6": listByCustomer(); break;
                    case "0": return;
                    default:
                        System.out.println("Invalid choice, please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number entered. Nothing was saved.");
            } catch (ValidationException e) {
                printErrors("Not done. Please fix:", e);
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void createDraft() throws ValidationException, SQLException {
        System.out.println("--- Create draft ---");
        int customerId = Integer.parseInt(ask("Customer id: "));

        System.out.println("Product: 1. PERSONAL   2. EDUCATION");
        String p = ask("Choose product: ");
        Product product;
        if (p.equals("1")) {
            product = Product.PERSONAL;
        } else if (p.equals("2")) {
            product = Product.EDUCATION;
        } else {
            System.out.println("Invalid product choice.");
            return;
        }

        String branchCode = ask("Branch code: ");
        String branchName = ask("Branch name: ");
        BigDecimal amount = new BigDecimal(ask("Requested amount: "));
        int tenure = Integer.parseInt(ask("Tenure (months): "));
        BigDecimal income = new BigDecimal(ask("Monthly income: "));
        BigDecimal obligations = new BigDecimal(ask("Existing monthly obligations: "));

        LoanApplication a = new LoanApplication();
        a.setCustomerId(customerId);
        a.setProduct(product);
        a.setBranchCode(branchCode);
        a.setBranchName(branchName);
        a.setRequestedAmount(amount);
        a.setTenureMonths(tenure);
        a.setMonthlyIncome(income);
        a.setExistingMonthlyObligations(obligations);

        LoanApplication saved = applications.createDraft(a, USER);
        System.out.println("Draft created. Application number: " + saved.getApplicationNumber());
    }

    private void editDraft() throws ValidationException, SQLException {
        System.out.println("--- Edit draft (press Enter to keep the current value) ---");
        LoanApplication a = findByNumberOrReport(ask("Application number: "));
        if (a == null) {
            return;
        }

        BigDecimal amount = askDecimal("Requested amount", a.getRequestedAmount());
        int tenure = askInt("Tenure (months)", a.getTenureMonths());
        BigDecimal income = askDecimal("Monthly income", a.getMonthlyIncome());
        BigDecimal obligations = askDecimal("Existing monthly obligations",
                a.getExistingMonthlyObligations());

        applications.editDraft(a.getApplicationId(), amount, tenure, income, obligations, USER);
        System.out.println("Draft updated.");
    }

    private void submitApplication() throws ValidationException, SQLException {
        System.out.println("--- Submit application ---");
        LoanApplication a = findByNumberOrReport(ask("Application number: "));
        if (a == null) {
            return;
        }
        String confirm = ask("Submit " + a.getApplicationNumber() + " now? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Cancelled.");
            return;
        }

        LoanApplication r = applications.submit(a.getApplicationId(), USER);
        System.out.println("Result      : " + r.getStatus());
        System.out.println("Interest    : " + r.getAnnualInterestRate() + "% p.a.");
        System.out.println("Proposed EMI: " + AmountFormatter.format(r.getProposedEmi()));
        System.out.println("FOIR        : " + r.getFoirPercentage() + "%");
        System.out.println("Eligibility : " + r.getEligibilityReasons());
    }

    private void viewDetails() throws SQLException {
        System.out.println("--- Application details ---");
        LoanApplication a = findByNumberOrReport(ask("Application number: "));
        if (a == null) {
            return;
        }
        Customer c = customers.findById(a.getCustomerId());

        System.out.println("Application no : " + a.getApplicationNumber());
        System.out.println("Customer       : " + a.getCustomerId()
                + (c == null ? "" : " (" + c.getName() + ")"));
        System.out.println("Product        : " + a.getProduct());
        System.out.println("Branch         : " + a.getBranchCode() + " - " + a.getBranchName());
        System.out.println("Amount         : " + AmountFormatter.format(a.getRequestedAmount()));
        System.out.println("Tenure         : " + a.getTenureMonths() + " months");
        System.out.println("Monthly income : " + AmountFormatter.format(a.getMonthlyIncome()));
        System.out.println("Obligations    : "
                + AmountFormatter.format(a.getExistingMonthlyObligations()));
        System.out.println("Interest rate  : " + dash(a.getAnnualInterestRate()));
        System.out.println("Proposed EMI   : " + (a.getProposedEmi() == null
                ? "-" : AmountFormatter.format(a.getProposedEmi())));
        System.out.println("FOIR           : " + dash(a.getFoirPercentage()));
        System.out.println("Eligible       : " + dash(a.getEligible()));
        System.out.println("Reasons        : " + dash(a.getEligibilityReasons()));
        System.out.println("Status         : " + a.getStatus());
        System.out.println("Created at     : " + a.getCreatedAt());
        System.out.println("Submitted at   : " + dash(a.getSubmittedAt()));
        System.out.println("Decided at     : " + dash(a.getDecidedAt()));
        System.out.println("Reviewer       : " + dash(a.getReviewerCode()));
        System.out.println("Decision reason: " + dash(a.getDecisionReason()));
        System.out.println("-------------------------------");
    }

    private void viewHistory() throws SQLException {
        System.out.println("--- Application history ---");
        LoanApplication a = findByNumberOrReport(ask("Application number: "));
        if (a == null) {
            return;
        }
        List<HistoryDao.HistoryEntry> entries = applications.history(a.getApplicationId());
        if (entries.isEmpty()) {
            System.out.println("No history found.");
            return;
        }
        System.out.println("History of " + a.getApplicationNumber() + ":");
        for (HistoryDao.HistoryEntry h : entries) {
            System.out.println(h.performedAt() + " | " + h.action()
                    + " | " + dash(h.oldStatus()) + " -> " + dash(h.newStatus())
                    + " | by " + h.performedBy()
                    + " | " + dash(h.remarks()));
        }
    }

    private void listByCustomer() throws SQLException {
        System.out.println("--- Applications of a customer ---");
        int customerId = Integer.parseInt(ask("Customer id: "));
        List<LoanApplication> list = applications.findByCustomer(customerId);
        if (list.isEmpty()) {
            System.out.println("No applications found for this customer.");
            return;
        }
        for (LoanApplication a : list) {
            System.out.println(a.getApplicationNumber() + " | " + a.getProduct()
                    + " | " + AmountFormatter.format(a.getRequestedAmount())
                    + " | " + a.getTenureMonths() + " months | " + a.getStatus());
        }
    }

    // ------------------------------------------------------------------ helpers

    private LoanApplication findByNumberOrReport(String number) throws SQLException {
        LoanApplication a = applications.findByNumber(number);
        if (a == null) {
            System.out.println("No application found with number " + number);
        }
        return a;
    }

    private String ask(String label) {
        System.out.print(label);
        return in.nextLine().trim();
    }

    private BigDecimal askDecimal(String label, BigDecimal current) {
        String s = ask(label + " [" + current.toPlainString() + "]: ");
        return s.isEmpty() ? current : new BigDecimal(s);
    }

    private int askInt(String label, int current) {
        String s = ask(label + " [" + current + "]: ");
        return s.isEmpty() ? current : Integer.parseInt(s);
    }

    private String dash(Object value) {
        return value == null ? "-" : value.toString();
    }

    private void printErrors(String heading, ValidationException e) {
        System.out.println(heading);
        for (String error : e.getErrors()) {
            System.out.println("  - " + error);
        }
    }
}