package com.estatehub.controller;

import com.estatehub.dto.CustomerDetailsRequest;
import com.estatehub.dto.LoanRequest;
import com.estatehub.entity.*;
import com.estatehub.repository.SalesPropertyRepository;
import com.estatehub.service.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

// ─────────────────────────────────────────────────────────────────
// DASHBOARD CONTROLLER
// Migrated from Customer.process() — main user menu
// ─────────────────────────────────────────────────────────────────
@Controller
@RequestMapping("/dashboard")
@Slf4j
class DashboardController {

    @Autowired private CustomerService customerService;
    @Autowired private PropertyService propertyService;
    @Autowired private LoanService loanService;
    @Autowired private SalesPropertyRepository salesPropertyRepository;

    /**
     * Migrated from Customer.process():
     *   1. View details of admin
     *   2. Connect as Buyer/Seller
     *   3. Exit
     */
    @GetMapping({"", "/"})
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<Customer> customerOpt = customerService.getCustomerByEmail(userDetails.getUsername());
        customerOpt.ifPresent(c -> model.addAttribute("customer", c));
        model.addAttribute("cities", propertyService.getValidCities());

        // Recent purchases for this user
        if (customerOpt.isPresent()) {
            List<SalesProperty> purchases = salesPropertyRepository.findByCustomerId(
                customerOpt.get().getCustomerId());
            model.addAttribute("purchases", purchases);

            // Loans
            List<Loan> loans = loanService.getLoansByCustomer(customerOpt.get().getCustomerId());
            model.addAttribute("loans", loans);
        }
        return "dashboard/index";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        customerService.getCustomerByEmail(userDetails.getUsername())
            .ifPresent(c -> model.addAttribute("customer", c));
        return "dashboard/profile";
    }

    /** Migrated from Customer.customer_details() — save profile details */
    @PostMapping("/profile")
    public String saveProfile(@AuthenticationPrincipal UserDetails userDetails,
                               @ModelAttribute CustomerDetailsRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            customerService.saveCustomerDetails(userDetails.getUsername(), request);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/profile";
    }
}

// ─────────────────────────────────────────────────────────────────
// LOAN CONTROLLER
// Migrated from Transaction.java (Transactions + Loan classes)
// ─────────────────────────────────────────────────────────────────
@Controller
@RequestMapping("/loan")
@Slf4j
class LoanController {

    @Autowired private LoanService loanService;
    @Autowired private PropertyService propertyService;
    @Autowired private CustomerService customerService;

    /**
     * Migrated from Transactions.transaction():
     *   1. Loan → Loan.createLoan()
     *   2. POA  → POA.poa()
     */
    @GetMapping("/choose/{propertyId}")
    public String chooseTransaction(@PathVariable Integer propertyId, Model model) {
        propertyService.getPropertyById(propertyId)
            .ifPresent(p -> model.addAttribute("property", p));
        model.addAttribute("loanRequest", new LoanRequest());
        model.addAttribute("propertyId", propertyId);
        return "loan/choose";
    }

    /**
     * POST loan — migrated from Loan.createLoan()
     * Banks: 1=Saurastra(8.75%), 2=HDFC(9.75%), 3=Karnavati(8.95%)
     * totalAmount = loanAmount * 1.015  (1.5% brokerage)
     * EMI = (totalAmount * monthlyRate * (1+monthlyRate)^n) / ((1+monthlyRate)^n - 1)
     */
    @PostMapping("/create")
    public String createLoan(@Valid @ModelAttribute LoanRequest request,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        try {
            Loan loan = loanService.createLoan(request, userDetails.getUsername());

            // Show EMI details — from Loan.createLoan() output
            redirectAttributes.addFlashAttribute("success",
                String.format("Loan created successfully! Bank: %s | EMI: ₹%.2f/month",
                    loan.getBankName(), loan.getEmiAmount()));
            redirectAttributes.addFlashAttribute("loan", loan);
            return "redirect:/loan/success";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/loan/choose/" + request.getPropertyId();
        }
    }

    /** POA payment — migrated from POA.poa(): if (amount == (int) totalAmount) */
    @PostMapping("/poa")
    public String processPOA(@RequestParam Integer propertyId,
                              @RequestParam Double amountPaid,
                              RedirectAttributes redirectAttributes) {
        try {
            String result = loanService.processPOA(propertyId, amountPaid);
            redirectAttributes.addFlashAttribute("success", result);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/loan/success";
    }

    @GetMapping("/success")
    public String loanSuccess() { return "loan/success"; }
}

// ─────────────────────────────────────────────────────────────────
// ADMIN CONTROLLER
// Migrated from Admin.java — all admin menu items
// ─────────────────────────────────────────────────────────────────
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private PropertyService propertyService;
    @Autowired private LoanService loanService;

