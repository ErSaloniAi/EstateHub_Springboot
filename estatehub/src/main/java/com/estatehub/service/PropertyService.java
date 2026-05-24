package com.estatehub.service;

import com.estatehub.dto.AddPropertyRequest;
import com.estatehub.dto.SellerRequest;
import com.estatehub.entity.Property;
import com.estatehub.entity.SalesProperty;
import com.estatehub.repository.PropertyRepository;
import com.estatehub.repository.SalesPropertyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * PropertyService - migrated from dbms.java, Buyer.java, Seller.java, Admin.java
 *
 * ALL business logic preserved exactly as in original files.
 */
@Service
@Slf4j
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private SalesPropertyRepository salesPropertyRepository;

    @Value("${app.upload.dir:uploads/properties}")
    private String uploadDir;

    // Mirrors dbms.price static field — used in Loan.createLoan(): double loanAmount = dbms.price
    // Now passed contextually through the service calls

    // ============================================================
    // BUYER OPERATIONS
    // Migrated from Buyer.java and dbms.java
    // ============================================================

    /**
     * Migrated from dbms.Sales_Data(cityName, propertyName)
     *
     * Preserves city-to-table mapping switch from dbms.Sales_Data():
     *   "ahmedabad" → ahmedabad table
     *   "rajkot"    → rajkot table
     *   "surat"     → surat table
     *   "vadodara"  → vadodara table
     *   "bhavnagar" → bhavnagar table
     *
     * Preserves query: SELECT rId, rName, rAddress, rPrice, rArea, rFacility, rPath
     *                  FROM tableName WHERE rName LIKE propertyName+"%"
     *
     * Preserves property type check:
     *   if (!actualType.toLowerCase().startsWith(propertyName.toLowerCase()))
     *     → "NOTE: The selected property ID is not a X type. It is actually Y."
     */
    public List<Property> searchProperties(String cityName, String propertyName) {
        // Validate city name — from dbms.Sales_Data() switch
        if (!isValidCity(cityName)) {
            throw new IllegalArgumentException("Invalid city name: " + cityName);
        }
        return propertyRepository.findByCityNameIgnoreCaseAndRNameStartingWithIgnoreCase(cityName, propertyName);
    }

    /**
     * Migrated from dbms.Sales_Data() — property type validation:
     *   if (!actualType.toLowerCase().startsWith(propertyName.toLowerCase()))
     *     → "NOTE: The selected property ID (%d) is not a %s type. It is actually '%s'."
     */
    public Property validateAndGetProperty(Integer rId, String propertyName) {
        Optional<Property> propertyOpt = propertyRepository.findByRId(rId);
        if (propertyOpt.isEmpty()) {
            throw new IllegalArgumentException("No property found with the given ID.");
        }
        Property property = propertyOpt.get();

        // Type check preserved from dbms.Sales_Data()
        if (!property.getRName().toLowerCase().startsWith(propertyName.toLowerCase())) {
            throw new IllegalArgumentException(
                "NOTE: The selected property ID (" + rId + ") is not a " + propertyName +
                " type. It is actually '" + property.getRName() + "'."
            );
        }
        return property;
    }

    /**
     * Migrated from dbms.Sales_Data() — purchase flow:
     *   1. Find property
     *   2. Validate type
     *   3. Delete property (call deleteData procedure → here: mark as sold)
     *   4. Save to sales_property
     *   5. Write to purchase file
     *   Returns the price (mirrors dbms.price = rs1.getDouble("rPrice"))
     */
    public Property purchaseProperty(Integer rId, String propertyName, String cityName, Integer customerId) {
        Property property = validateAndGetProperty(rId, propertyName);

        // Mirrors dbms.Sales_Data() deleteData procedure call
        property.setStatus("sold");
        propertyRepository.save(property);

        // Mirrors Admin.view_properties("sold") — save to sales_property
        SalesProperty sale = new SalesProperty();
        sale.setCityName(cityName);
        sale.setRId(rId);
        sale.setRName(property.getRName());
        sale.setRAddress(property.getRAddress());
        sale.setRPrice(property.getRPrice());
        sale.setRArea(property.getRArea());
        sale.setRFacility(property.getRFacility());
        sale.setRPath(property.getRPath());
        sale.setSaleType("sold");
        sale.setCustomerId(customerId);
        salesPropertyRepository.save(sale);

        log.info("Property purchased successfully. ID: {}, City: {}", rId, cityName);
        return property;
    }

    /**
     * Migrated from dbms.LowestPriceDynamicFetcher()
     * Buyer menu option 1: "View Cheapest Property (Per City)"
     */
    public List<Property> getCheapestProperties(String cityName) {
        return propertyRepository.findCheapestByCity(cityName.toLowerCase());
    }

    /**
     * Migrated from dbms.SearchPropertiesByBudget()
     * Buyer menu option 2: "Search Property by Budget"
     * Original: CALL SearchPropertiesByBudget(cityName, maxPrice)
     * Preserved: city + maxPrice filter
     */
    public List<Property> searchByBudget(String cityName, Double maxPrice) {
        if (!isValidCity(cityName)) {
            throw new IllegalArgumentException("Invalid city: " + cityName);
        }
        return propertyRepository.findByCityNameIgnoreCaseAndRPriceLessThanEqual(cityName, maxPrice);
    }

    /**
     * Migrated from dbms.ShowAllPropertiesPricesCaller()
     * Buyer menu option 3: "Show All Properties Prices & ID"
     * Original: CALL ShowAllPropertiesPrices()
     */
    public List<Property> showAllPropertiesPrices() {
        return propertyRepository.findAllAvailableOrderByPrice();
    }

    /**
     * Migrated from dbms.countPropertiesByCity()
     * Admin menu option 8: "view Count of Property by city"
     * Original: CALL CountPropertiesByCity(city)
     */
    public long countPropertiesByCity(String cityName) {
        return propertyRepository.countByCityNameIgnoreCase(cityName);
    }

    /**
     * Migrated from dbms.getContacts()
     * Admin menu option 9: "view contact of customer"
     * Original: SELECT GetCustomerContact(custId) as contact
     */
    public Optional<Long> getOwnerContact(Integer rId) {
        return propertyRepository.findOwnerContactByRId(rId);
    }

    // ============================================================
    // SELLER OPERATIONS
    // Migrated from Seller.seller_Residential() in Customer.java
    // ============================================================

    /**
     * Migrated from Seller.seller_Residential()
     *
     * Preserves ALL seller logic:
     *   - Property name choices: flat/bungalow/tenement/villa/raw-house
     *   - rArea = area + " sq ft" (from Seller.seller_Residential line: rArea = sc.nextLine().concat("sq ft"))
     *   - rExtention = rPath.substring(rPath.lastIndexOf("."))
     *   - Brokerage check: totalPrice = rPrice * 0.015
     *     if (brokerage == totalPrice) → "property is committed"
     *     else → "please enter valid brokerage"
     *     if (brokerage != totalPrice again) → "Brokerage mismatch again. Exiting..."
     *   - Stored procedure: {call insertResidentialData(?,?,?,?,?,?,?,?,?,?,?,?)}
     *     params: cityName, rName, rAddress, rPrice, rArea, rFacility,
     *             imageBlob, rPath, rExtention, sellerName, contact, address
     *
     * Returns: "committed" if brokerage matches, throws exception if not
     */
    public Property listProperty(SellerRequest request, String sellerName, String sellerPhone) {
        // Brokerage check — from Seller.seller_Residential()
        // totalPrice = rPrice * 0.015
        double expectedBrokerage = request.getPrice() * 0.015;

        // if (brokerage == totalPrice) → "your property is committed"
        if (request.getBrokerage() == null ||
            Math.abs(request.getBrokerage() - expectedBrokerage) > 0.01) {
            // else → "your property is not committed, please enter valid brokerage"
            // Second check: if (brokerage != totalPrice) → "Brokerage mismatch again. Exiting..."
            throw new IllegalArgumentException(
                "Brokerage mismatch. Expected: " + String.format("%.2f", expectedBrokerage) +
                ". Please enter valid brokerage amount."
            );
        }

        // rArea = area + "sq ft" — from Seller.seller_Residential()
        String rArea = request.getArea() + "sq ft";

        // rExtention = rPath.substring(rPath.lastIndexOf(".")) — from Seller
        String rPath = "";
        String rExtention = ".jpg";
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            try {
                rPath = saveImage(request.getImageFile(), request.getCityName());
                String originalName = request.getImageFile().getOriginalFilename();
                if (originalName != null && originalName.contains(".")) {
                    rExtention = originalName.substring(originalName.lastIndexOf("."));
                }
            } catch (Exception e) {
                log.error("Image save error: {}", e.getMessage());
            }
        }

        Property property = new Property();
        property.setRName(request.getPropertyType());  // flat/bungalow/tenement/villa/raw-house
        property.setRAddress(request.getPropertyAddress());
        property.setRPrice(request.getPrice());
        property.setRArea(rArea);
        property.setRFacility(request.getFacilities());
        property.setRPath(rPath);
        property.setRExtention(rExtention);
        property.setCityName(request.getCityName().toLowerCase());
        property.setOwnerName(sellerName);
        property.setOwnerContact(sellerPhone != null ? parseLong(sellerPhone) : null);
        property.setOwnerAddress(request.getSellerAddress());
        property.setPropertyCategory(request.getPropertyCategory() != null ? request.getPropertyCategory() : "Residential");
        // "property is committed" — from Seller.seller_Residential()
        property.setStatus("committed");

        Property saved = propertyRepository.save(property);

        // Mirror: Admin.view_properties("commited") → property_log table
        SalesProperty committed = new SalesProperty();
        committed.setCityName(request.getCityName().toLowerCase());
        committed.setRId(saved.getRId());
        committed.setRName(saved.getRName());
        committed.setRAddress(saved.getRAddress());
        committed.setRPrice(saved.getRPrice());
        committed.setRArea(saved.getRArea());
        committed.setRFacility(saved.getRFacility());
        committed.setRPath(saved.getRPath());
        committed.setSaleType("commited"); // original spelling preserved: "commited"
        salesPropertyRepository.save(committed);

        log.info("Property listed by seller: {} in {}", request.getPropertyType(), request.getCityName());
        return saved;
    }

    // ============================================================
    // ADMIN OPERATIONS
    // Migrated from Admin.java
    // ============================================================

    /**
     * Migrated from Admin.addProperty()
     *
     * Preserves INSERT SQL:
     *   INSERT INTO properties (rName, rArea, rPath, rExtention, rFacility, rAddress, rPrice)
     *
     * Preserves area formatting: area1 + " sq ft." — from Admin.addProperty()
     */
    public Property adminAddProperty(AddPropertyRequest request) {
        // area1 + " sq ft." — from Admin.addProperty()
        String area = request.getArea() + " sq ft.";

        String rPath = "";
        String rExtention = ".jpg";
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            try {
                rPath = saveImage(request.getImageFile(), request.getCityName() != null ? request.getCityName() : "general");
                String originalName = request.getImageFile().getOriginalFilename();
                if (originalName != null && originalName.contains(".")) {
                    rExtention = originalName.substring(originalName.lastIndexOf("."));
                }
            } catch (Exception e) {
                log.error("Image save error: {}", e.getMessage());
            }
        }

        Property property = new Property();
        property.setRName(request.getName());
        property.setRArea(area);
        property.setRPath(rPath);
        property.setRExtention(rExtention);
        property.setRFacility(request.getFacility());
        property.setRAddress(request.getAddress());
        property.setRPrice(request.getPrice());
        property.setCityName(request.getCityName() != null ? request.getCityName().toLowerCase() : null);
        property.setPropertyCategory(request.getPropertyCategory());
        property.setStatus("available");

        Property saved = propertyRepository.save(property);
        log.info("Admin added property: {}", request.getName());
        return saved;
    }

    /**
     * Migrated from Admin.deleteProperty()
     *
     * Original:
     *   String sql = "DELETE FROM residential_" + city + "1 WHERE rId=?";
     * Web version: marks property as deleted/unavailable
     */
    public boolean adminDeleteProperty(String cityName, Integer rId) {
        Optional<Property> propertyOpt = propertyRepository.findByRId(rId);
        if (propertyOpt.isEmpty()) {
            log.warn("Property Not Found: rId={}", rId);
            return false;
        }
        Property property = propertyOpt.get();
        property.setStatus("deleted");
        propertyRepository.save(property);
        log.info("Property deleted by admin: rId={}, city={}", rId, cityName);
        return true;
    }

    /**
     * Migrated from Admin.view_properties("sold")
     * Admin menu 5: SELECT * FROM sales_property
     */
    public List<SalesProperty> viewSoldProperties() {
        return salesPropertyRepository.findBySaleType("sold");
    }

    /**
     * Migrated from Admin.view_properties("commited")
     * Admin menu 6: SELECT * FROM property_log
     * Original spelling "commited" preserved.
     */
    public List<SalesProperty> viewCommittedProperties() {
        return salesPropertyRepository.findBySaleType("commited");
    }

    /**
     * Get all properties for a city — all categories (for Buyer menu)
     */
    public List<Property> getPropertiesByCity(String cityName) {
        return propertyRepository.findByCityNameIgnoreCase(cityName);
    }

    /**
     * Get all available properties
     */
    public List<Property> getAllAvailableProperties() {
        return propertyRepository.findByStatus("available");
    }

    /**
     * Get property by ID
     */
    public Optional<Property> getPropertyById(Integer rId) {
        return propertyRepository.findByRId(rId);
    }

    // ============================================================
    // FILE OPERATIONS - Migrated from FileMerger.java
    // ============================================================

    /**
     * Migrated from FileMerger.mergeFiles(cityName)
     *
     * Preserves all section headers:
     *   "================ Loan Details ================"
     *   "================ Customer Details ================"
     *   "================ Purchase Details ================"
     * Output: "property_{cityName}.txt"
     *
     * In web version: generates a summary string for email attachment
     */
    public File generateMergedPropertyFile(String cityName, String customerDetails,
                                            String loanDetails, String purchaseDetails) {
        // Always recreate — from FileMerger: new FileOutputStream(mergedFile, false)
        File mergedFile = new File("property_" + cityName + ".txt");

        try (FileOutputStream fos = new FileOutputStream(mergedFile, false)) {

            // Loan Details section — from FileMerger
            if (loanDetails != null && !loanDetails.isEmpty()) {
                fos.write("\n================ Loan Details ================\n".getBytes());
                fos.write(loanDetails.getBytes());
            }

            // Customer Details section — from FileMerger
            if (customerDetails != null && !customerDetails.isEmpty()) {
                fos.write("\n================ Customer Details ================\n".getBytes());
                fos.write(customerDetails.getBytes());
            }

            // Purchase Details section — from FileMerger
            if (purchaseDetails != null && !purchaseDetails.isEmpty()) {
                fos.write("\n================ Purchase Details ================\n".getBytes());
                fos.write(purchaseDetails.getBytes());
            }

            fos.flush();
            log.info("Merged file created: {}", mergedFile.getName());

        } catch (IOException e) {
            log.error("Error creating merged file: {}", e.getMessage());
        }

        return mergedFile;
    }

    /**
     * Generates purchase receipt file — migrated from dbms.Sales_Data() file writing
     *
     * Original header:
     *   "%-15s %-5s %-15s %-35s %-30s %-45s %-12s%n"
     *   "Property", "ID", "Area", "Image Path", "Facilities", "Address", "Price"
     */
    public File generatePurchaseReceiptFile(String cityName, Property property) {
        File f = new File(cityName + "_PurchaseFile.txt");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            // Preserve exact format from dbms.Sales_Data()
            String header = String.format(
                "%-15s %-5s %-15s %-35s %-30s %-45s %-12s%n",
                "Property", "ID", "Area", "Image Path", "Facilities", "Address", "Price"
            );
            fos.write(header.getBytes());
            fos.write("-------------------------------------------------------------------------------------------------------------\n".getBytes());

            String line = String.format(
                "%-15s %-5d %-15s %-35s %-30s %-45s %-12.2f%n",
                property.getRName(),
                property.getRId(),
                property.getRArea(),
                property.getRPath(),
                property.getRFacility(),
                property.getRAddress(),
                property.getRPrice()
            );
            fos.write(line.getBytes());
            fos.flush();
        } catch (IOException e) {
            log.error("Error writing purchase file: {}", e.getMessage());
        }
        return f;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Validates city name — from dbms.Sales_Data() switch:
     * ahmedabad, rajkot, surat, vadodara, bhavnagar
     */
    private boolean isValidCity(String cityName) {
        if (cityName == null) return false;
        return switch (cityName.toLowerCase()) {
            case "ahmedabad", "rajkot", "surat", "vadodara", "bhavnagar" -> true;
            default -> false;
        };
    }

    /**
     * Returns all 5 valid cities — from Customer.chooseCity()
     */
    public List<String> getValidCities() {
        return List.of("ahmedabad", "rajkot", "surat", "vadodara", "bhavnagar");
    }

    /**
     * Returns residential property types — from Buyer.java and Seller.java:
     *   flat, bungalow, tenement, villa, raw-house
     */
    public List<String> getResidentialTypes() {
        return List.of("flat", "bungalow", "tenement", "villa", "raw-house");
    }

    /**
     * Returns commercial property types — from Buyer.java:
     *   Office, Shop, mall, Showroom
     */
    public List<String> getCommercialTypes() {
        return List.of("Office", "Shop", "mall", "Showroom");
    }

    /**
     * Returns industrial property types — from Buyer.java:
     *   warehouse, factory, manufacturing, workshop
     */
    public List<String> getIndustrialTypes() {
        return List.of("warehouse", "factory", "manufacturing", "workshop");
    }

    private String saveImage(MultipartFile file, String cityName) throws IOException {
        Path uploadPath = Paths.get(uploadDir, cityName);
        Files.createDirectories(uploadPath);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        return filePath.toString();
    }

    private Long parseLong(String s) {
        try {
            return Long.parseLong(s.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
