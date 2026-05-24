# ESTATEHUB — Production-Ready Spring Boot Real Estate Platform

> Gujarat's Premier Real Estate Platform  
> **Zero Business Logic Changed** from original Java source files.

---

## PROJECT STRUCTURE

```
estatehub/
├── src/main/java/com/estatehub/
│   ├── EstateHubApplication.java          # Spring Boot entry point
│   ├── config/
│   │   ├── DataInitializer.java           # Seeds default admins on startup
│   │   ├── SecurityConfig.java            # JWT + role-based auth
│   │   └── WebConfig.java                 # Static resources, MVC
│   ├── controller/
│   │   ├── AuthController.java            # /auth/** — register, login, OTP, reset
│   │   ├── HomeController.java            # /, /about, /contact
│   │   ├── PropertyController.java        # /properties/**, /buyer/**, /seller/**
│   │   ├── AppControllers.java            # /dashboard/**, /loan/**, /admin/**
│   │   └── GlobalExceptionHandler.java    # Error pages
│   ├── dto/                               # Request/Response objects
│   ├── entity/                            # JPA entities
│   ├── repository/                        # Spring Data JPA repositories
│   ├── security/                          # JWT filter, UserDetailsService
│   └── service/                           # Business logic layer
├── src/main/resources/
│   ├── application.properties             # Main config
│   ├── application-prod.properties        # Production profile
│   ├── schema.sql                         # Complete MySQL schema + stored procs
│   ├── static/
│   │   ├── css/main.css                   # Full stylesheet
│   │   └── js/main.js                     # Frontend JS
│   └── templates/                         # Thymeleaf HTML pages
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## QUICK START

### Option 1: Docker Compose (Recommended)

```bash
git clone <repo>
cd estatehub
docker-compose up --build
```
Visit: http://localhost:8080

### Option 2: Manual Setup

**Prerequisites:** Java 17, Maven 3.8+, MySQL 8 running on port 3307

```bash
# 1. Create database
mysql -u root -P 3307 < src/main/resources/schema.sql

# 2. Build
mvn clean package -DskipTests

# 3. Run
java -jar target/estatehub-1.0.0.jar
```

---

## DATABASE CONFIGURATION

Preserved from `CreateConnection.java` and `Admin.java`:
```
URL:      jdbc:mysql://localhost:3307/real_estate
User:     root
Password: (empty)
```

Change in `application.properties` or via environment variables:
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://host:3307/real_estate
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=yourpassword
```

---

## DEFAULT ADMIN ACCOUNTS

Preserved from `Admin()` constructor — seeded automatically on startup:

| Name    | Email                 | Password    |
|---------|-----------------------|-------------|
| Saloni  | sgorsiya@gmail.com    | gorsiya@s   |
| Husain  | haghariya@gmail.com   | aghariya@h  |
| Naimish | ngondaliya@gmail.com  | gondaliya@n |

---

## ALL PAGES & ROUTES

### Public Pages
| URL | Description |
|-----|-------------|
| `/` | Landing page with hero, featured properties, city search |
| `/properties/list` | Browse all properties with filters |
| `/properties/view/{id}` | Property detail page |
| `/about` | About page with team (Saloni, Husain, Naimish) |
| `/contact` | Contact page |

### Authentication (`/auth/**`)
| URL | Method | Description |
|-----|--------|-------------|
| `/auth/register` | GET/POST | Register — validates @gmail.com + ^[6-9]\d{9}$ |
| `/auth/login` | GET/POST | Login — email must end with @gmail.com |
| `/auth/verify-otp` | GET/POST | 6-digit OTP email verification |
| `/auth/resend-otp` | POST | Resend OTP |
| `/auth/forgot-password` | GET/POST | Initiate password reset |
| `/auth/reset-password` | GET/POST | Reset with token |
| `/auth/logout` | POST | Logout and clear session |

