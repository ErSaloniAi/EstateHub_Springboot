package com.estatehub.repository;

import com.estatehub.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * PropertyRepository - migrated from dbms.java and Admin.java
 *
 * Preserves all query patterns from dbms.Sales_Data():
 *   SELECT rId, rName, rAddress, rPrice, rArea, rFacility, rPath
 *   FROM tableName WHERE rName LIKE ?   (propertyName + "%")
 *
 * Preserves property type check from dbms.Sales_Data():
 *   SELECT rName FROM tableName WHERE rId = ?
 *   if (!actualType.toLowerCase().startsWith(propertyName.toLowerCase()))
 *
 * Preserves dbms.SearchPropertiesByBudget city + maxPrice filter
 *
 * Preserves dbms.LowestPriceDynamicFetcher (cheapest per city)
 *
 * Preserves dbms.countPropertiesByCity (COUNT per city)
 */
@Repository
public interface PropertyRepository extends JpaRepository<Property, Integer> {

    // dbms.Sales_Data(): WHERE rName LIKE propertyName + "%"
    List<Property> findByCityNameIgnoreCaseAndRNameStartingWithIgnoreCase(String cityName, String rName);

    // dbms.Sales_Data(): SELECT by rId
    Optional<Property> findByRId(Integer rId);

    // dbms.countPropertiesByCity: COUNT by city
    long countByCityNameIgnoreCase(String cityName);

    // dbms.SearchPropertiesByBudget: city + maxPrice
    List<Property> findByCityNameIgnoreCaseAndRPriceLessThanEqual(String cityName, Double maxPrice);

    // Admin.view_properties("sold") / "commited"
    List<Property> findByStatus(String status);

    // All by city
    List<Property> findByCityNameIgnoreCase(String cityName);

    // All by category (Residential/Commercial/Industrial)
    List<Property> findByPropertyCategoryIgnoreCase(String category);

    // City + category
    List<Property> findByCityNameIgnoreCaseAndPropertyCategoryIgnoreCase(String cityName, String category);

    // dbms.LowestPriceDynamicFetcher: cheapest per city
    @Query("SELECT p FROM Property p WHERE p.cityName = :cityName AND p.status = 'available' ORDER BY p.rPrice ASC")
    List<Property> findCheapestByCity(@Param("cityName") String cityName);

    // dbms.ShowAllPropertiesPrices: all available properties with price
    @Query("SELECT p FROM Property p WHERE p.status = 'available' ORDER BY p.cityName, p.rPrice ASC")
    List<Property> findAllAvailableOrderByPrice();

    // GetCustomerContact (admin function): find property owner contact by rId
    @Query("SELECT p.ownerContact FROM Property p WHERE p.rId = :rId")
    Optional<Long> findOwnerContactByRId(@Param("rId") Integer rId);
}
