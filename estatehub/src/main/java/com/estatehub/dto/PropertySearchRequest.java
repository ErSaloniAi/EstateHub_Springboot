package com.estatehub.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * PropertySearchRequest - migrated from dbms.SearchPropertiesByBudget()
 * and Buyer menu option 2: "Search Property by Budget"
 *
 * Preserves: city + maxPrice filter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertySearchRequest {
    private String cityName;
    private String propertyType;
    private String propertyCategory;
    private Double maxPrice;
    private Double minPrice;
}
