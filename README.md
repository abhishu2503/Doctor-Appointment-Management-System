# Doctor's Appointment Management System

[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data-JPA-green.svg)](https://spring.io/projects/spring-data-jpa)
[![Database](https://img.shields.io/badge/Database-MySQL%20%7C%20H2-orange.svg)](https://www.mysql.com/)
[![OpenAPI / Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-green.svg)](https://swagger.io/)
[![Build Status](https://img.shields.io/badge/Tests-29%20Passed-success.svg)]()

A robust, enterprise-grade RESTful backend application built with **Spring Boot 3**, **Spring Data JPA**, and **MySQL** that simplifies doctor-patient interactions, automates appointment booking, prevents scheduling conflicts, and provides token-based secure access.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Problem Statement](#problem-statement)
3. [Key Features](#key-features)
4. [Technology Stack](#technology-stack)
5. [Architecture & Design](#architecture--design)
6. [Database Design & ER Model](#database-design--er-model)
7. [Project Directory Structure](#project-directory-structure)
8. [Installation & Setup](#installation--setup)
9. [Configuration & Environment Variables](#configuration--environment-variables)
10. [Running the Application](#running-the-application)
11. [Swagger UI Documentation](#swagger-ui-documentation)
12. [REST API Documentation & Samples](#rest-api-documentation--samples)
13. [Validation & Exception Handling](#validation--exception-handling)
14. [Automated Testing](#automated-testing)
15. [License](#license)

---

## Project Overview

The **Doctor's Appointment Management System** is a backend service designed to modernize patient scheduling and healthcare workflows. Built following industry-standard **Layered Architecture (Controller-Service-Repository-DTO-Entity)**, it enables patients to register securely, browse doctors by medical specialization, book future appointment slots, reschedule or cancel appointments, and view appointment histories without conflict.

---

## Problem Statement

Traditional hospital booking systems often suffer from:
- Manual scheduling errors and double-booked doctor time slots.
- Accidental bookings in the past or scheduling conflicts across simultaneous requests.
- Insecure password storage (e.g., plain text or outdated MD5 hashing).
- Direct entity exposure in API contracts leading to infinite JSON circular serialization.
- Tight coupling, hardcoded database credentials, and lack of automated unit testing.

This project resolves these challenges by introducing:
- **Concurrency & Double-Booking Prevention**: Automated verification ensuring a doctor cannot be assigned multiple active bookings for the same time slot.
- **Secure Password Hashing**: Upgraded to **BCrypt** with salted hashing.
- **DTO Layer**: Complete separation of database entities from incoming/outgoing JSON representations.
- **Token-Based Authentication**: Secure stateless tokens for authenticated patient operations.
- **Centralized Exception Handling**: Standardized HTTP status codes and JSON error responses via `@RestControllerAdvice`.

---

## Key Features

### 1. Patient Management & Authentication
- **Registration**: Validates email format, phone digits, and password length. Ensures duplicate email prevention.
- **BCrypt Password Security**: Passwords are securely hashed with BCrypt before persistence.
- **Authentication**: Generates a unique UUID token upon successful sign-in.
- **Profile Management**: Retrieve and update profile details for authenticated patients.
- **Sign Out**: Invalidates active authentication tokens safely.

### 2. Doctor Directory
- **Doctor Registration & Management**: Add, retrieve, update, and remove doctor records.
- **Specialization Filtering**: Search doctors by medical specialty (e.g., `Cardiology`, `Dermatology`, `Pediatrics`, `Neurology`, etc.).
- **Doctor Schedule Inspection**: View all booked appointments for any specific doctor.

### 3. Appointment Scheduling & Conflict Engine
- **Slot Booking**: Validates that the appointment date/time is strictly in the future (`@Future`).
- **Conflict Prevention**: Pre-booking validation rejects double bookings for both doctors and patients at identical slots.
- **Status Tracking**: Explicit status lifecycles (`BOOKED`, `CANCELLED`, `COMPLETED`).
- **Ownership Verification**: Patients can only view, reschedule, or cancel their own appointments.
- **Soft Cancellation**: Updates status to `CANCELLED` instead of destructive physical deletion.

---

## Technology Stack

| Layer / Concern | Technology |
|---|---|
| **Language** | Java 17 LTS |
| **Framework** | Spring Boot 3.1.4 |
| **Web Layer** | Spring MVC (RESTful Controllers) |
| **Persistence** | Spring Data JPA / Hibernate 6 |
| **Database** | MySQL 8.x (Production/Local), H2 (In-Memory for Tests) |
| **Security / Hashing** | Spring Security Crypto (BCrypt) |
| **Validation** | Jakarta Bean Validation (`hibernate-validator`) |
| **Documentation** | SpringDoc OpenAPI 3.0 / Swagger UI |
| **Boilerplate Reduction** | Project Lombok |
| **Build Tool** | Apache Maven |
| **Testing** | JUnit 5, Mockito |

---

## Architecture & Design

The project strictly follows the **Layered Architectural Pattern**:

```
[ Client / Swagger / Postman ]
               |
               v HTTP (JSON)
+---------------------------------------------------+
|               Controller Layer                    |
|  - PatientController                              |
|  - DoctorController                               |
|  - AppointmentController                          |
|  - GlobalExceptionHandler                         |
+---------------------------------------------------+
               |
               v DTOs / Method Calls
+---------------------------------------------------+
|                Service Layer                      |
|  - PatientService                                 |
|  - DoctorService                                  |
|  - AppointmentService                             |
|  - AuthenticationService                          |
|  - Business rules, BCrypt, Conflict checks        |
+---------------------------------------------------+
               |
               v Domain Entities
+---------------------------------------------------+
|               Repository Layer                    |
|  - IPatientRepo                                   |
|  - IDoctorRepo                                    |
|  - IAppointmentRepo                               |
|  - ITokenRepo                                     |
+---------------------------------------------------+
               |
               v SQL Queries
+---------------------------------------------------+
|            Database Layer (MySQL / H2)            |
+---------------------------------------------------+
```

### Key Architectural Principles:
1. **Separation of Concerns**: Controllers only handle HTTP contracts; Services contain business logic; Repositories handle database persistence.
2. **Data Transfer Objects (DTOs)**: Internal JPA entities are never exposed directly to prevent unwanted data leaks and Jackson infinite recursion.
3. **Constructor Injection**: All dependencies are injected via constructors (`@RequiredArgsConstructor`), ensuring immutability and ease of unit testing.

---

## Database Design & ER Model

```
+--------------------+        1:N        +-----------------------+
|      PATIENTS      |<------------------|      APPOINTMENTS     |
+--------------------+                   +-----------------------+
| PK patient_id      |                   | PK appointment_id     |
|    first_name      |                   |    appointment_time   |
|    last_name       |                   |    status             |
|    email (UNIQUE)  |                   |    reason             |
|    password (HASH) |                   |    booking_time       |
|    contact         |                   | FK patient_id         |
+--------------------+                   | FK doctor_id          |
         | 1                             +-----------------------+
         |                                           | N
         | 1:1                                       |
         v                                           | 1
+-------------------------+              +-----------------------+
|  AUTHENTICATION_TOKENS  |              |        DOCTORS        |
+-------------------------+              +-----------------------+
| PK token_id             |              | PK doctor_id          |
|    token (UNIQUE)       |              |    doctor_name        |
|    creation_time        |              |    specialization     |
| FK fk_patient_id        |              |    contact            |
+-------------------------+              |    email              |
                                         |    qualification      |
                                         |    consultation_fee   |
                                         +-----------------------+
```

### Table Relationships:
- **Doctor to Appointment**: `1 : Many` (One doctor can have many appointments).
- **Patient to Appointment**: `1 : Many` (One patient can book multiple appointments over time).
- **Patient to AuthenticationToken**: `1 : 1` (Each authenticated patient holds an active session token).

---

## Project Directory Structure

```
Doctor-s-Appointment-Application/
├── pom.xml
├── .gitignore
├── README.md
├── INTERVIEW_GUIDE.md
├── src/
│   ├── main/
│   │   ├── java/com/geekster/DoctorsAppointmentApplication/
│   │   │   ├── DoctorAppApplication.java
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java          # BCrypt PasswordEncoder Bean
│   │   │   │   └── OpenApiConfig.java      # Swagger OpenAPI 3.0 Setup
│   │   │   ├── controller/
│   │   │   │   ├── AppointmentController.java
│   │   │   │   ├── DoctorController.java
│   │   │   │   └── PatientController.java
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java        # Standardized API response wrapper
│   │   │   │   ├── ErrorResponse.java      # Structured error schema
│   │   │   │   ├── SignUpInput.java
│   │   │   │   ├── SignUpOutput.java
│   │   │   │   ├── SignInInput.java
│   │   │   │   ├── SignInOutput.java
│   │   │   │   ├── PatientProfileResponse.java
│   │   │   │   ├── PatientUpdateRequest.java
│   │   │   │   ├── DoctorRequest.java
│   │   │   │   ├── DoctorResponse.java
│   │   │   │   ├── AppointmentBookingRequest.java
│   │   │   │   ├── AppointmentUpdateRequest.java
│   │   │   │   └── AppointmentResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── AppointmentConflictException.java
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── InvalidCredentialsException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── UnauthorizedAccessException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/
│   │   │   │   ├── Appointment.java
│   │   │   │   ├── AppointmentStatus.java
│   │   │   │   ├── AuthenticationToken.java
│   │   │   │   ├── Doctor.java
│   │   │   │   ├── Patient.java
│   │   │   │   └── Specialization.java
│   │   │   ├── repository/
│   │   │   │   ├── IAppointmentRepo.java
│   │   │   │   ├── IDoctorRepo.java
│   │   │   │   ├── IPatientRepo.java
│   │   │   │   └── ITokenRepo.java
│   │   │   └── service/
│   │   │       ├── AppointmentService.java
│   │   │       ├── AuthenticationService.java
│   │   │       ├── DoctorService.java
│   │   │       └── PatientService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application.properties.example
│   └── test/
│       ├── java/com/geekster/DoctorsAppointmentApplication/
│       │   ├── DoctorAppApplicationTests.java
│       │   └── service/
│       │       ├── AppointmentServiceTest.java
│       │       ├── DoctorServiceTest.java
│       │       └── PatientServiceTest.java
│       └── resources/
│           └── application.properties       # H2 Test DB Configuration
```

---

## Installation & Setup

### Prerequisites
- **Java Development Kit (JDK 17 or higher)**
- **Apache Maven 3.8+**
- **MySQL Server 8.x**

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/Doctors-Appointment-Application.git
cd Doctors-Appointment-Application
```

### 2. Configure MySQL Database
Create a database in your local MySQL instance:
```sql
CREATE DATABASE HospitalManagement;
```

---

## Configuration & Environment Variables

Sensitive credentials are parameterized using environment variables with safe development defaults.

Create your environment variables or update `application.properties`:

| Variable | Description | Default Value |
|---|---|---|
| `PORT` | Application server port | `8080` |
| `DB_URL` | JDBC Connection URL | `jdbc:mysql://localhost:3306/HospitalManagement?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL database username | `root` |
| `DB_PASSWORD` | MySQL database password | `password` |

Refer to `src/main/resources/application.properties.example` for reference.

---

## Running the Application

Run the application using Maven:
```bash
mvn clean spring-boot:run
```

Or build the executable JAR and run it:
```bash
mvn clean package -DskipTests
java -jar target/Doctors-Appointment-Application-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`.

---

## Swagger UI Documentation

Once the application is running, interactively inspect and test all API endpoints using Swagger UI:

- **Swagger UI URL:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## REST API Documentation & Samples

### 1. Patient APIs (`/patient`)

#### Patient Sign-Up (Registration)
- **Endpoint:** `POST /patient/signup`
- **Status:** `201 CREATED`
- **Request Body:**
```json
{
  "userFirstName": "Rahul",
  "userLastName": "Sharma",
  "userEmail": "rahul.sharma@example.com",
  "userPassword": "Password@123",
  "userContact": "9876543210"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Patient registered successfully",
  "data": {
    "status": "SUCCESS",
    "message": "Patient registered successfully",
    "patientId": 1
  },
  "timestamp": "2026-11-15T13:30:00"
}
```

#### Patient Sign-In (Login)
- **Endpoint:** `POST /patient/signin`
- **Status:** `200 OK`
- **Request Body:**
```json
{
  "patientEmail": "rahul.sharma@example.com",
  "patientPassword": "Password@123"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "status": "Authentication Successful",
    "token": "45d7a8e2-b13c-4a37-bce4-6f81014dfa45",
    "patientEmail": "rahul.sharma@example.com",
    "patientName": "Rahul Sharma"
  },
  "timestamp": "2026-11-15T13:31:00"
}
```

#### Get Patient Profile
- **Endpoint:** `GET /patient/profile?userEmail=rahul.sharma@example.com&token=45d7a8e2-b13c-4a37-bce4-6f81014dfa45`
- **Status:** `200 OK`
- **Response:**
```json
{
  "success": true,
  "message": "Profile fetched successfully",
  "data": {
    "patientId": 1,
    "patientFirstName": "Rahul",
    "patientLastName": "Sharma",
    "patientEmail": "rahul.sharma@example.com",
    "patientContact": "9876543210"
  },
  "timestamp": "2026-11-15T13:32:00"
}
```

#### Patient Sign-Out (Logout)
- **Endpoint:** `POST /patient/signout?userEmail=rahul.sharma@example.com&token=45d7a8e2-b13c-4a37-bce4-6f81014dfa45`
- **Status:** `200 OK`

---

### 2. Doctor APIs (`/doctor`)

#### Add Doctor
- **Endpoint:** `POST /doctor`
- **Status:** `201 CREATED`
- **Request Body:**
```json
{
  "doctorName": "Dr. Anita Roy",
  "specialization": "Cardiology",
  "doctorContact": "9123456780",
  "doctorEmail": "anita.roy@hospital.com",
  "qualification": "MBBS, MD Cardiology",
  "consultationFee": 800.0
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Doctor added successfully",
  "data": {
    "doctorId": 1,
    "doctorName": "Dr. Anita Roy",
    "specialization": "Cardiology",
    "doctorContact": "9123456780",
    "doctorEmail": "anita.roy@hospital.com",
    "qualification": "MBBS, MD Cardiology",
    "consultationFee": 800.0
  },
  "timestamp": "2026-11-15T13:33:00"
}
```

#### Get All Doctors
- **Endpoint:** `GET /doctor`
- **Status:** `200 OK`

#### Get Doctor by ID
- **Endpoint:** `GET /doctor/1`
- **Status:** `200 OK`

#### Search Doctors by Specialization
- **Endpoint:** `GET /doctor/specialization/Cardiology`
- **Status:** `200 OK`

#### View Doctor Appointments
- **Endpoint:** `GET /doctor/1/appointments`
- **Status:** `200 OK`

---

### 3. Appointment APIs (`/appointment`)

#### Book an Appointment
- **Endpoint:** `POST /appointment/book?userEmail=rahul.sharma@example.com&token=45d7a8e2-b13c-4a37-bce4-6f81014dfa45`
- **Status:** `201 CREATED`
- **Request Body:**
```json
{
  "doctorId": 1,
  "appointmentTime": "2026-12-01T10:30:00",
  "reason": "Routine cardiac checkup"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Appointment booked successfully",
  "data": {
    "appointmentId": 101,
    "appointmentTime": "2026-12-01T10:30:00",
    "status": "BOOKED",
    "reason": "Routine cardiac checkup",
    "bookingTime": "2026-11-15T13:34:00",
    "doctorId": 1,
    "doctorName": "Dr. Anita Roy",
    "doctorSpecialization": "Cardiology",
    "patientId": 1,
    "patientName": "Rahul Sharma",
    "patientEmail": "rahul.sharma@example.com",
    "patientContact": "9876543210"
  },
  "timestamp": "2026-11-15T13:34:00"
}
```

#### View Authenticated Patient's Appointments
- **Endpoint:** `GET /appointment/my-appointments?userEmail=rahul.sharma@example.com&token=45d7a8e2-b13c-4a37-bce4-6f81014dfa45`
- **Status:** `200 OK`

#### Cancel an Appointment
- **Endpoint:** `PUT /appointment/101/cancel?userEmail=rahul.sharma@example.com&token=45d7a8e2-b13c-4a37-bce4-6f81014dfa45` (or `DELETE /appointment/101?...`)
- **Status:** `200 OK`
- **Response:**
```json
{
  "success": true,
  "message": "Appointment cancelled successfully",
  "data": {
    "appointmentId": 101,
    "status": "CANCELLED"
  },
  "timestamp": "2026-11-15T13:35:00"
}
```

#### Reschedule an Appointment
- **Endpoint:** `PUT /appointment/101?userEmail=rahul.sharma@example.com&token=45d7a8e2-b13c-4a37-bce4-6f81014dfa45`
- **Status:** `200 OK`
- **Request Body:**
```json
{
  "appointmentTime": "2026-12-02T11:00:00",
  "reason": "Rescheduled due to personal emergency"
}
```

---

## Validation & Exception Handling

All incoming requests are strictly validated using **Jakarta Bean Validation**. Centralized exceptions produce a consistent JSON payload:

```json
{
  "timestamp": "2026-11-15T13:36:00",
  "status": 409,
  "error": "Conflict",
  "message": "Doctor Dr. Anita Roy is already booked at 2026-12-01T10:30:00. Please select another slot.",
  "path": "/appointment/book"
}
```

| HTTP Status | Exception Name | Scenario |
|---|---|---|
| **400 Bad Request** | `BadRequestException`, `MethodArgumentNotValidException` | Booking in the past, invalid contact format, cancelling already cancelled slot |
| **401 Unauthorized** | `InvalidCredentialsException` | Incorrect password or unregistered email during login |
| **403 Forbidden** | `UnauthorizedAccessException` | Missing/invalid token, or attempting to cancel another patient's appointment |
| **404 Not Found** | `ResourceNotFoundException` | Non-existent doctor ID or appointment ID |
| **409 Conflict** | `DuplicateResourceException`, `AppointmentConflictException` | Duplicate patient email, or doctor double-booking at identical slot |
| **500 Server Error** | `Exception` | Unexpected server-side issues |

---

## Automated Testing

The project has thorough automated unit tests covering service business logic and validation rules:

```bash
mvn clean test
```

### Test Suite Summary:
- **`AppointmentServiceTest`**: 12 tests covering booking, future slot checks, doctor double-booking conflict, patient conflict, unauthorized cancellation, non-existent doctor/appointment, and rescheduling.
- **`DoctorServiceTest`**: 8 tests covering doctor onboarding, directory lookups, specialization filters, profile updates, deletions, and schedule retrieval.
- **`PatientServiceTest`**: 8 tests covering registration, duplicate email rejection, BCrypt login matching, invalid password rejection, profile retrieval, profile updates, and logout token invalidation.
- **`DoctorAppApplicationTests`**: Spring Boot application context load verification with H2 in-memory DB.

**Total Test Count:** 29 Tests | **Failures:** 0 | **Errors:** 0

---

## License

This project is licensed under the [BSD 3-Clause License](LICENSE).
