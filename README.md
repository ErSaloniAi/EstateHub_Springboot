# 🏡 ESTATEHUB

<div align="center">

### 🌍 Smart Real Estate Management Platform

A modern, secure, and scalable real estate web application built using **Java, Spring Boot, MySQL, Docker, and Thymeleaf**.

---

<img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java" />
<img src="https://img.shields.io/badge/SpringBoot-3.x-brightgreen?style=for-the-badge&logo=springboot" />
<img src="https://img.shields.io/badge/MySQL-Database-blue?style=for-the-badge&logo=mysql" />
<img src="https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker" />
<img src="https://img.shields.io/badge/Security-JWT-red?style=for-the-badge" />

</div>

---

# 📌 About ESTATEHUB

ESTATEHUB is a production-ready real estate platform designed to simplify property management, property discovery, and secure user interactions.

The platform provides:

* Property listing management
* User authentication & authorization
* Property search and filtering
* Role-based access control
* Secure JWT authentication
* Email & OTP verification
* Admin management system
* Dockerized deployment support

---

# 🚀 Features

## 👤 Authentication System

* User Registration
* Secure Login & Logout
* JWT Authentication
* Email Verification
* OTP Verification
* Forgot Password
* Reset Password
* Password Encryption

---

## 🏠 Property Management

* Add Property
* Edit Property
* Delete Property
* Upload Property Images
* Property Search
* Advanced Filters
* Favorite Properties

---

## 📊 Dashboards

### User Dashboard

* Profile Management
* Saved Properties
* User Activity

### Admin Dashboard

* Manage Users
* Manage Properties
* Analytics & Monitoring

---

## 🔐 Security

* Spring Security
* JWT Token Authentication
* Role-Based Authorization
* Session Protection
* Password Encryption

---

# 🛠️ Tech Stack

| Technology      | Usage                 |
| --------------- | --------------------- |
| Java 17         | Backend               |
| Spring Boot 3   | Application Framework |
| Spring MVC      | Web Architecture      |
| Spring Security | Security              |
| Hibernate / JPA | ORM                   |
| MySQL           | Database              |
| Thymeleaf       | Frontend Rendering    |
| Bootstrap       | UI Design             |
| Docker          | Deployment            |
| Maven           | Dependency Management |

---

# 📂 Project Structure

```bash
ESTATEHUB/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   ├── resources/
│   │   │   ├── templates/
│   │   │   ├── static/
│   │   │   └── application.properties
│   │
│   └── test/
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# ⚙️ Installation & Setup

## 1️⃣ Clone Repository

```bash
git clone https://github.com/your-username/estatehub.git
cd estatehub
```

---

## 2️⃣ Configure Database

Update database credentials inside:

```properties
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/estatehub
spring.datasource.username=root
spring.datasource.password=your_password
```

---

## 3️⃣ Run Application

### Using Maven

```bash
mvn clean install
mvn spring-boot:run
```

---

### Using Docker

```bash
docker-compose up --build
```

---

# 🌐 API Modules

* Authentication APIs
* User APIs
* Property APIs
* Admin APIs
* Email & OTP APIs

---

# 📸 Screens Included

✅ Landing Page
✅ Login/Register
✅ Property Listings
✅ Admin Dashboard
✅ User Dashboard
✅ Property Details
✅ Search & Filters

---

# 🔮 Future Enhancements

* AI Property Recommendation
* Google Maps Integration
* Real-time Chat System
* Payment Integration
* Cloud Deployment
* Mobile App Support

---

# 🐳 Docker Support

ESTATEHUB is fully containerized using Docker for easy deployment and scalability.

Run:

```bash
docker-compose up --build
```

---

# 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to branch
5. Open a Pull Request

---

# 📄 License

This project is licensed under the MIT License.

---

# 👩‍💻 Developer

### Saloni Gorsiya

Computer Engineering Student • Full Stack Developer • AI Enthusiast

---

# ⭐ Support

If you like this project, give it a ⭐ on GitHub and support the repository.

---

<div align="center">

### 💙 Built with Java & Spring Boot

</div>
