package com.estatehub.service;

import com.estatehub.dto.LoanRequest;
import com.estatehub.entity.Loan;
import com.estatehub.entity.Property;
import com.estatehub.repository.CustomerRepository;
import com.estatehub.repository.LoanRepository;
import com.estatehub.repository.PropertyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * LoanService - migrated from Transaction.java (Loan class)
 *
 * ZERO business logic changed. ALL calculations preserved:
 *
 * Bank selection from Loan.createLoan():
 *   case 1 → bankName = "Saurastra bank", interestRate = 8.75
 *   case 2 → bankName = "HDFC bank",      interestRate = 9.75
 *   case 3 → bankName = "Karnavati bank", interestRate = 8.95
 *   default → "Invalid choice! Loan process cancelled."
 *
 * Brokerage from Loan.createLoan():
 *   totalAmount = loanAmount * 1.015   (1.5% brokerage)
 *   "Your total payment (including 1.5% brokerage): "
 *
 * EMI formula from Loan.createLoan():
 *   monthlyRate = (interestRate / 100) / 12
 *   emiAmount = (totalAmount * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths))
 *               / (Math.pow(1 + monthlyRate, tenureMonths) - 1)
 *
 * Dates all set to today — from Loan.createLoan():
 *   java.sql.Date today = new java.sql.Date(System.currentTimeMillis())
 *   sanctionDate = today, disbursementDate = today, repaymentStartDate = today
 *
 * Loan file content from Loan.createLoan():
 *   "================ Bank Loan Details ================"
 *   "Bank: %s | Loan Amount: %f | EMI: %f"
 */
@Service
@Slf4j
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Migrated from Loan.createLoan()
     * All logic preserved exactly.
     */
    public Loan createLoan(LoanRequest request, String customerEmail) {

        // Bank selection — from Loan.createLoan() switch
        String bankName;
        double interestRate;

        switch (request.getBankChoice()) {
            case 1 -> {
                // case 1: bankName = "Saurastra bank"; interestRate = 8.75;
                bankName = "Saurastra bank";
                interestRate = 8.75;
            }
            case 2 -> {
                // case 2: bankName = "HDFC bank"; interestRate = 9.75;
                bankName = "HDFC bank";
                interestRate = 9.75;
            }
            case 3 -> {
                // case 3: bankName = "Karnavati bank"; interestRate = 8.95;
                bankName = "Karnavati bank";
                interestRate = 8.95;
            }
            default -> {
                // default: "Invalid choice! Loan process cancelled."
                throw new IllegalArgumentException("Invalid choice! Loan process cancelled.");
            }
        }

        // Get property price — mirrors: double loanAmount = dbms.price
        Optional<Property> propertyOpt = propertyRepository.findByRId(request.getPropertyId());
        if (propertyOpt.isEmpty()) {
            throw new IllegalArgumentException("Property not found.");
        }
        double loanAmount = propertyOpt.get().getRPrice();

        // Get customerId — from Loan.createLoan():
        //   SELECT customerId FROM customer WHERE cName = ?
        Optional<String> nameOpt = customerRepository.findNameByEmail(customerEmail);
        if (nameOpt.isEmpty()) {
            throw new IllegalArgumentException("No customer found with email: " + customerEmail);
        }
        Optional<Integer> customerIdOpt = customerRepository.findCustomerIdByName(nameOpt.get());
        if (customerIdOpt.isEmpty()) {
            throw new IllegalArgumentException("No customer found with name: " + nameOpt.get());
        }
        int customerId = customerIdOpt.get();

        // Brokerage — from Loan.createLoan():
        // System.out.println("Your total payment (including 1.5% brokerage):");
        // double totalAmount = loanAmount * 1.015;
        double totalAmount = loanAmount * 1.015;

        int tenureMonths = request.getTenureMonths();

        // EMI formula — from Loan.createLoan() — PRESERVED EXACTLY
        double monthlyRate = (interestRate / 100) / 12;
        double emiAmount = (totalAmount * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths))
                / (Math.pow(1 + monthlyRate, tenureMonths) - 1);

        // Date = today — from Loan.createLoan():
        // java.sql.Date today = new java.sql.Date(System.currentTimeMillis())
        LocalDate today = LocalDate.now();

        // Build loan entity
        Loan loan = new Loan();
        loan.setBankName(bankName);
        loan.setCustomerId(customerId);
        loan.setPropertyId(request.getPropertyId());
        loan.setLoanAmount(loanAmount);
        loan.setInterestRate(interestRate);
        loan.setTenureMonths(tenureMonths);
        loan.setEmiAmount(emiAmount);
        loan.setTotalAmount(totalAmount);
        loan.setLoanStatus("Pending");         // from Loan.createLoan(): loanStatus = "Pending"
        loan.setSanctionDate(today);           // from Loan.createLoan(): sanctionDate = today
        loan.setDisbursementDate(today);       // from Loan.createLoan(): disbursementDate = today
        loan.setRepaymentStartDate(today);     // from Loan.createLoan(): repaymentStartDate = today

        Loan saved = loanRepository.save(loan);

        // Loan confirmation output — from Loan.createLoan():
        // "Loan created successfully for Customer ID: %d and Property ID: %d"
        // "Bank: %s | Loan Amount: %f | EMI: %f"
        log.info("Loan created successfully for Customer ID: {} and Property ID: {}", customerId, request.getPropertyId());
        log.info("Bank: {} | Loan Amount: {} | EMI: {}", bankName, loanAmount, emiAmount);

        // Send email — replaces file writing from Loan.createLoan():
        // "================ Bank Loan Details ================"
        // "Bank: " + bankName + " | Loan Amount: " + loanAmount + " | EMI: " + emiAmount
        emailService.sendLoanConfirmationEmail(customerEmail, bankName, loanAmount, emiAmount, tenureMonths);

        return saved;
    }

    /**
     * Admin.viewLoanDetail(): SELECT * FROM loan
     */
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getLoansByCustomer(Integer customerId) {
        return loanRepository.findByCustomerId(customerId);
    }

    /**
     * POA transaction — migrated from POA.poa() in Transaction.java
     *
     * Preserves:
     *   totalAmount = dbms.price * 1.015
     *   "Your total payment to give us (including 1.5% brokerage):"
     *   if (amount == (int) totalAmount) → "Payment successful"
     *
     * Note: POA.poa() had a bug (String Line = null → NullPointerException on fos.write).
     *       We preserve the logic intent: verify exact payment amount.
     */
    public String processPOA(Integer propertyId, double amountPaid) {
        Optional<Property> propertyOpt = propertyRepository.findByRId(propertyId);
        if (propertyOpt.isEmpty()) {
            throw new IllegalArgumentException("Property not found.");
        }

        // totalAmount = dbms.price * 1.015 — from POA.poa()
        double totalAmount = propertyOpt.get().getRPrice() * 1.015;

        // if (amount == (int) totalAmount) → "Payment successful" — from POA.poa()
        if (amountPaid == (int) totalAmount) {
            log.info("POA Payment successful for property: {}", propertyId);
            return "Payment successful";
        } else {
            return "Payment amount mismatch. Expected: " + (int) totalAmount;
        }
    }
}
