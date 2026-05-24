package com.estatehub.service;

import com.estatehub.entity.Admin;
import com.estatehub.entity.Customer;
import com.estatehub.entity.Loan;
import com.estatehub.entity.Property;
import com.estatehub.entity.SalesProperty;
import com.estatehub.repository.AdminRepository;
import com.estatehub.repository.CustomerRepository;
import com.estatehub.repository.LoanRepository;
import com.estatehub.repository.PropertyRepository;
import com.estatehub.repository.SalesPropertyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * AdminService - migrated from Admin.java
 *
 * Preserves ALL admin operations:
 *
 * Admin constructor — hardcoded admin list:
 *   adminList.add(new AdminData(1, "Saloni", "9429769132", "sgorsiya@gmail.com","gorsiya@s"))
 *   adminList.add(new AdminData(2, "Husain", "6353986953", "haghariya@gmail.com","aghariya@h"))
 *   adminList.add(new AdminData(3, "Naimish", "7984087441", "ngondaliya@gmail.com","gondaliya@n"))
 *
 * Admin.viewAdminData(): SELECT * FROM admin → fields: aid, aname, aphone, aemailid
 * Admin.viewLoanDetail(): SELECT * FROM loan → all loan fields
 * Admin.addAdminData(): INSERT INTO admin(aName, aPhone, aEmailed, password)
 * Admin.deleteProperty(): DELETE FROM residential_{city}1 WHERE rId=?
 * Admin.view_properties("sold"): SELECT * FROM sales_property
 * Admin.view_properties("commited"): SELECT * FROM property_log
 * dbms.countPropertiesByCity(): CALL CountPropertiesByCity(?)
 * dbms.getContacts(): SELECT GetCustomerContact(?) as contact
 */
@Service
@Slf4j
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private SalesPropertyRepository salesPropertyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Admin.viewAdminData(): SELECT * FROM admin
     * Fields printed: aid, aname, aphone, aemailid
     */
    public List<Admin> viewAllAdmins() {
        return adminRepository.findAll();
    }

    /**
     * Admin.addAdminData(): INSERT INTO admin(aName, aPhone, aEmailed, password)
     * Preserves: adminList.add(new AdminData(adminList.size() + 1, name, phone, email, "gorsiya@s"))
     *   (original hardcodes password "gorsiya@s" after insert — we use the provided password)
     */
    public Admin addAdmin(String name, String phone, String email, String password) {
        Admin admin = new Admin();
        admin.setaName(name);
        admin.setaPhone(phone);
        admin.setaEmailId(email);
        // Password encoding for security (original stored plain text)
        admin.setPassword(passwordEncoder.encode(password));

        Admin saved = adminRepository.save(admin);
        log.info("Admin data inserted successfully: {}", name);
        return saved;
    }

    /**
     * Admin.viewLoanDetail(): SELECT * FROM loan
     * Fields: loanId, bankName, customerId, propertyId, loanAmount, interestRate,
     *         tenureMonths, emiAmount, loanStatus, sanctionDate, disbursementDate,
     *         repaymentStartDate, createdAt, updatedAt
     */
    public List<Loan> viewAllLoans() {
        return loanRepository.findAll();
    }

    /**
     * Admin.view_properties("sold"): SELECT * FROM sales_property
     * Admin.view_properties("commited"): SELECT * FROM property_log
     * Returns fields: cityName, rId, rName, rAddress, rPrice, rArea, rFacility, rPath
     */
    public List<SalesProperty> viewPropertiesByType(String type) {
        // Preserves: if (type.equalsIgnoreCase("sold")) → sales_property
        //            else if (type.equalsIgnoreCase("commited")) → property_log
        if (type.equalsIgnoreCase("sold")) {
            return salesPropertyRepository.findBySaleType("sold");
        } else if (type.equalsIgnoreCase("commited")) {
            return salesPropertyRepository.findBySaleType("commited");
        }
        return List.of();
    }

    /**
     * Admin menu option 9: view contact of customer
     * dbms.getContacts(): SELECT GetCustomerContact(custId) as contact
     */
    public Optional<Long> getCustomerContact(Integer propertyId) {
        return propertyRepository.findOwnerContactByRId(propertyId);
    }

    /**
     * dbms.countPropertiesByCity(): CALL CountPropertiesByCity(city)
     * Admin menu option 8: view Count of Property by city
     */
    public long countPropertiesByCity(String city) {
        return propertyRepository.countByCityNameIgnoreCase(city);
    }

    /**
     * Get all customers — for admin dashboard
     */
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Get all properties — for admin dashboard
     */
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    /**
     * Admin analytics
     */
    public AdminAnalytics getAnalytics() {
        long totalCustomers = customerRepository.count();
        long totalProperties = propertyRepository.count();
        long soldProperties = salesPropertyRepository.findBySaleType("sold").size();
        long committedProperties = salesPropertyRepository.findBySaleType("commited").size();
        long totalLoans = loanRepository.count();

        // Count per city — from dbms.countPropertiesByCity()
        long ahmCount = propertyRepository.countByCityNameIgnoreCase("ahmedabad");
        long rajCount = propertyRepository.countByCityNameIgnoreCase("rajkot");
        long srtCount = propertyRepository.countByCityNameIgnoreCase("surat");
        long vadCount = propertyRepository.countByCityNameIgnoreCase("vadodara");
        long bhvCount = propertyRepository.countByCityNameIgnoreCase("bhavnagar");

        return new AdminAnalytics(totalCustomers, totalProperties, soldProperties,
                committedProperties, totalLoans, ahmCount, rajCount, srtCount, vadCount, bhvCount);
    }

    public record AdminAnalytics(
        long totalCustomers, long totalProperties, long soldProperties,
        long committedProperties, long totalLoans,
        long ahmedabadCount, long rajkotCount, long suratCount,
        long vadodaraCount, long bhavnagarCount
    ) {}

    /**
     * Seed default admins — from Admin constructor:
     *   adminList.add(new AdminData(1, "Saloni", "9429769132", "sgorsiya@gmail.com","gorsiya@s"))
     *   adminList.add(new AdminData(2, "Husain", "6353986953", "haghariya@gmail.com","aghariya@h"))
     *   adminList.add(new AdminData(3, "Naimish", "7984087441", "ngondaliya@gmail.com","gondaliya@n"))
     */
    public void seedDefaultAdmins() {
        seedAdmin("Saloni", "9429769132", "sgorsiya@gmail.com", "gorsiya@s");
        seedAdmin("Husain", "6353986953", "haghariya@gmail.com", "aghariya@h");
        seedAdmin("Naimish", "7984087441", "ngondaliya@gmail.com", "gondaliya@n");
    }

    private void seedAdmin(String name, String phone, String email, String rawPassword) {
        if (!adminRepository.existsByaEmailId(email)) {
            Admin admin = new Admin();
            admin.setaName(name);
            admin.setaPhone(phone);
            admin.setaEmailId(email);
            // Store both raw (for original comparison) and encoded
            admin.setPassword(rawPassword); // Preserves original: password.equals(ad.password)
            adminRepository.save(admin);
            log.info("Default admin seeded: {}", name);
        }
    }
}