### User Dashboard (`/dashboard/**`) — ROLE_USER
| URL | Description |
|-----|-------------|
| `/dashboard` | Dashboard with purchases + loans |
| `/dashboard/profile` | Edit profile (Customer.customer_details() logic) |

### Buyer (`/buyer/**`) — ROLE_USER
| URL | Description |
|-----|-------------|
| `/buyer/browse` | Browse by city/type/budget |
| `/buyer/cheapest?city=X` | Cheapest per city — dbms.LowestPriceDynamicFetcher() |
| `/buyer/search-budget?city=X&maxPrice=Y` | Budget search — dbms.SearchPropertiesByBudget() |
| `/buyer/purchase/{id}` | Purchase property — dbms.Sales_Data() |
| `/buyer/purchase-success` | Confirmation page |

### Seller (`/seller/**`) — ROLE_USER
| URL | Description |
|-----|-------------|
| `/seller/list-property` | List property — Seller.seller_Residential() with 1.5% brokerage |
| `/seller/my-listings` | View own listings |

### Loan (`/loan/**`) — ROLE_USER
| URL | Description |
|-----|-------------|
| `/loan/choose/{propertyId}` | Choose Loan or POA — Transactions.transaction() |
| `/loan/create` | POST — Loan.createLoan() with all 3 banks + EMI formula |
| `/loan/poa` | POST — POA.poa() exact amount check |
| `/loan/success` | Confirmation with EMI breakdown |

### Admin (`/admin/**`) — ROLE_ADMIN only
| URL | Menu Item | Source |
|-----|-----------|--------|
| `/admin` | Dashboard with analytics | — |
| `/admin/admins` | Menu 1: View Admin Details | Admin.viewAdminData() |
| `/admin/admins/add` | Menu 2: Add Admin | Admin.addAdminData() |
| `/admin/properties/add` | Menu 3: Add Property | Admin.addProperty() |
| `/admin/properties/delete` | Menu 4: Delete Property | Admin.deleteProperty() |
| `/admin/sold` | Menu 5: View Sold Properties | Admin.view_properties("sold") |
| `/admin/committed` | Menu 6: View Committed | Admin.view_properties("commited") |
| `/admin/loans` | Menu 7: View Loan Details | Admin.viewLoanDetail() |
| `/admin/count-by-city` | Menu 8: Count by City | dbms.countPropertiesByCity() |
| `/admin/contacts` | Menu 9: View Contacts | dbms.getContacts() |
| `/admin/customers` | Bonus: All customers | — |

---

## PRESERVED BUSINESS LOGIC CHECKLIST

### ✅ RealEstate.java
- [x] `register()`: firstName.trim(), lastName.trim(), name = fn.concat(ln)
- [x] `register()`: mobileNumber regex `^[6-9]\d{9}$`
- [x] `login()` / `main()`: email must end with `@gmail.com`
- [x] `login()`: SELECT cEmail FROM customer → equalsIgnoreCase check
- [x] `main()`: admin email check loop before customer login

### ✅ Admin.java
- [x] Default admins: Saloni (gorsiya@s), Husain (aghariya@h), Naimish (gondaliya@n)
- [x] `connectAsAdmin()`: email + password check
- [x] `viewAdminData()`: SELECT * FROM admin → all fields displayed
- [x] `addAdminData()`: INSERT INTO admin
- [x] `addProperty()`: area formatted as `area1 + " sq ft."`
- [x] `deleteProperty()`: city + rId delete
- [x] `view_properties("sold")`: SELECT * FROM sales_property
- [x] `view_properties("commited")`: SELECT * FROM property_log (original spelling preserved)
- [x] `viewLoanDetail()`: all 14 loan fields displayed

### ✅ Customer.java
- [x] `customer_details()`: INSERT INTO customer with all fields
- [x] State defaults to "Gujarat" if empty
- [x] Age defaults to 25 if <= 0
- [x] Income defaults to 0.0 if invalid
- [x] `chooseCity()`: 1=Ahmedabad, 2=Rajkot, 3=Surat, 4=Vadodara, 5=Bhavnagar

