package com.estatehub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * RegisterRequest DTO - migrated from RealEstate.register()
 *
 * Preserves ALL validation logic from RealEstate.register():
 *   - fn: first name
 *   - ln: last name
 *   - name = fn.concat(ln)
 *   - emailID.endsWith("@gmail.com") → validated by @Email + @Pattern
 *   - mobileNumber.matches("^[6-9]\\d{9}$") → validated by @Pattern
 */
@Data
public class RegisterRequest {

    // From RealEstate.register(): fn = sc.next().trim()
    @NotBlank(message = "First name is required")
    private String firstName;

    // From RealEstate.register(): ln = sc.next().trim()
    @NotBlank(message = "Last name is required")
    private String lastName;

    // From RealEstate.register() and main(): flag = emailID.endsWith("@gmail.com")
    @NotBlank(message = "Email is required")
    @Pattern(regexp = ".*@gmail\\.com$", message = "Email must end with @gmail.com")
    private String email;

    // From RealEstate.register(): mobileNumber.matches("^[6-9]\\d{9}$")
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number. Must start with 6-9 and be 10 digits.")
    private String mobileNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
