package com.estatehub.repository;

import com.estatehub.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * CityRepository - migrated from City class and SLL (Ds.java)
 * SLL.search(): search by city name
 */
@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    Optional<City> findByCityNameIgnoreCase(String cityName);
}
