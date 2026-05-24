package com.estatehub.controller;

import com.estatehub.dto.AddPropertyRequest;
import com.estatehub.dto.SellerRequest;
import com.estatehub.entity.Customer;
import com.estatehub.entity.Property;
import com.estatehub.service.CustomerService;
import com.estatehub.service.EmailService;
import com.estatehub.service.PropertyService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

/**
 * PropertyController
 * Buyer flows migrated from Buyer.java + dbms.java
 * Seller flows migrated from Seller.seller_Residential()
 */
@Controller
@Slf4j
public class PropertyController {

    @Autowired private PropertyService propertyService;
    @Autowired private CustomerService customerService;
    @Autowired private EmailService emailService;

    // ─── PUBLIC LISTING ──────────────────────────────────────────

    @GetMapping("/properties/list")
    public String listProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double maxPrice,
            Model model) {

        List<Property> properties;

        // Migrated from dbms.SearchPropertiesByBudget(): city + maxPrice
        if (city != null && !city.isEmpty() && maxPrice != null) {
            properties = propertyService.searchByBudget(city, maxPrice);
        } else if (city != null && !city.isEmpty() && type != null && !type.isEmpty()) {
            // Migrated from dbms.Sales_Data(): city + propertyName
            properties = propertyService.searchProperties(city, type);
        } else if (city != null && !city.isEmpty()) {
            properties = propertyService.getPropertiesByCity(city);
        } else {
            // dbms.ShowAllPropertiesPrices(): all available
            properties = propertyService.showAllPropertiesPrices();
        }

        model.addAttribute("properties", properties);
        model.addAttribute("cities", propertyService.getValidCities());
        model.addAttribute("residentialTypes", propertyService.getResidentialTypes());
        model.addAttribute("commercialTypes", propertyService.getCommercialTypes());
        model.addAttribute("industrialTypes", propertyService.getIndustrialTypes());
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedType", type);
        model.addAttribute("maxPrice", maxPrice);
        return "property/list";
    }

    @GetMapping("/properties/view/{id}")
    public String viewProperty(@PathVariable Integer id, Model model) {
        Optional<Property> property = propertyService.getPropertyById(id);
        if (property.isEmpty()) {
            return "redirect:/properties/list";
        }
        model.addAttribute("property", property.get());
        return "property/view";
    }

    // ─── BUYER FLOWS ─────────────────────────────────────────────

    @GetMapping("/buyer/browse")
    public String buyerBrowse(Model model) {
        model.addAttribute("cities", propertyService.getValidCities());
        model.addAttribute("residentialTypes", propertyService.getResidentialTypes());
        model.addAttribute("commercialTypes", propertyService.getCommercialTypes());
        model.addAttribute("industrialTypes", propertyService.getIndustrialTypes());
        return "buyer/browse";
    }

    /** Migrated from dbms.LowestPriceDynamicFetcher() — Buyer menu option 1 */
    @GetMapping("/buyer/cheapest")
    public String cheapestByCity(@RequestParam String city, Model model) {
        List<Property> cheapest = propertyService.getCheapestProperties(city);
        model.addAttribute("properties", cheapest);
        model.addAttribute("city", city);
        model.addAttribute("title", "Cheapest Properties in " + capitalize(city));
        return "buyer/results";
    }

    /** Migrated from dbms.SearchPropertiesByBudget() — Buyer menu option 2 */
    @GetMapping("/buyer/search-budget")
    public String searchByBudget(@RequestParam String city,
                                  @RequestParam Double maxPrice, Model model) {
        List<Property> results = propertyService.searchByBudget(city, maxPrice);
        model.addAttribute("properties", results);
        model.addAttribute("city", city);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("title", "Properties in " + capitalize(city) + " under ₹" + maxPrice);
        return "buyer/results";
    }

    /** Migrated from dbms.Sales_Data() — main purchase initiation */
    @PostMapping("/buyer/purchase/{id}")
    public String purchaseProperty(@PathVariable Integer id,
                                    @RequestParam String propertyType,
                                    @RequestParam String cityName,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            Optional<Customer> customerOpt = customerService.getCustomerByEmail(userDetails.getUsername());
            Integer customerId = customerOpt.map(Customer::getCustomerId).orElse(null);

            Property purchased = propertyService.purchaseProperty(id, propertyType, cityName, customerId);

            // Generate receipt file — from dbms.Sales_Data() file writing
            File receiptFile = propertyService.generatePurchaseReceiptFile(cityName, purchased);

            // Generate merged file — from FileMerger.mergeFiles()
            String customerDetails = customerOpt.map(c ->
                "Name: " + c.getCName() + "\nPhone: " + c.getCPhone() + "\nEmail: " + c.getCEmail()
            ).orElse("");

            File mergedFile = propertyService.generateMergedPropertyFile(
                cityName, customerDetails, "", receiptFile.exists() ? new String(java.nio.file.Files.readAllBytes(receiptFile.toPath())) : ""
            );

            // Send email — from GmailSender.gmail(): receiver = RealEstate.emailID
            emailService.sendPropertyPurchaseEmail(userDetails.getUsername(), mergedFile);

            redirectAttributes.addFlashAttribute("success",
                "Property purchased successfully! Confirmation sent to " + userDetails.getUsername());
            redirectAttributes.addFlashAttribute("purchasedProperty", purchased);
            return "redirect:/buyer/purchase-success";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/properties/view/" + id;
        } catch (Exception e) {
            log.error("Purchase error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Purchase failed: " + e.getMessage());
            return "redirect:/properties/view/" + id;
        }
    }

    @GetMapping("/buyer/purchase-success")
    public String purchaseSuccess() {
        return "buyer/purchase-success";
    }

    // ─── SELLER FLOWS ────────────────────────────────────────────

    @GetMapping("/seller/list-property")
    public String sellerListPage(Model model) {
        model.addAttribute("sellerRequest", new SellerRequest());
        model.addAttribute("cities", propertyService.getValidCities());
        model.addAttribute("residentialTypes", propertyService.getResidentialTypes());
        model.addAttribute("commercialTypes", propertyService.getCommercialTypes());
        model.addAttribute("industrialTypes", propertyService.getIndustrialTypes());
        return "seller/list-property";
    }

    /**
     * POST seller listing — migrated from Seller.seller_Residential()
     * Brokerage = price * 0.015 check preserved exactly
     */
    @PostMapping("/seller/list-property")
    public String listProperty(@Valid @ModelAttribute SellerRequest request,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            Optional<Customer> customerOpt = customerService.getCustomerByEmail(userDetails.getUsername());
            String sellerName = customerOpt.map(Customer::getCName).orElse(userDetails.getUsername());
            String sellerPhone = customerOpt.map(Customer::getCPhone).orElse("");

            Property listed = propertyService.listProperty(request, sellerName, sellerPhone);
            redirectAttributes.addFlashAttribute("success",
                "Property listed successfully! Your property is committed. ID: " + listed.getRId());
            return "redirect:/seller/my-listings";

        } catch (IllegalArgumentException e) {
            // Preserves: "Brokerage mismatch again. Exiting..." logic
            model.addAttribute("error", e.getMessage());
            model.addAttribute("sellerRequest", request);
            model.addAttribute("cities", propertyService.getValidCities());
            model.addAttribute("residentialTypes", propertyService.getResidentialTypes());
            model.addAttribute("commercialTypes", propertyService.getCommercialTypes());
            model.addAttribute("industrialTypes", propertyService.getIndustrialTypes());
            // Show expected brokerage
            if (request.getPrice() != null) {
                model.addAttribute("expectedBrokerage", String.format("%.2f", request.getPrice() * 0.015));
            }
            return "seller/list-property";
        }
    }

    @GetMapping("/seller/my-listings")
    public String myListings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<Customer> customerOpt = customerService.getCustomerByEmail(userDetails.getUsername());
        String ownerName = customerOpt.map(Customer::getCName).orElse("");
        List<Property> myProps = propertyService.getAllAvailableProperties().stream()
            .filter(p -> ownerName.equals(p.getOwnerName()))
            .toList();
        model.addAttribute("properties", myProps);
        return "seller/my-listings";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
