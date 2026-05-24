package com.estatehub.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Loan entity - migrated from Transaction.java (Loan class)
 *
 * Preserves ALL fields from INSERT loan SQL in Loan.createLoan():
 *   INSERT INTO loan (bankName, customerId, propertyId, loanAmount, interestRate,
 *                     tenureMonths, emiAmount, loanStatus, sanctionDate,
 *                     disbursementDate, repaymentStartDate)
 *
 * Preserves ALL 3 banks and their interest rates from Loan.createLoan():
 *   "Saurastra bank" → 8.75%
 *   "HDFC bank"      → 9.75%
 *   "Karnavati bank" → 8.95%
 *
 * Preserves brokerage calculation from Loan.createLoan():
 *   totalAmount = loanAmount * 1.015   (1.5% brokerage)
 *
 * Preserves EMI formula from Loan.createLoan():
 *   monthlyRate = (interestRate / 100) / 12
 *   emiAmount = (totalAmount * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths))
 *               / (Math.pow(1 + monthlyRate, tenureMonths) - 1)
 */
@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loanId")
    private Integer loanId;

    // From Admin.viewLoanDetail(): bankName
    @Column(name = "bankName", length = 100)
    private String bankName;

    // From Admin.viewLoanDetail(): customerId
    @Column(name = "customerId")
    private Integer customerId;

    // From Admin.viewLoanDetail(): propertyId = dbms.id
    @Column(name = "propertyId")
    private Integer propertyId;

    // From Admin.viewLoanDetail(): loanAmount = dbms.price
    @Column(name = "loanAmount")
    private Double loanAmount;

    // Interest rate: 8.75, 9.75, or 8.95
    @Column(name = "interestRate")
    private Double interestRate;

    // From Admin.viewLoanDetail(): tenureMonths
    @Column(name = "tenureMonths")
    private Integer tenureMonths;

    // Computed by EMI formula from Loan.createLoan()
    @Column(name = "emiAmount")
    private Double emiAmount;

    // From Admin.viewLoanDetail(): loanStatus — default "Pending"
    @Column(name = "loanStatus", length = 20)
    private String loanStatus;

    // From Admin.viewLoanDetail(): sanctionDate = today
    @Column(name = "sanctionDate")
    private LocalDate sanctionDate;

    // From Admin.viewLoanDetail(): disbursementDate = today
    @Column(name = "disbursementDate")
    private LocalDate disbursementDate;

    // From Admin.viewLoanDetail(): repaymentStartDate = today
    @Column(name = "repaymentStartDate")
    private LocalDate repaymentStartDate;

    // From Admin.viewLoanDetail(): createdAt
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    // From Admin.viewLoanDetail(): updatedAt
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    // Total amount including brokerage: loanAmount * 1.015 — preserved from Loan.createLoan()
    @Column(name = "totalAmount")
    private Double totalAmount;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.loanStatus == null) {
            this.loanStatus = "Pending";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
