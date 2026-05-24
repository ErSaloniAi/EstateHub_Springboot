package com.estatehub.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Admin entity - migrated from Admin.java (AdminData inner class)
 * Preserves ALL fields from AdminData constructor:
 *   AdminData(int id, String name, String phone, String email, String password)
 *
 * Hardcoded admins from Admin constructor preserved as DB seeds:
 *   AdminData(1, "Saloni", "9429769132", "sgorsiya@gmail.com","gorsiya@s")
 *   AdminData(2, "Husain", "6353986953", "haghariya@gmail.com","aghariya@h")
 *   AdminData(3, "Naimish", "7984087441", "ngondaliya@gmail.com","gondaliya@n")
 */
@Entity
@Table(name = "admin1")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aid")
    private Integer aid;

    // From AdminData.name
    @Column(name = "aName", length = 100)
    private String aName;

    // From AdminData.phone
    @Column(name = "aPhone", length = 15)
    private String aPhone;

    // From AdminData.email — used for login check in Admin.connectAsAdmin()
    @Column(name = "aEmailId", unique = true, length = 200)
    private String aEmailId;

    // From AdminData.password — used in password check in Admin.connectAsAdmin()
    @Column(name = "password", length = 200)
    private String password;
}
