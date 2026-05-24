package com.estatehub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * SellerRequest DTO - migrated from Seller.seller_Residential() in Customer.java
 *
 * Preserves ALL fields from insertResidentialData stored procedure call:
 *   {call insertResidentialData(?,?,?,?,?,?,?,?,?,?,?,?)}
 *   cityName, rName, rAddress, rPrice, rArea, rFacility,
 *   imageBlob, rPath, rExtention, sellerName, contact, address
 *
 * Preserves brokerage check:
 *   totalPrice = rPrice * 0.015
 *   if (brokerage == totalPrice) → "property is committed"
 *   else → "please enter valid brokerage"
 *   if (brokerage != totalPrice again) → "Brokerage mismatch again. Exiting..."
 *
 * Preserves property name choices (from switch):
 *   1.flat / 2.bungalow / 3.tenement / 4.villa / 5.raw-house
 *
 * Preserves rExtention = rPath.substring(rPath.lastIndexOf("."))
 */
@Data
public class SellerRequest {

    // From Seller.seller_Residential(): cityName parameter
    @NotBlank(message = "City is required")
    private String cityName;

    // From Seller switch: 1=flat, 2=bungalow, 3=tenement, 4=villa, 5=raw-house
    @NotBlank(message = "Property type is required")
    private String propertyType;

    // From Seller: "Enter property area"
    @NotBlank(message = "Area is required")
    private String area;

    // From Seller: "Enter property price"
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    // From Seller: "Enter property address"
    @NotBlank(message = "Address is required")
    private String propertyAddress;

    // From Seller: "Enter facilities (comma separated)"
    @NotBlank(message = "Facilities are required")
    private String facilities;

    // From Seller: "enter your present address"
    @NotBlank(message = "Seller address is required")
    private String sellerAddress;

    // Image file upload — replaces FileInputStream imageStream1
    private MultipartFile imageFile;

    // Brokerage submitted — must equal price * 0.015
    // Preserved from: if (brokerage == totalPrice)
    @NotNull(message = "Brokerage is required")
    private Double brokerage;

    // Property category: Residential / Commercial / Industrial
    private String propertyCategory;
}
