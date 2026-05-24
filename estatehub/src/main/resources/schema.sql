-- ============================================================
-- ESTATEHUB — Complete MySQL Schema
-- Database: real_estate
-- Port:     3307 (from CreateConnection.java / Admin.java)
-- ============================================================

CREATE DATABASE IF NOT EXISTS real_estate
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE real_estate;

-- ============================================================
-- TABLE: customer
-- From Customer.java customer_details() INSERT:
--   INSERT INTO customer (cName, cPhone, cEmail, cAddress, cCity, cState,
--                         cAge, cOccupation, cIncome, type)
-- Defaults: state = 'Gujarat' if empty, age = 25 if <=0
-- ============================================================
CREATE TABLE IF NOT EXISTS customer (
    customerId      INT AUTO_INCREMENT PRIMARY KEY,
    cFirstName      VARCHAR(100),
    cLastName       VARCHAR(100),
    cName           VARCHAR(200),
    cPhone          VARCHAR(15),
    cEmail          VARCHAR(200) UNIQUE NOT NULL,
    cAddress        VARCHAR(300),
    cCity           VARCHAR(100),
    cState          VARCHAR(100) DEFAULT 'Gujarat',  -- default from Customer.customer_details()
    cAge            INT          DEFAULT 25,           -- default from Customer.customer_details()
    cOccupation     VARCHAR(100),
    cIncome         DOUBLE       DEFAULT 0.0,
    type            VARCHAR(20),                       -- 'buyer' or 'seller'
    password        VARCHAR(200),
    role            VARCHAR(20)  DEFAULT 'ROLE_USER',
    emailVerified   BOOLEAN      DEFAULT FALSE,
    otp             VARCHAR(10),
    otpExpiry       DATETIME,
    resetToken      VARCHAR(200),
    resetTokenExpiry DATETIME,
    cityName        VARCHAR(50),                       -- from Customer.chooseCity()
    createdAt       DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: admin1
-- From Admin.java AdminData inner class:
--   AdminData(int id, String name, String phone, String email, String password)
-- Hardcoded default admins from Admin() constructor:
--   ("Saloni", "9429769132", "sgorsiya@gmail.com",  "gorsiya@s")
--   ("Husain", "6353986953", "haghariya@gmail.com", "aghariya@h")
--   ("Naimish","7984087441", "ngondaliya@gmail.com","gondaliya@n")
-- ============================================================
CREATE TABLE IF NOT EXISTS admin1 (
    aid         INT AUTO_INCREMENT PRIMARY KEY,
    aName       VARCHAR(100),
    aPhone      VARCHAR(15),
    aEmailId    VARCHAR(200) UNIQUE NOT NULL,
    password    VARCHAR(200)
);

-- Seed default admins (preserved from Admin() constructor)
INSERT IGNORE INTO admin1 (aName, aPhone, aEmailId, password) VALUES
    ('Saloni',  '9429769132', 'sgorsiya@gmail.com',  'gorsiya@s'),
    ('Husain',  '6353986953', 'haghariya@gmail.com', 'aghariya@h'),
    ('Naimish', '7984087441', 'ngondaliya@gmail.com','gondaliya@n');

-- ============================================================
-- TABLE: properties
-- From dbms.insertProperties() and Admin.addProperty():
--   INSERT INTO properties (rName, rArea, rPath, rExtention, rFacility,
--                           rImage, rAddress, rPrice, OwnerName, OContact, OAddress)
-- Admin.addProperty() area formatting: area1 + " sq ft."
-- rExtention = rPath.substring(rPath.lastIndexOf("."))
-- Cities (from dbms.Sales_Data()): ahmedabad, rajkot, surat, vadodara, bhavnagar
-- Residential types: flat, bungalow, tenement, villa, raw-house
-- Commercial types:  Office, Shop, mall, Showroom
-- Industrial types:  warehouse, factory, manufacturing, workshop
-- ============================================================
CREATE TABLE IF NOT EXISTS properties (
    rId              INT AUTO_INCREMENT PRIMARY KEY,
    rName            VARCHAR(100),
    rAddress         VARCHAR(300),
    rPrice           DOUBLE,
    rArea            VARCHAR(100),
    rFacility        VARCHAR(500),
    rPath            VARCHAR(500),
    rExtention       VARCHAR(10),
    rImage           LONGBLOB,
    OwnerName        VARCHAR(200),
    OContact         BIGINT,
    OAddress         VARCHAR(300),
    cityName         VARCHAR(50),
    propertyCategory VARCHAR(30),
    status           VARCHAR(20)  DEFAULT 'available'
);

-- ============================================================
-- TABLE: loan
-- From Loan.createLoan() in Transaction.java:
--   INSERT INTO loan (bankName, customerId, propertyId, loanAmount, interestRate,
--                     tenureMonths, emiAmount, loanStatus, sanctionDate,
--                     disbursementDate, repaymentStartDate)
-- Banks:
--   'Saurastra bank' → 8.75%
--   'HDFC bank'      → 9.75%
--   'Karnavati bank' → 8.95%
-- Brokerage: totalAmount = loanAmount * 1.015  (1.5%)
-- EMI: (totalAmount * monthlyRate * (1+r)^n) / ((1+r)^n - 1)
--   where r = (interestRate/100)/12
-- Dates all = today (java.sql.Date today = new java.sql.Date(System.currentTimeMillis()))
-- loanStatus default = 'Pending'
-- ============================================================
CREATE TABLE IF NOT EXISTS loan (
    loanId              INT AUTO_INCREMENT PRIMARY KEY,
    bankName            VARCHAR(100),
    customerId          INT,
    propertyId          INT,
    loanAmount          DECIMAL(15,2),
    interestRate        DECIMAL(5,2),
    tenureMonths        INT,
    emiAmount           DECIMAL(15,2),
    loanStatus          VARCHAR(20)  DEFAULT 'Pending',
    sanctionDate        DATE,
    disbursementDate    DATE,
    repaymentStartDate  DATE,
    totalAmount         DECIMAL(15,2),
    createdAt           DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updatedAt           DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customerId) REFERENCES customer(customerId) ON DELETE SET NULL,
    FOREIGN KEY (propertyId) REFERENCES properties(rId)     ON DELETE SET NULL
);

