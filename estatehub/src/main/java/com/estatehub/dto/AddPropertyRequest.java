package com.estatehub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * AddPropertyRequest - migrated from Admin.addProperty()
 *
 * Preserves ALL fields from Admin.addProperty() INSERT:
 *   INSERT INTO properties (rName, rArea, rPath, rExtention, rFacility, rAddress, rPrice)
 *
 * Preserves area formatting: area1 + " sq ft." — from Admin.addProperty()
 * Preserves InputMismatchException handling for area and price
 */
@Data
public class AddPropertyRequest {

    @NotBlank(message = "Property name is required")
    private String name;

    // area1 + " sq ft." — from Admin.addProperty()
    @NotNull(message = "Area is required")
    private Double area;

    @NotBlank(message = "Facility is required")
    private String facility;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private Double price;

    private MultipartFile imageFile;

    private String cityName;
    private String propertyCategory;
}
