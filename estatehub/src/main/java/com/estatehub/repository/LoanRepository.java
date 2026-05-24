package com.estatehub.repository;

import com.estatehub.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * LoanRepository - migrated from Loan.createLoan() in Transaction.java
 * Mirrors Admin.viewLoanDetail(): SELECT * FROM loan
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {
    List<Loan> findByCustomerId(Integer customerId);
    List<Loan> findByPropertyId(Integer propertyId);
}
