package com.estatehub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * LoanRequest DTO - migrated from Loan.createLoan() in Transaction.java
 *
 * Preserves ALL 3 bank choices:
 *   1. Saurastra bank → 8.75%
 *   2. HDFC bank      → 9.75%
 *   3. Karnavati bank → 8.95%
 *
 * Preserves brokerage: totalAmount = loanAmount * 1.015
 *
 * Preserves EMI formula:
 *   monthlyRate = (interestRate / 100) / 12
 *   emiAmount = (totalAmount * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths))
 *               / (Math.pow(1 + monthlyRate, tenureMonths) - 1)
 */
@Data
public class LoanRequest {

    // 1=Saurastra(8.75%), 2=HDFC(9.75%), 3=Karnavati(8.95%)
    @NotNull(message = "Bank choice is required")
    @Min(value = 1, message = "Choose bank 1, 2, or 3")
    @Max(value = 3, message = "Choose bank 1, 2, or 3")
    private Integer bankChoice;

    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    private Integer tenureMonths;

    // The property being purchased (dbms.id)
    @NotNull(message = "Property ID is required")
    private Integer propertyId;

    // The city of the property
    @NotBlank(message = "City name is required")
    private String cityName;
}
