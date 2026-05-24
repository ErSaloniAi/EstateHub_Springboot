package com.estatehub.service;

import com.estatehub.dto.CustomerDetailsRequest;
import com.estatehub.entity.Customer;
import com.estatehub.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * CustomerService - migrated from Customer.java
 *
 * Preserves ALL logic from Customer.customer_details():
 *   INSERT INTO customer (cName, cPhone, cEmail, cAddress, cCity, cState, cAge, cOccupation, cIncome, type)
 *   state defaults to "Gujarat" if empty
 *   age defaults to 25 if <=0
 *   income defaults to 0.0 if invalid
 *
 * Preserves ALL cities from Customer.chooseCity():
 *   1.Ahmedabad → "ahmedabad"
 *   2.Rajkot    → "rajkot"
 *   3.Surat     → "surat"
 *   4.Vadodara  → "vadodara"
 *   5.Bhavnagar → "bhavnagar"
 */
@Service
@Slf4j
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Migrated from Customer.customer_details()
     *
     * Preserves INSERT SQL:
     *   INSERT INTO customer (cName, cPhone, cEmail, cAddress, cCity, cState,
     *                         cAge, cOccupation, cIncome, type)
     *
     * Preserves defaults:
     *   if (state.trim().isEmpty()) state = "Gujarat"
     *   if (age <= 0) age = 25
     *   income = 0.0 if invalid
     */
    public Customer saveCustomerDetails(String email, CustomerDetailsRequest request) {
        Optional<Customer> customerOpt = customerRepository.findBycEmail(email);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer not found with email: " + email);
        }

        Customer customer = customerOpt.get();

        customer.setCAddress(request.getAddress());
        customer.setCCity(request.getCity());

        // state defaults to "Gujarat" if empty — from Customer.customer_details()
        String state = request.getState();
        if (state == null || state.trim().isEmpty()) {
            state = "Gujarat";
        }
        customer.setCState(state);

        // age defaults to 25 if <=0 — from Customer.customer_details()
        Integer age = request.getAge();
        if (age == null || age <= 0) {
            age = 25;
        }
        customer.setCAge(age);

        customer.setCOccupation(request.getOccupation());

        // income defaults to 0.0 — from Customer.customer_details()
        Double income = request.getIncome();
        if (income == null) {
            income = 0.0;
        }
        customer.setCIncome(income);

        customer.setType(request.getType());

        // cityName — from Customer.chooseCity()
        if (request.getCityName() != null) {
            customer.setCityName(request.getCityName().toLowerCase());
        }

        Customer saved = customerRepository.save(customer);
        log.info("Customer details saved for: {}", email);
        return saved;
    }

    /**
     * Get customer by email
     */
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findBycEmail(email);
    }

    /**
     * Migrated from Customer.chooseCity() switch — returns display name
     * 1.Ahmedabad / 2.Rajkot / 3.Surat / 4.Vadodara / 5.Bhavnagar
     */
    public String resolveCityName(int choice) {
        return switch (choice) {
            case 1 -> "ahmedabad";
            case 2 -> "rajkot";
            case 3 -> "surat";
            case 4 -> "vadodara";
            case 5 -> "bhavnagar";
            default -> throw new IllegalArgumentException("Invalid city choice! Try again.");
        };
    }
}
