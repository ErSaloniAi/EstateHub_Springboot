package com.estatehub.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Customer entity - migrated from Customer.java
 * Preserves ALL fields from customer_details() INSERT SQL:
 *   INSERT INTO customer (cName, cPhone, cEmail, cAddress, cCity, cState,
 *                         cAge, cOccupation, cIncome, type)
 * Also stores authentication fields from RealEstate.java (fn, ln, name, emailID, mobileNumber)
 */
@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerId")
    private Integer customerId;

    // From RealEstate.java: fn (first name)
    @Column(name = "cFirstName", length = 100)
    private String firstName;

    // From RealEstate.java: ln (last name)
    @Column(name = "cLastName", length = 100)
    private String lastName;

    // From RealEstate.java: name = fn.concat(ln)
    @Column(name = "cName", length = 200)
    private String cName;

    // From RealEstate.java: mobileNumber — validated by regex "^[6-9]\\d{9}$"
    @Column(name = "cPhone", length = 15)
    private String cPhone;

    // From RealEstate.java: emailID — must end with @gmail.com
    @Column(name = "cEmail", unique = true, length = 200)
    private String cEmail;

    // From Customer.customer_details(): address
    @Column(name = "cAddress", length = 300)
    private String cAddress;

    // From Customer.customer_details(): city
    @Column(name = "cCity", length = 100)
    private String cCity;

    // From Customer.customer_details(): state — defaults to "Gujarat" if empty
    @Column(name = "cState", length = 100)
    private String cState;

    // From Customer.customer_details(): age — defaults to 25 if <=0
    @Column(name = "cAge")
    private Integer cAge;

    // From Customer.customer_details(): occupation
    @Column(name = "cOccupation", length = 100)
    private String cOccupation;

    // From Customer.customer_details(): income
    @Column(name = "cIncome")
    private Double cIncome;

    // From Customer.java: type = "buyer" or "seller"
    @Column(name = "type", length = 20)
    private String type;

    // Auth: encrypted password
    @Column(name = "password", length = 200)
    private String password;

    // Auth: role — ROLE_USER or ROLE_ADMIN
    @Column(name = "role", length = 20)
    private String role;

    // Auth: email verified flag
    @Column(name = "emailVerified")
    private Boolean emailVerified = false;

    // Auth: OTP for verification
    @Column(name = "otp", length = 10)
    private String otp;

    // Auth: OTP expiry
    @Column(name = "otpExpiry")
    private LocalDateTime otpExpiry;

    // Auth: password reset token
    @Column(name = "resetToken", length = 200)
    private String resetToken;

    // Auth: reset token expiry
    @Column(name = "resetTokenExpiry")
    private LocalDateTime resetTokenExpiry;

    // From Customer.chooseCity(): the city chosen for property search
    @Column(name = "cityName", length = 50)
    private String cityName;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        // Default state to "Gujarat" if empty — from Customer.customer_details()
        if (this.cState == null || this.cState.trim().isEmpty()) {
            this.cState = "Gujarat";
        }
        // Default age to 25 if <=0 — from Customer.customer_details()
        if (this.cAge == null || this.cAge <= 0) {
            this.cAge = 25;
        }
        if (this.emailVerified == null) {
            this.emailVerified = false;
        }
    }
}