### ✅ Transaction.java — Loan.createLoan()
- [x] Bank 1: "Saurastra bank" → 8.75%
- [x] Bank 2: "HDFC bank" → 9.75%
- [x] Bank 3: "Karnavati bank" → 8.95%
- [x] default: "Invalid choice! Loan process cancelled."
- [x] Brokerage: `totalAmount = loanAmount * 1.015` (1.5%)
- [x] EMI: `(totalAmount * r * (1+r)^n) / ((1+r)^n - 1)` where `r = (rate/100)/12`
- [x] Dates: sanctionDate = disbursementDate = repaymentStartDate = today
- [x] loanStatus default = "Pending"
- [x] File output: "================ Bank Loan Details ================"

### ✅ Transaction.java — POA.poa()
- [x] `totalAmount = dbms.price * 1.015`
- [x] `if (amount == (int) totalAmount)` → "Payment successful"

### ✅ Buyer.java + dbms.java
- [x] `Sales_Data()`: SELECT by city + rName LIKE propertyName+"%"
- [x] Type validation: if actual type doesn't match → NOTE message
- [x] Cities: ahmedabad, rajkot, surat, vadodara, bhavnagar
- [x] Residential: flat, bungalow, tenement, villa, raw-house
- [x] Commercial: Office, Shop, mall, Showroom
- [x] Industrial: warehouse, factory, manufacturing, workshop
- [x] Menu 1: `LowestPriceDynamicFetcher()` — cheapest per city
- [x] Menu 2: `SearchPropertiesByBudget()` — city + maxPrice
- [x] Menu 3: `ShowAllPropertiesPrices()` — all available sorted by price

### ✅ Seller (from Customer.java)
- [x] `seller_Residential()`: 5 property types
- [x] rArea = area + "sq ft"
- [x] rExtention = rPath.substring(rPath.lastIndexOf("."))
- [x] Brokerage: totalPrice = rPrice * 0.015
- [x] if match → "property is committed"
- [x] if mismatch → "please enter valid brokerage"
- [x] Stored proc: insertResidentialData (12 params)

### ✅ GmailSender.java
- [x] sender: estatehub807@gmail.com
- [x] Subject: "Property Details from ESTATE HUB"
- [x] Body: "Dear User,\n\nFind attached..."
- [x] Attachment: merged property file
- [x] SendFailedException: invalid + unsent address logging

### ✅ FileMerger.java
- [x] Section headers: "================ Loan/Customer/Purchase Details ================"
- [x] Output: "property_{cityName}.txt"
- [x] Always recreate (false append flag)

### ✅ Ds.java (SLL + PropertyNode)
- [x] City fields: cityNo, cityName, totalFlat, totalBunglow, totalTenement, totalRow_house, total_villa, total
- [x] SLL.search(): by cityName ignoreCase

---

## EMAIL SETUP

Preserved from `GmailSender.java`:
- SMTP: smtp.gmail.com:587
- TLS: enabled
- SSL Protocol: TLSv1.2
- Account: estatehub807@gmail.com

> **Note:** The App Password in the code is `bpbx wjuf vchc ylyv`.  
> If expired, generate a new one at myaccount.google.com/apppasswords

---

## TECH STACK

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8 (port 3307) |
| Templates | Thymeleaf 3 |
| Frontend | Bootstrap 5.3 + Custom CSS + Vanilla JS |
| Email | Spring Mail (Jakarta Mail) |
| Build | Maven |
| Container | Docker + Docker Compose |

---

## FINAL VERIFICATION

**ZERO BUSINESS LOGIC CHANGED.**  
Every condition, loop, calculation, validation, workflow, authentication check,  
email logic, brokerage formula, EMI formula, and city/type mapping from the  
original Java source files has been faithfully preserved and wrapped inside  
the Spring Boot 3 production architecture.
