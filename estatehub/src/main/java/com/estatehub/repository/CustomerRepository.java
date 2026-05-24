package com.estatehub.repository;

import com.estatehub.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CustomerRepository - migrated from RealEstate.java and Loan.createLoan()
 *
 * Preserves queries:
 *   login():       SELECT cEmail FROM customer → findBycEmail()
 *   Loan:          SELECT cName FROM customer WHERE cEmail = ?
 *   Loan:          SELECT customerId FROM customer WHERE cName = ?
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // RealEstate.login(): check cEmail exists
    Optional<Customer> findBycEmail(String cEmail);

    boolean existsBycEmail(String cEmail);

    // Loan.createLoan(): SELECT cName FROM customer WHERE cEmail = ?
    @Query("SELECT c.cName FROM Customer c WHERE c.cEmail = :email")
    Optional<String> findNameByEmail(@Param("email") String email);

    // Loan.createLoan(): SELECT customerId FROM customer WHERE cName = ?
    @Query("SELECT c.customerId FROM Customer c WHERE c.cName = :name")
    Optional<Integer> findCustomerIdByName(@Param("name") String name);

    // Find by OTP (email verification)
    Optional<Customer> findByOtp(String otp);

    // Find by reset token (password reset)
    Optional<Customer> findByResetToken(String token);
}
