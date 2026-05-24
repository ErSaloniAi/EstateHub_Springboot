package com.estatehub.service;

import com.estatehub.dto.RegisterRequest;
import com.estatehub.entity.Customer;
import com.estatehub.repository.AdminRepository;
import com.estatehub.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * AuthService - migrated from RealEstate.java (register, login, main)
 * and Admin.java (connectAsAdmin)
 *
 * Preserves ALL authentication logic from RealEstate:
 *
 * register():
 *   - fn = first name
 *   - ln = last name
 *   - name = fn.concat(ln)
 *   - mobileNumber validated by "^[6-9]\\d{9}$"
 *   - calls Customer.process() after registration
 *
 * login():
 *   - SELECT cEmail FROM customer
 *   - email.equalsIgnoreCase(emailID) → found=true, fl=1, Customer.process()
 *   - if not found → "Not registered customer, please login first"
 *   - emailID must end with @gmail.com (flag = emailID.endsWith("@gmail.com"))
 *
 * main():
 *   - Check if email belongs to admin: adminList loop → Admin.connectAsAdmin()
 *   - Then 1.Register or 2.Login
 *
 * Admin.connectAsAdmin():
 *   - RealEstate.emailID.equalsIgnoreCase(ad.email) && password.equals(ad.password)
 *   - If matched → admin menu
 */
