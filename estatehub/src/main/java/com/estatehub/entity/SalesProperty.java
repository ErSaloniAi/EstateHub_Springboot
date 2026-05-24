package com.estatehub.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * SalesProperty entity - migrated from Admin.view_properties("sold")
 * Mirrors sales_property table used in:
 *   SELECT * FROM sales_property
 *
 * Also mirrors property_log table used in:
 *   SELECT * FROM property_log (for "commited" properties)
 *
 * From Admin.view_properties() SELECT fields:
 *   cityName, rId, rName, rAddress, rPrice, rArea, rFacility, rPath
 */
@Entity
@Table(name = "sales_property")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cityName", length = 50)
    private String cityName;

    @Column(name = "rId")
    private Integer rId;

    @Column(name = "rName", length = 100)
    private String rName;

    @Column(name = "rAddress", length = 300)
    private String rAddress;

    @Column(name = "rPrice")
    private Double rPrice;

    @Column(name = "rArea", length = 100)
    private String rArea;

    @Column(name = "rFacility", length = 500)
    private String rFacility;

    @Column(name = "rPath", length = 500)
    private String rPath;

    @Column(name = "saleType", length = 20)
    private String saleType; // "sold" or "commited"

    @Column(name = "soldAt")
    private LocalDateTime soldAt;

    @Column(name = "customerId")
    private Integer customerId;

    @PrePersist
    public void prePersist() {
        this.soldAt = LocalDateTime.now();
    }
}
