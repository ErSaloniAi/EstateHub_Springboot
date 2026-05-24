package com.estatehub.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Property entity - migrated from Details.java and dbms.java
 *
 * Preserves ALL fields used in dbms.Sales_Data() SELECT:
 *   SELECT rId, rName, rAddress, rPrice, rArea, rFacility, rPath
 *
 * Preserves ALL fields from dbms.insertProperties() INSERT:
 *   INSERT INTO tableName (rName, rArea, rPath, rExtention, rFacility, rImage,
 *                          rAddress, rPrice, OwnerName, OContact, OAddress)
 *
 * Preserves ALL city table names from dbms.Sales_Data() switch:
 *   ahmedabad, rajkot, surat, vadodara, bhavnagar
 *
 * Preserves ALL property types from Buyer.java:
 *   Residential: flat, bungalow, tenement, villa, raw-house
 *   Commercial:  Office, Shop, mall, Showroom
 *   Industrial:  warehouse, factory, manufacturing, workshop
 */
@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rId")
    private Integer rId;

    // rName = property type (flat/bungalow/villa etc) — from all city tables
    @Column(name = "rName", length = 100)
    private String rName;

    // rAddress — from Details and all SELECT queries
    @Column(name = "rAddress", length = 300)
    private String rAddress;

    // rPrice — from Details.price and dbms.price static field
    @Column(name = "rPrice")
    private Double rPrice;

    // rArea — from Details.area and Seller: area + " sq ft"
    @Column(name = "rArea", length = 100)
    private String rArea;

    // rFacility — from Details.facility
    @Column(name = "rFacility", length = 500)
    private String rFacility;

    // rPath — file path to image, from Details.path
    @Column(name = "rPath", length = 500)
    private String rPath;

    // rExtention — from Seller.seller_Residential: rPath.substring(rPath.lastIndexOf("."))
    @Column(name = "rExtention", length = 10)
    private String rExtention;

    // rImage — BLOB in original, stored as byte[] here
    @Lob
    @Column(name = "rImage", columnDefinition = "LONGBLOB")
    private byte[] rImage;

    // OwnerName — from dbms.insertProperties and Seller
    @Column(name = "OwnerName", length = 200)
    private String ownerName;

    // OContact — from dbms.insertProperties: Long ownerContact
    @Column(name = "OContact")
    private Long ownerContact;

    // OAddress — from dbms.insertProperties
    @Column(name = "OAddress", length = 300)
    private String ownerAddress;

    // cityName — which city this property belongs to
    // From dbms.Sales_Data() switch: ahmedabad, rajkot, surat, vadodara, bhavnagar
    @Column(name = "cityName", length = 50)
    private String cityName;

    // propertyCategory — Residential / Commercial / Industrial
    // From Buyer.java menu options
    @Column(name = "propertyCategory", length = 30)
    private String propertyCategory;

    // status — available / sold / committed
    // From Admin.view_properties(): "sold" → sales_property, "commited" → property_log
    @Column(name = "status", length = 20)
    private String status;
}
