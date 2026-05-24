package com.estatehub.security;

import com.estatehub.entity.Admin;
import com.estatehub.entity.Customer;
import com.estatehub.repository.AdminRepository;
import com.estatehub.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * UserDetailsServiceImpl - loads user details for Spring Security
 *
 * Preserves admin login logic from Admin.connectAsAdmin():
 *   for (AdminData ad : adminList) {
 *     if (RealEstate.emailID.equalsIgnoreCase(ad.email) && password.equals(ad.password))
 *
 * Preserves customer login from RealEstate.login():
 *   SELECT cEmail FROM customer → email.equalsIgnoreCase(emailID) → found=true
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Check admin first — from RealEstate.main():
        //   for (AdminData ad : adminList) { if (emailID.equals(ad.email)) → Admin.connectAsAdmin() }
        Optional<Admin> adminOpt = adminRepository.findByaEmailId(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            return new User(
                admin.getaEmailId(),
                // Admin.connectAsAdmin() uses raw password compare; encode for Spring Security
                "{noop}" + admin.getPassword(), // {noop} since we stored raw password for original compat
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        // Check customer — from RealEstate.login(): SELECT cEmail FROM customer
        Optional<Customer> customerOpt = customerRepository.findBycEmail(email);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            return new User(
                customer.getCEmail(),
                customer.getPassword(),
                List.of(new SimpleGrantedAuthority(
                    customer.getRole() != null ? customer.getRole() : "ROLE_USER"
                ))
            );
        }

        throw new UsernameNotFoundException("User not found: " + email);
    }
}
