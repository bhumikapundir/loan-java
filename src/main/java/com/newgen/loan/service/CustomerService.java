package com.newgen.loan.service;

import com.newgen.loan.config.BusinessClock;
import com.newgen.loan.config.ProductRules;
import com.newgen.loan.dao.CustomerDao;
import com.newgen.loan.exception.ValidationException;
import com.newgen.loan.model.Customer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    private final CustomerDao dao = new CustomerDao();

    public List<String> validate(Customer c) throws SQLException {
        List<String> errors = new ArrayList<>();

        if (c.getName() == null || c.getName().isBlank()) {
            errors.add("Name is required");
        }

        LocalDate dob = c.getDateOfBirth();
        if (dob == null) {
            errors.add("Date of birth is required");
        } else if (dob.isAfter(BusinessClock.today())) {
            errors.add("Date of birth cannot be in the future");
        }

        String mobile = c.getMobile();
        if (mobile == null || !mobile.matches("\\d{10}")) {
            errors.add("Mobile must be exactly 10 digits");
        } else {
            Customer existing = dao.findByMobile(mobile);
            if (existing != null && existing.getCustomerId() != c.getCustomerId()) {
                errors.add("Mobile already registered to customer id "
                        + existing.getCustomerId());
            }
        }
        return errors;
    }

    public int create(Customer c) throws ValidationException, SQLException {
        List<String> errors = validate(c);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return dao.save(c);
    }
        public Customer findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public Customer findByMobile(String mobile) throws SQLException {
        return dao.findByMobile(mobile);
    }

    public List<Customer> searchByName(String namePart) throws SQLException {
        return dao.searchByName(namePart);
    }
}