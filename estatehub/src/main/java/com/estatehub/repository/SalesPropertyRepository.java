package com.estatehub.repository;

import com.estatehub.entity.SalesProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * SalesPropertyRepository - migrated from Admin.view_properties()
 * Mirrors: SELECT * FROM sales_property / property_log
 */
@Repository
public interface SalesPropertyRepository extends JpaRepository<SalesProperty, Integer> {
    // Admin.view_properties("sold")
    List<SalesProperty> findBySaleType(String saleType);

    // By customer
    List<SalesProperty> findByCustomerId(Integer customerId);
}