-- ============================================================
-- TABLE: sales_property
-- From Admin.view_properties("sold"): SELECT * FROM sales_property
-- Fields: cityName, rId, rName, rAddress, rPrice, rArea, rFacility, rPath
-- ============================================================
CREATE TABLE IF NOT EXISTS sales_property (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    cityName    VARCHAR(50),
    rId         INT,
    rName       VARCHAR(100),
    rAddress    VARCHAR(300),
    rPrice      DOUBLE,
    rArea       VARCHAR(100),
    rFacility   VARCHAR(500),
    rPath       VARCHAR(500),
    saleType    VARCHAR(20),   -- 'sold' or 'commited' (original spelling preserved)
    customerId  INT,
    soldAt      DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: property_log (alias for committed, mirrors sales_property)
-- From Admin.view_properties("commited"): SELECT * FROM property_log
-- Original spelling "commited" preserved throughout
-- ============================================================
CREATE OR REPLACE VIEW property_log AS
    SELECT * FROM sales_property WHERE saleType = 'commited';

-- ============================================================
-- TABLE: city
-- From City class and SLL linked list (Ds.java):
--   cityNo, cityName, totalFlat, totalBunglow, totalTenement,
--   totalRow_house, total_villa, total
-- Cities from Customer.chooseCity():
--   1.Ahmedabad, 2.Rajkot, 3.Surat, 4.Vadodara, 5.Bhavnagar
-- ============================================================
CREATE TABLE IF NOT EXISTS city (
    cityNo          INT AUTO_INCREMENT PRIMARY KEY,
    cityName        VARCHAR(50) UNIQUE,
    totalFlat       INT DEFAULT 0,
    totalBunglow    INT DEFAULT 0,
    totalTenement   INT DEFAULT 0,
    totalRow_house  INT DEFAULT 0,
    total_villa     INT DEFAULT 0,
    total           INT DEFAULT 0
);

-- Seed cities from Customer.chooseCity()
INSERT IGNORE INTO city (cityName) VALUES
    ('ahmedabad'),
    ('rajkot'),
    ('surat'),
    ('vadodara'),
    ('bhavnagar');

-- ============================================================
-- STORED PROCEDURES
-- ============================================================

DROP PROCEDURE IF EXISTS deleteData;
DELIMITER //
-- From PropertyDAOImpl.deleteProperty():
--   CallableStatement cst = con.prepareCall("{CALL deleteData (?,?)}");
CREATE PROCEDURE deleteData(IN p_city VARCHAR(50), IN p_id INT)
BEGIN
    UPDATE properties SET status = 'sold' WHERE rId = p_id AND cityName = p_city;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS insertResidentialData;
DELIMITER //
-- From Seller.seller_Residential():
--   {call insertResidentialData(?,?,?,?,?,?,?,?,?,?,?,?)}
--   cityName, rName, rAddress, rPrice, rArea, rFacility,
--   imageBlob, rPath, rExtention, sellerName, contact, address
CREATE PROCEDURE insertResidentialData(
    IN p_cityName     VARCHAR(50),
    IN p_rName        VARCHAR(100),
    IN p_rAddress     VARCHAR(300),
    IN p_rPrice       DOUBLE,
    IN p_rArea        VARCHAR(100),
    IN p_rFacility    VARCHAR(500),
    IN p_rImage       LONGBLOB,
    IN p_rPath        VARCHAR(500),
    IN p_rExtention   VARCHAR(10),
    IN p_sellerName   VARCHAR(200),
    IN p_contact      BIGINT,
    IN p_address      VARCHAR(300)
)
BEGIN
    INSERT INTO properties
        (cityName, rName, rAddress, rPrice, rArea, rFacility,
         rImage, rPath, rExtention, OwnerName, OContact, OAddress,
         propertyCategory, status)
    VALUES
        (p_cityName, p_rName, p_rAddress, p_rPrice, p_rArea, p_rFacility,
         p_rImage, p_rPath, p_rExtention, p_sellerName, p_contact, p_address,
         'Residential', 'committed');
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS SearchPropertiesByBudget;
DELIMITER //
-- From dbms.SearchPropertiesByBudgetCaller():
--   "CALL SearchPropertiesByBudget(cityName, maxPrice)"
CREATE PROCEDURE SearchPropertiesByBudget(
    IN p_city     VARCHAR(50),
    IN p_maxPrice DOUBLE
)
BEGIN
    SELECT rId, rName, rAddress, rPrice, rArea, rFacility, rPath
    FROM properties
    WHERE cityName = p_city
      AND rPrice <= p_maxPrice
      AND status = 'available'
    ORDER BY rPrice ASC;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS ShowAllPropertiesPrices;
DELIMITER //
-- From dbms.ShowAllPropertiesPricesCaller():
--   "CALL ShowAllPropertiesPrices()"
CREATE PROCEDURE ShowAllPropertiesPrices()
BEGIN
    SELECT rId, rName, cityName, rPrice, rArea
    FROM properties
    WHERE status = 'available'
    ORDER BY cityName, rPrice ASC;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS CountPropertiesByCity;
DELIMITER //
-- From dbms.countPropertiesByCity():
--   "CALL CountPropertiesByCity(?)" — Admin menu 8
CREATE PROCEDURE CountPropertiesByCity(IN p_city VARCHAR(50))
BEGIN
    SELECT COUNT(*) AS propertyCount
    FROM properties
    WHERE cityName = p_city;
END //
DELIMITER ;

-- ============================================================
-- FUNCTION: GetCustomerContact
-- From dbms.getContacts():
--   "SELECT GetCustomerContact(custId) as contact"
--   Admin menu option 9: "view contact of customer"
-- ============================================================
DROP FUNCTION IF EXISTS GetCustomerContact;
DELIMITER //
CREATE FUNCTION GetCustomerContact(p_custId INT)
RETURNS BIGINT DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_contact BIGINT;
    SELECT OContact INTO v_contact
    FROM properties
    WHERE rId = p_custId
    LIMIT 1;
    RETURN v_contact;
END //
DELIMITER ;

-- ============================================================
-- SAMPLE DATA (optional — for testing)
-- ============================================================
INSERT IGNORE INTO properties
    (rName, rAddress, rPrice, rArea, rFacility, rPath, rExtention, cityName, propertyCategory, status)
VALUES
    ('flat',       'Prahlad Nagar, Ahmedabad',    4500000, '1200 sq ft.', 'Parking, Lift, Security, Gym',        '', '.jpg', 'ahmedabad', 'Residential', 'available'),
    ('bungalow',   'Satellite Road, Ahmedabad',   9800000, '3500 sq ft.', 'Garden, Parking, 5BHK, Swimming Pool','', '.jpg', 'ahmedabad', 'Residential', 'available'),
    ('flat',       '150ft Ring Road, Rajkot',     3200000, '900 sq ft.',  'Parking, 24hr Water, Security',       '', '.jpg', 'rajkot',    'Residential', 'available'),
    ('villa',      'Vesu, Surat',                 7500000, '2800 sq ft.', 'Private Pool, Garden, 4BHK',          '', '.jpg', 'surat',     'Residential', 'available'),
    ('tenement',   'Alkapuri, Vadodara',           1800000, '600 sq ft.',  'Parking, Security',                   '', '.jpg', 'vadodara',  'Residential', 'available'),
    ('Office',     'Adajan, Surat',                6200000, '2000 sq ft.', 'AC, Lift, Parking, 24hr Power',       '', '.jpg', 'surat',     'Commercial',  'available'),
    ('warehouse',  'GIDC Estate, Bhavnagar',       3900000, '5000 sq ft.', 'Loading Dock, 3-Phase Power',         '', '.jpg', 'bhavnagar', 'Industrial',  'available'),
    ('raw-house',  'Karelibaug, Vadodara',         2700000, '1100 sq ft.', 'Parking, 2BHK, Terrace',              '', '.jpg', 'vadodara',  'Residential', 'available'),
    ('Shop',       'MG Road, Rajkot',              5100000, '800 sq ft.',  'Ground Floor, High Footfall Area',    '', '.jpg', 'rajkot',    'Commercial',  'available'),
    ('factory',    'Sachin GIDC, Surat',           8800000, '8000 sq ft.', 'Power Connection, Water, Drainage',   '', '.jpg', 'surat',     'Industrial',  'available');
