package com.estatehub.config;

import com.estatehub.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataInitializer - runs on startup to seed default admin data
 *
 * Preserved from Admin() constructor:
 *   adminList.add(new AdminData(1, "Saloni", "9429769132", "sgorsiya@gmail.com","gorsiya@s"))
 *   adminList.add(new AdminData(2, "Husain", "6353986953", "haghariya@gmail.com","aghariya@h"))
 *   adminList.add(new AdminData(3, "Naimish", "7984087441", "ngondaliya@gmail.com","gondaliya@n"))
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminService adminService;

    @Override
    public void run(String... args) {
        log.info("Initializing default admin data...");
        adminService.seedDefaultAdmins();
        log.info("EstateHub is ready!");
    }
}
