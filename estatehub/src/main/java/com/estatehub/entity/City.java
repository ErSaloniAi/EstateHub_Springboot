package com.estatehub.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * City entity - migrated from City class used in Ds.java (SLL linked list)
 * Preserves ALL fields displayed in SLL.display():
 *   cityNo, cityName, totalFlat, totalBunglow, totalTenement,
 *   totalRow_house, total_villa, total
 *
 * Cities from Customer.chooseCity():
 *   1.Ahmedabad, 2.Rajkot, 3.Surat, 4.Vadodara, 5.Bhavnagar
 */
@Entity
@Table(name = "city")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cityNo")
    private Integer cityNo;

    // From SLL.display(): cityName
    @Column(name = "cityName", unique = true, length = 50)
    private String cityName;

    // From SLL.display(): totalFlat
    @Column(name = "totalFlat")
    private Integer totalFlat;

    // From SLL.display(): totalBunglow
    @Column(name = "totalBunglow")
    private Integer totalBunglow;

    // From SLL.display(): totalTenement
    @Column(name = "totalTenement")
    private Integer totalTenement;

    // From SLL.display(): totalRow_house
    @Column(name = "totalRow_house")
    private Integer totalRow_house;

    // From SLL.display(): total_villa
    @Column(name = "total_villa")
    private Integer total_villa;

    // From SLL.display(): total (grand total of all property types)
    @Column(name = "total")
    private Integer total;
}
