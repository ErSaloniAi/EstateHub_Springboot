package com.estatehub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * CustomerDetailsRequest - migrated from Customer.customer_details()
 *
 * Preserves ALL fields from INSERT customer SQL:
 *   INSERT INTO customer (cName, cPhone, cEmail, cAddress, cCity, cState,
 *                         cAge, cOccupation, cIncome, type)
 *
 * Preserves validations:
 *   - state defaults to "Gujarat" if empty
 *   - age defaults to 25 if <=0
 *   - income = 0.0 if invalid
 */
@Data
public class CustomerDetailsRequest {

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    // Defaults to "Gujarat" if empty — preserved from Customer.customer_details()
    private String state;

    // Defaults to 25 if <=0 — preserved from Customer.customer_details()
    private Integer age;

    @NotBlank(message = "Occupation is required")
    private String occupation;

    // Defaults to 0.0 if invalid — preserved from Customer.customer_details()
    private Double income;

    // "buyer" or "seller" — from Customer.java: type field
    private String type;

    // City chosen for property — from Customer.chooseCity()
    private String cityName;
}
