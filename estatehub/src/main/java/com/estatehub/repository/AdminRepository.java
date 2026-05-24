package com.estatehub.repository;

import com.estatehub.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * AdminRepository - migrated from Admin.java
 * Admin.connectAsAdmin() checks: RealEstate.emailID.equalsIgnoreCase(ad.email) && password.equals(ad.password)
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByaEmailId(String email);
    boolean existsByaEmailId(String email);
}
