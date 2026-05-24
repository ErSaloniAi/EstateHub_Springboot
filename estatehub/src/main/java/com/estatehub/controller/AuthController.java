package com.estatehub.controller;

import com.estatehub.dto.RegisterRequest;
import com.estatehub.entity.Customer;
import com.estatehub.security.JwtUtil;
import com.estatehub.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * AuthController - handles all authentication flows
 *
 * Migrated from RealEstate.java:
 *   register() → POST /auth/register
 *   login()    → POST /auth/login
 *   main() admin check → verified in AuthService.login()
 *
 * Migrated from Admin.connectAsAdmin():
 *   Admin password verify → handled by Spring Security + AdminService
 */
@Controller
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    // ============================================================
    // REGISTER
    // ============================================================

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    /**
     * POST register — migrated from RealEstate.register()
     *
     * Preserves:
     *   fn = sc.next().trim()
     *   ln = sc.next().trim()
     *   name = fn.concat(ln)
     *   mobileNumber.matches("^[6-9]\\d{9}$")
     *   emailID.endsWith("@gmail.com")
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            Customer customer = authService.register(request);
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please check your email for OTP.");
            return "redirect:/auth/verify-otp";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            model.addAttribute("error", "Unexpected error during registration: " + e.getMessage());
            return "auth/register";
        }
    }

    // ============================================================
    // OTP VERIFICATION
    // ============================================================

    @GetMapping("/verify-otp")
    public String verifyOtpPage(Model model) {
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                             @RequestParam String otp,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            boolean verified = authService.verifyOtp(email, otp);
            if (verified) {
                redirectAttributes.addFlashAttribute("success", "Email verified successfully! Please login.");
                return "redirect:/auth/login";
            } else {
                model.addAttribute("error", "Invalid OTP. Please try again.");
                model.addAttribute("email", email);
                return "auth/verify-otp";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "auth/verify-otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            authService.resendOtp(email);
            redirectAttributes.addFlashAttribute("success", "New OTP sent to your email.");
            redirectAttributes.addFlashAttribute("email", email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/auth/verify-otp";
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String logout,
                             Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    // ============================================================
    // FORGOT PASSWORD
    // ============================================================

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    /**
     * Initiate password reset — sends reset link to email
     * emailID.endsWith("@gmail.com") validation preserved
     */
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort();
            authService.initiatePasswordReset(email, baseUrl);
            redirectAttributes.addFlashAttribute("success",
                "If your email is registered, you will receive a reset link shortly.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/auth/forgot-password";
    }

    // ============================================================
    // RESET PASSWORD
    // ============================================================

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        try {
            authService.resetPassword(token, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password reset successfully! Please login.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
    }
}