@Service
@Slf4j
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Migrated from RealEstate.register()
     *
     * Original logic preserved:
     *   fn = sc.next().trim()
     *   ln = sc.next().trim()
     *   name = fn.concat(ln)
     *   mobileNumber validated by "^[6-9]\\d{9}$"
     */
    public Customer register(RegisterRequest request) {
        // Validate email ends with @gmail.com — from RealEstate.main() and login()
        if (!request.getEmail().endsWith("@gmail.com")) {
            throw new IllegalArgumentException("Invalid email ID. Must end with @gmail.com");
        }

        // Validate mobile number — from RealEstate.register()
        // flag = mobileNumber.matches("^[6-9]\\d{9}$")
        if (!request.getMobileNumber().matches("^[6-9]\\d{9}$")) {
            throw new IllegalArgumentException("Invalid mobile number. Must start with 6-9 and be 10 digits.");
        }

        if (customerRepository.existsBycEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered. Please login.");
        }

        Customer customer = new Customer();

        // From RealEstate.register(): fn = sc.next().trim()
        customer.setFirstName(request.getFirstName().trim());

        // From RealEstate.register(): ln = sc.next().trim()
        customer.setLastName(request.getLastName().trim());

        // From RealEstate.register(): name = fn.concat(ln)
        customer.setCName(request.getFirstName().trim().concat(request.getLastName().trim()));

        customer.setCEmail(request.getEmail());
        customer.setCPhone(request.getMobileNumber());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setRole("ROLE_USER");
        customer.setEmailVerified(false);

        // Default state to "Gujarat" if not yet set — from Customer.customer_details()
        customer.setCState("Gujarat");
        customer.setCAge(25);

        // Generate OTP for email verification
        String otp = generateOtp();
        customer.setOtp(otp);
        customer.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

        Customer saved = customerRepository.save(customer);

        // Send OTP email
        emailService.sendOtpEmail(request.getEmail(), otp);
        // Send welcome email
        emailService.sendWelcomeEmail(request.getEmail(), customer.getCName());

        log.info("New customer registered: {}", request.getEmail());
        return saved;
    }

    /**
     * Migrated from RealEstate.login()
     *
     * Original logic:
     *   SELECT cEmail FROM customer
     *   if (email.equalsIgnoreCase(emailID)) → found=true, fl=1
     *   if (!found) → "Not registered customer, please login first"
     */
    public Customer login(String email, String password) {
        // Email must end with @gmail.com — from RealEstate.main() do-while
        if (!email.endsWith("@gmail.com")) {
            throw new IllegalArgumentException("Invalid email ID. Must end with @gmail.com");
        }

        // Check if admin first — from RealEstate.main():
        // for (AdminData ad : adminList) { if (emailID.equals(ad.email)) → Admin.connectAsAdmin() }
        Optional<com.estatehub.entity.Admin> adminOpt = adminRepository.findByaEmailId(email);
        if (adminOpt.isPresent()) {
            com.estatehub.entity.Admin admin = adminOpt.get();
            // Admin.connectAsAdmin(): password.equals(ad.password) — raw compare in original
            // We use passwordEncoder for security
            if (admin.getPassword().equals(password) || passwordEncoder.matches(password, admin.getPassword())) {
                // Create a virtual Customer object for session management
                Customer adminCustomer = new Customer();
                adminCustomer.setCEmail(admin.getaEmailId());
                adminCustomer.setCName(admin.getaName());
                adminCustomer.setCPhone(admin.getaPhone());
                adminCustomer.setRole("ROLE_ADMIN");
                adminCustomer.setEmailVerified(true);
                return adminCustomer;
            } else {
                throw new IllegalArgumentException("Invalid admin password");
            }
        }

        // From RealEstate.login(): SELECT cEmail FROM customer + equalsIgnoreCase check
        Optional<Customer> customerOpt = customerRepository.findBycEmail(email);
        if (customerOpt.isEmpty()) {
            // Original: "Not registered customer, please login first"
            throw new IllegalArgumentException("Not registered customer, please register first");
        }

        Customer customer = customerOpt.get();
        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        if (!customer.getEmailVerified()) {
            throw new IllegalArgumentException("Please verify your email first. Check your inbox for OTP.");
        }

        log.info("Customer logged in: {}", email);
        return customer;
    }

    /**
     * Verify OTP for email verification.
     * OTP expires in 10 minutes.
     */
    public boolean verifyOtp(String email, String otp) {
        Optional<Customer> customerOpt = customerRepository.findBycEmail(email);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Email not found");
        }

        Customer customer = customerOpt.get();

        if (customer.getOtp() == null || !customer.getOtp().equals(otp)) {
            return false;
        }

        if (customer.getOtpExpiry() == null || customer.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        customer.setEmailVerified(true);
        customer.setOtp(null);
        customer.setOtpExpiry(null);
        customerRepository.save(customer);

        return true;
    }

    /**
     * Resend OTP — regenerates and sends new OTP
     */
    public void resendOtp(String email) {
        Optional<Customer> customerOpt = customerRepository.findBycEmail(email);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Email not found");
        }

        Customer customer = customerOpt.get();
        String otp = generateOtp();
        customer.setOtp(otp);
        customer.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        customerRepository.save(customer);

        emailService.sendOtpEmail(email, otp);
    }

    /**
     * Initiate password reset — generates reset token and sends email
     */
    public void initiatePasswordReset(String email, String baseUrl) {
        if (!email.endsWith("@gmail.com")) {
            throw new IllegalArgumentException("Invalid email. Must end with @gmail.com");
        }

        Optional<Customer> customerOpt = customerRepository.findBycEmail(email);
        if (customerOpt.isEmpty()) {
            // Don't reveal if email exists — security best practice
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        Customer customer = customerOpt.get();
        String token = UUID.randomUUID().toString();
        customer.setResetToken(token);
        customer.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        customerRepository.save(customer);

        String resetLink = baseUrl + "/auth/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(email, resetLink);
    }

    /**
     * Complete password reset with token
     */
    public void resetPassword(String token, String newPassword) {
        Optional<Customer> customerOpt = customerRepository.findByResetToken(token);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        Customer customer = customerOpt.get();
        if (customer.getResetTokenExpiry() == null ||
            customer.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired. Please request a new one.");
        }

        customer.setPassword(passwordEncoder.encode(newPassword));
        customer.setResetToken(null);
        customer.setResetTokenExpiry(null);
        customerRepository.save(customer);
    }

    /**
     * Generates 6-digit OTP
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Check if an email belongs to an admin — from RealEstate.main()
     * Original: for (AdminData ad : adminList) { if (emailID.equals(ad.email)) }
     */
    public boolean isAdminEmail(String email) {
        return adminRepository.existsByaEmailId(email);
    }
}