    /** Admin dashboard — mirrors Admin menu 1-10 */
    @GetMapping({"", "/"})
    public String adminDashboard(Model model) {
        AdminService.AdminAnalytics analytics = adminService.getAnalytics();
        model.addAttribute("analytics", analytics);
        return "admin/dashboard";
    }

    /** Admin menu 1: View Admin Details — Admin.viewAdminData(): SELECT * FROM admin */
    @GetMapping("/admins")
    public String viewAdmins(Model model) {
        model.addAttribute("admins", adminService.viewAllAdmins());
        return "admin/admins";
    }

    /** Admin menu 2: Add Admin — Admin.addAdminData(): INSERT INTO admin */
    @PostMapping("/admins/add")
    public String addAdmin(@RequestParam String name, @RequestParam String phone,
                            @RequestParam String email, @RequestParam String password,
                            RedirectAttributes redirectAttributes) {
        try {
            adminService.addAdmin(name, phone, email, password);
            redirectAttributes.addFlashAttribute("success", "Admin data inserted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/admins";
    }

    /** Admin menu 3: Add Property — Admin.addProperty() */
    @GetMapping("/properties/add")
    public String addPropertyPage(Model model) {
        model.addAttribute("addPropertyRequest", new com.estatehub.dto.AddPropertyRequest());
        model.addAttribute("cities", propertyService.getValidCities());
        model.addAttribute("residentialTypes", propertyService.getResidentialTypes());
        model.addAttribute("commercialTypes", propertyService.getCommercialTypes());
        model.addAttribute("industrialTypes", propertyService.getIndustrialTypes());
        return "admin/add-property";
    }

    @PostMapping("/properties/add")
    public String addProperty(@Valid @ModelAttribute("addPropertyRequest") com.estatehub.dto.AddPropertyRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            Property p = propertyService.adminAddProperty(request);
            redirectAttributes.addFlashAttribute("success", "Property Added Successfully! ID: " + p.getRId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding property: " + e.getMessage());
        }
        return "redirect:/admin/properties";
    }

    /** Admin menu 4: Delete Property — Admin.deleteProperty() */
    @PostMapping("/properties/delete")
    public String deleteProperty(@RequestParam String city,
                                  @RequestParam Integer rId,
                                  RedirectAttributes redirectAttributes) {
        boolean deleted = propertyService.adminDeleteProperty(city, rId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Property Deleted!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Property Not Found");
        }
        return "redirect:/admin/properties";
    }

    @GetMapping("/properties")
    public String manageProperties(Model model) {
        model.addAttribute("properties", adminService.getAllProperties());
        model.addAttribute("cities", propertyService.getValidCities());
        return "admin/properties";
    }

    /** Admin menu 5: View Sold Properties — Admin.view_properties("sold") */
    @GetMapping("/sold")
    public String viewSold(Model model) {
        model.addAttribute("properties", propertyService.viewSoldProperties());
        model.addAttribute("type", "sold");
        return "admin/sold-properties";
    }

    /** Admin menu 6: View Committed Properties — Admin.view_properties("commited") */
    @GetMapping("/committed")
    public String viewCommitted(Model model) {
        model.addAttribute("properties", propertyService.viewCommittedProperties());
        model.addAttribute("type", "commited");
        return "admin/sold-properties";
    }

    /** Admin menu 7: View Loan Details — Admin.viewLoanDetail() */
    @GetMapping("/loans")
    public String viewLoans(Model model) {
        model.addAttribute("loans", loanService.getAllLoans());
        return "admin/loans";
    }

    /** Admin menu 8: Count Properties by City — dbms.countPropertiesByCity() */
    @GetMapping("/count-by-city")
    public String countByCity(@RequestParam(required = false) String city, Model model) {
        model.addAttribute("cities", propertyService.getValidCities());
        if (city != null && !city.isEmpty()) {
            long count = propertyService.countPropertiesByCity(city);
            model.addAttribute("count", count);
            model.addAttribute("selectedCity", city);
        }
        return "admin/count-by-city";
    }

    /** Admin menu 9: View Contact of Customer — dbms.getContacts() */
    @GetMapping("/contacts")
    public String viewContacts(@RequestParam(required = false) Integer propertyId, Model model) {
        model.addAttribute("properties", adminService.getAllProperties());
        if (propertyId != null) {
            propertyService.getOwnerContact(propertyId)
                .ifPresent(c -> model.addAttribute("contact", c));
            model.addAttribute("propertyId", propertyId);
        }
        return "admin/contacts";
    }

    /** Admin: Manage Customers */
    @GetMapping("/customers")
    public String manageCustomers(Model model) {
        model.addAttribute("customers", adminService.getAllCustomers());
        return "admin/customers";
    }
}

// SalesPropertyRepository is in com.estatehub.repository package
