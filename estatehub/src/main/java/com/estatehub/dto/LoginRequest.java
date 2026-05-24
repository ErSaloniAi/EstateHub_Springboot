package com.estatehub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank
    @Pattern(regexp = ".*@gmail\\.com$", message = "Email must end with @gmail.com")
    private String email;

    @NotBlank
    private String password;
}
