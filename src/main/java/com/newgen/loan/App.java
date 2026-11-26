package com.newgen.loan;

import com.newgen.loan.config.BusinessClock;
import com.newgen.loan.dao.CustomerDao;
import com.newgen.loan.model.Customer;
import com.newgen.loan.model.LoanApplication;
import com.newgen.loan.model.Product;
import com.newgen.loan.service.ApplicationService;
import com.newgen.loan.util.AmountFormatter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

public class App {

    static final ApplicationService service = new ApplicationService();
    static final CustomerDao customerDao = new CustomerDao();
    static int counter = 0;

    static LoanApplication newDraft(LocalDate dob, Product product, String amount,
                                    int tenure, String income, String obligations)
            throws Exception {
        counter++;
        String mobile = String.format("9%09d",
                Math.floorMod(System.nanoTime() + counter, 1_000_000_000L));
        Customer c = new Customer("Test Customer " + counter, dob, mobile, "Test address", "KYC");
        customerDao.save(c);

        LoanApplication a = new LoanApplication();
        a.setCustomerId(c.getCustomerId());
        a.setProduct(product);
        a.setBranchCode("BR001");
        a.setBranchName("Meerut Main");
        a.setRequestedAmount(new BigDecimal(amount));
        a.setTenureMonths(tenure);
        a.setMonthlyIncome(new BigDecimal(income));
        a.setExistingMonthlyObligations(new BigDecimal(obligations));
        return service.createDraft(a, "CLERK01");
    }

    static void run(String label, LocalDate dob, Product product, String amount,
                    int tenure, String income, String obligations) throws Exception {
        LoanApplication draft = newDraft(dob, product, amount, tenure, income, obligations);
        LoanApplication r = service.submit(draft.getApplicationId(), "CLERK01");
        System.out.println(label + " -> " + r.getStatus()
                + " | EMI " + AmountFormatter.format(r.getProposedEmi())
                + " | FOIR " + r.getFoirPercentage() + "%");
        if (!r.getEligible()) {
            System.out.println("      reasons: " + r.getEligibilityReasons());
        }
    }

    public static void main(String[] args) throws Exception {
        LocalDate today = BusinessClock.today();
        LocalDate age30 = today.minusYears(30);
        Product p = Product.PERSONAL;

        run("A1 worked example pass ", age30, p, "100000", 12, "30000", "5000");
        run("A2 worked example fail ", age30, p, "100000", 12, "30000", "7000");
        run("B1 FOIR exactly 50     ", age30, p, "100000", 12, "20000", "1115.12");
        run("B2 FOIR just above 50  ", age30, p, "100000", 12, "20000", "1120");
        run("C1 amount min 50000    ", age30, p, "50000", 12, "30000", "0");
        run("C2 amount 49999        ", age30, p, "49999", 12, "30000", "0");
        run("C3 amount max 500000   ", age30, p, "500000", 60, "30000", "0");
        run("C4 amount 500001       ", age30, p, "500001", 60, "30000", "0");
        run("C5 tenure 11           ", age30, p, "100000", 11, "30000", "0");
        run("C6 tenure 61           ", age30, p, "100000", 61, "30000", "0");
        run("C7 income 19999        ", age30, p, "100000", 12, "19999", "0");
        run("D1 age 20 (1 day short)", today.minusYears(21).plusDays(1), p, "100000", 12, "30000", "0");
        run("D2 age 21              ", today.minusYears(21), p, "100000", 12, "30000", "0");
        run("D3 age 60              ", today.minusYears(60), p, "100000", 12, "30000", "0");
        run("D4 age 61              ", today.minusYears(61), p, "100000", 12, "30000", "0");
        run("E1 many failures       ", today.minusYears(65), p, "10000", 6, "5000", "9000");

        System.out.println();
        System.out.println("--- Rollback test ---");
        LoanApplication d = newDraft(age30, p, "100000", 12, "30000", "5000");
        service.setSimulateFailure(true);
        try {
            service.submit(d.getApplicationId(), "CLERK01");
        } catch (SQLException e) {
            System.out.println("Submit failed on purpose: " + e.getMessage());
        }
        service.setSimulateFailure(false);
        LoanApplication after = service.findById(d.getApplicationId());
        System.out.println("Status after failure : " + after.getStatus()
                + " | EMI " + after.getProposedEmi()
                + " | submitted_at " + after.getSubmittedAt());
        System.out.println("History rows         : " + service.history(d.getApplicationId()).size());

        service.submit(d.getApplicationId(), "CLERK01");
        System.out.println("After normal retry   : "
                + service.findById(d.getApplicationId()).getStatus());
        System.out.println("History rows         : " + service.history(d.getApplicationId()).size());

        System.out.println();
        System.out.println("--- Submit again (invalid transition) ---");
        try {
            service.submit(d.getApplicationId(), "CLERK01");
        } catch (Exception e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }
}