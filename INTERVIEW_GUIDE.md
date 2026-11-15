# Doctor's Appointment Management System — Interview Preparation Guide

> **Target Audience:** Final-year Engineering Students & Fresher Candidates preparing for Java Backend Developer and Software Development Engineer (SDE-1) interviews.

---

## Table of Contents

1. [High-Level Project Pitch (30-Second Elevator Pitch)](#1-high-level-project-pitch-30-second-elevator-pitch)
2. [Core Conceptual Questions & Simple Answers](#2-core-conceptual-questions--simple-answers)
   - [What problem does this project solve?](#what-problem-does-this-project-solve)
   - [Why did you choose Spring Boot?](#why-did-you-choose-spring-boot)
   - [Why did you use Spring Data JPA / Hibernate?](#why-did-you-use-spring-data-jpa--hibernate)
   - [How does MVC / Layered architecture work in this project?](#how-does-mvc--layered-architecture-work-in-this-project)
   - [Explain the complete request-to-database flow.](#explain-the-complete-request-to-database-flow)
   - [Explain Entity, Repository, Service, and Controller with real examples.](#explain-entity-repository-service-and-controller-with-real-examples)
   - [Explain the database relationships (One-to-Many, Many-to-One, One-to-One).](#explain-the-database-relationships)
   - [How does the appointment booking workflow work step-by-step?](#how-does-the-appointment-booking-workflow-work-step-by-step)
   - [How do you prevent double-booking of doctor slots?](#how-do-you-prevent-double-booking-of-doctor-slots)
   - [How does password hashing work (BCrypt vs MD5)?](#how-does-password-hashing-work-bcrypt-vs-md5)
   - [What is Dependency Injection (DI) and Inversion of Control (IoC)?](#what-is-dependency-injection-di-and-inversion-of-control-ioc)
   - [What is a REST API and what HTTP status codes did you use?](#what-is-a-rest-api-and-what-http-status-codes-did-you-use)
   - [What is a DTO and why didn't you expose entities directly?](#what-is-a-dto-and-why-didnt-you-expose-entities-directly)
   - [How does centralized exception handling work in Spring Boot?](#how-does-centralized-exception-handling-work-in-spring-boot)
   - [What challenges did you encounter and how did you resolve them?](#what-challenges-did-you-encounter-and-how-did-you-resolve-them)
3. [Deep Dive into Key Classes and Methods](#3-deep-dive-into-key-classes-and-methods)
4. [Sample Interview Dialogue / Mock Q&A](#4-sample-interview-dialogue--mock-qa)

---

## 1. High-Level Project Pitch (30-Second Elevator Pitch)

> *"I developed the Doctor's Appointment Management System, a production-style RESTful backend application built with Java 17, Spring Boot 3, and Spring Data JPA. The system manages patient authentication, doctor specialization catalogs, and an intelligent appointment scheduling engine. The core highlight of the project is its conflict-prevention mechanism that stops double-booking of doctor time slots, enforces past-date validation, uses BCrypt for salted password hashing, and implements clean DTO-based separation and centralized exception handling. I also wrote 29 unit tests using JUnit 5 and Mockito to verify the critical business workflows."*

---

## 2. Core Conceptual Questions & Simple Answers

### What problem does this project solve?
- **Manual Scheduling Errors:** In clinics, patients often get double-booked for the same doctor at the same hour, causing long wait times and chaos.
- **Data Integrity & Security:** Healthcare data requires privacy. Patients must not see or modify other patients' appointments, and passwords must never be stored in plain text or weak hashes like MD5.
- **Architectural Solution:** This project automates validation (checking if doctor exists, if slot is in future, if doctor is already occupied), protects endpoints with token verification, and hashes passwords using BCrypt.

---

### Why did you choose Spring Boot?
1. **Auto-Configuration:** Spring Boot automatically configures Datasources, Jackson JSON mappers, and Hibernate based on classpath dependencies.
2. **Embedded Web Server:** It comes with embedded Tomcat, eliminating the need to install or configure external application servers.
3. **Production-Ready Features:** Out-of-the-box support for validation (`hibernate-validator`), metrics, structured error handling, and OpenAPI/Swagger documentation.
4. **Rapid Development:** Eliminates hundreds of lines of XML configuration that classic Spring required.

---

### Why did you use Spring Data JPA / Hibernate?
- **Object-Relational Mapping (ORM):** Hibernate bridges the gap between Java Object-Oriented concepts (classes, references) and relational database concepts (tables, foreign keys).
- **Reduced Boilerplate:** In plain JDBC, writing CRUD operations requires repetitive `PreparedStatement`, `ResultSet`, connection pooling, and SQL code. Spring Data JPA generates queries dynamically from method names such as `findBySpecialization(...)` or `existsByDoctorAndAppointmentTimeAndStatus(...)`.
- **Dialect Abstraction:** Switching from MySQL in production to an in-memory H2 database for testing requires only changing configuration properties without altering repository code.

---

### How does MVC / Layered architecture work in this project?
The project divides responsibility into decoupled layers:
1. **Controller Layer (Presentation/Web):** Exposes REST endpoints (`@RestController`), parses JSON request bodies, validates input via `@Valid`, and delegates to services.
2. **Service Layer (Business Logic):** Houses core business logic—such as double-booking validation, password hashing, and authorization checks.
3. **Repository Layer (Data Access):** Interfaces extending `JpaRepository` that query the database.
4. **Entity / Model Layer (Data Model):** JPA entities (`@Entity`) mapped to database tables.
5. **DTO Layer (Contract):** Separate classes for request payloads and response representations.

---

### Explain the complete request-to-database flow.

Let's trace what happens when a patient clicks **"Book Appointment"**:

```
1. Client (Browser / Swagger / Postman)
   Sends HTTP POST /appointment/book with JSON body:
   { "doctorId": 1, "appointmentTime": "2026-12-01T10:30:00", "reason": "Fever" }
   along with userEmail & token query parameters.
      │
      ▼
2. DispatcherServlet (Spring Boot Front Controller)
   Routes request to AppointmentController.bookAppointment(...)
      │
      ▼
3. Bean Validation (@Valid)
   Validates constraints: @NotNull doctorId, @Future appointmentTime, @NotBlank reason.
   If invalid, immediately intercepted by GlobalExceptionHandler (HTTP 400).
      │
      ▼
4. Controller -> AppointmentService.bookAppointment(...)
   Invokes business logic method with request DTO and auth credentials.
      │
      ▼
5. AuthenticationService.getAuthenticatedPatient(userEmail, token)
   Queries ITokenRepo. Verifies token is valid and belongs to userEmail.
      │
      ▼
6. Business Validations in Service:
   a. Queries IDoctorRepo.findById(doctorId) -> checks doctor exists.
   b. Verifies appointmentTime.isAfter(LocalDateTime.now()).
   c. Queries IAppointmentRepo.existsByDoctorAndAppointmentTimeAndStatus(...)
      -> Checks if doctor already has a BOOKED slot at that time.
      │
      ▼
7. Entity Creation & Persistence:
   Constructs Appointment entity with status = BOOKED.
   Calls IAppointmentRepo.save(appointment).
   Hibernate generates and executes SQL INSERT into appointments table.
      │
      ▼
8. Mapping to Response DTO:
   Converts persisted entity into AppointmentResponse DTO (masks internal entity fields).
      │
      ▼
9. HTTP 201 Created Response:
   Wrapped in ApiResponse.success(...) and returned to the client as JSON.
```

---

### Explain Entity, Repository, Service, and Controller with real examples.

| Component | Annotation | Purpose | Project Example |
|---|---|---|---|
| **Entity** | `@Entity`, `@Table` | Maps a Java class directly to a database table. Represents persistent state. | `Doctor.java` maps to the `doctors` table, containing fields like `doctorId`, `doctorName`, `specialization`. |
| **Repository** | `@Repository` | Handles DB CRUD operations and custom query execution using Spring Data JPA. | `IDoctorRepo.java` provides `findBySpecialization(Specialization spec)`. |
| **Service** | `@Service` | Implements business logic, transaction boundaries (`@Transactional`), and security rules. | `AppointmentService.java` validates future dates and prevents double-booking before saving. |
| **Controller** | `@RestController` | Entry point for HTTP requests. Handles routing, input validation, and HTTP response codes. | `AppointmentController.java` maps `POST /appointment/book` to the service method. |

---

### Explain the database relationships.

In our schema:
1. **Doctor to Appointment (`@OneToMany` / `@ManyToOne`):**
   - One doctor can have many appointments (`@OneToMany`).
   - Many appointments refer to one doctor (`@ManyToOne`).
   - The foreign key `doctor_id` resides in the `appointments` table.
2. **Patient to Appointment (`@OneToMany` / `@ManyToOne`):**
   - One patient can book multiple appointments over time (`@OneToMany`).
   - Many appointments belong to a single patient (`@ManyToOne`).
   - The foreign key `patient_id` resides in the `appointments` table.
3. **Patient to AuthenticationToken (`@OneToOne`):**
   - Each patient has at most one active session authentication token (`@OneToOne`).
   - The foreign key `fk_patient_id` resides in the `authentication_tokens` table.

---

### How does the appointment booking workflow work step-by-step?

1. **Authentication Check:** The token and email are validated against the database.
2. **Doctor Validation:** Verifies that the requested `doctorId` exists. If not, throws `ResourceNotFoundException`.
3. **Future Slot Check:** Ensures the requested slot is not in the past (`@Future` & `appointmentTime.isBefore(now)`).
4. **Doctor Double-Booking Check:** Checks if the doctor already has an active appointment with status `BOOKED` at the exact same timestamp. If yes, throws `AppointmentConflictException`.
5. **Patient Conflicting Slot Check:** Checks if the calling patient already has another active appointment at that timestamp.
6. **Persistence:** Saves the new appointment record with status `BOOKED`.
7. **DTO Response:** Maps the entity to an `AppointmentResponse` and returns HTTP `201 CREATED`.

---

### How do you prevent double-booking of doctor slots?

Double-booking is prevented at two levels:
1. **Repository Query:**
   ```java
   boolean existsByDoctorAndAppointmentTimeAndStatus(
       Doctor doctor, 
       LocalDateTime appointmentTime, 
       AppointmentStatus status
   );
   ```
2. **Service Validation Logic:**
   ```java
   boolean isSlotTaken = appointmentRepo.existsByDoctorAndAppointmentTimeAndStatus(
           doctor, request.getAppointmentTime(), AppointmentStatus.BOOKED);
   if (isSlotTaken) {
       throw new AppointmentConflictException(
           "Doctor " + doctor.getDoctorName() + " is already booked at " + request.getAppointmentTime());
   }
   ```
If an appointment at that slot exists with status `BOOKED`, an `AppointmentConflictException` is thrown, returning an HTTP `409 CONFLICT` status code to the client. If an appointment was previously `CANCELLED`, that slot becomes available again!

---

### How does password hashing work (BCrypt vs MD5)?

- **The Problem with MD5:**
  - MD5 is an obsolete, fast cryptographic hash function. Because it is fast and deterministic, attackers can precompute hashes ("Rainbow Tables") and crack millions of passwords per second.
- **Why BCrypt is Superior:**
  - **Salted Automatically:** BCrypt automatically generates a random 16-byte salt and embeds it directly into the resulting hash string. Even if two users have the same password ("Secret123"), their BCrypt hashes will look completely different.
  - **Work Factor (Adaptive Cost):** BCrypt is deliberately slow (uses the Eksblowfish algorithm with a tunable cost factor, default 10). It takes ~100ms per attempt, making brute-force and dictionary attacks computationally infeasible.
  - **Spring Security Integration:**
    ```java
    // Hashing during signup:
    String hash = passwordEncoder.encode(rawPassword);

    // Verification during signin:
    boolean matches = passwordEncoder.matches(rawPassword, storedHash);
    ```

---

### What is Dependency Injection (DI) and Inversion of Control (IoC)?

- **Inversion of Control (IoC):** Instead of classes manually instantiating their dependencies (e.g. `new AppointmentRepo()`), the Spring IoC Container manages object lifecycles and instantiates beans at startup.
- **Dependency Injection (DI):** The pattern through which the container injects dependencies into components.
- **Why Constructor Injection over Field Injection (`@Autowired`)?**
  1. **Immutability:** Dependencies can be marked `final`, preventing re-assignment.
  2. **Testability:** In unit tests, dependencies can be passed directly via constructor without needing reflection or Spring container startup.
  3. **Fail-Fast:** Prevents `NullPointerException` at runtime if a required dependency is missing.

In our code, we use Lombok's `@RequiredArgsConstructor` with `private final` fields:
```java
@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final IAppointmentRepo appointmentRepo;
    private final IDoctorRepo doctorRepo;
    private final AuthenticationService tokenService;
    private final DoctorService doctorService;
}
```

---

### What is a REST API and what HTTP status codes did you use?

**REST (Representational State Transfer)** is an architectural style for networked applications using standard HTTP verbs on stateless resources.

HTTP Verbs & Status Codes in our project:
- **`GET`**: Retrieve resources (`200 OK`).
- **`POST`**: Create new resources (`201 CREATED` on signup, doctor creation, booking).
- **`PUT`**: Update or modify resource state (`200 OK` on profile update, rescheduling, cancelling).
- **`DELETE`**: Remove resources (`200 OK` on doctor deletion, appointment cancellation).
- **`400 BAD REQUEST`**: Input validation failure or past appointment date.
- **`401 UNAUTHORIZED`**: Invalid credentials during sign-in.
- **`403 FORBIDDEN`**: Missing token or trying to cancel another user's appointment.
- **`404 NOT FOUND`**: Non-existent doctor or appointment ID.
- **`409 CONFLICT`**: Duplicate email or doctor slot collision.
- **`500 INTERNAL SERVER ERROR`**: Unexpected server error.

---

### What is a DTO and why didn't you expose entities directly?

**DTO (Data Transfer Object)** is an object that carries data between processes or network boundaries without carrying business logic or direct database mappings.

Why we use DTOs:
1. **Security:** Never expose sensitive database columns (such as `patientPassword`) in API responses.
2. **Prevents Circular Reference Errors:** Bidirectional JPA relationships (`Doctor` -> `List<Appointment>` -> `Doctor`) cause infinite recursion in Jackson serialization.
3. **Decoupling Database from API Contracts:** Changes in database schema (renaming a column) do not break client frontend contracts.
4. **Targeted Validation:** Request DTOs have annotations (`@Future`, `@Email`, `@NotBlank`) tailored to specific operations.

---

### How does centralized exception handling work in Spring Boot?

We use Spring's `@RestControllerAdvice` and `@ExceptionHandler`:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(AppointmentConflictException ex, HttpServletRequest req) {
        ErrorResponse err = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .build();
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }
}
```
**Benefits:**
- Eliminates repetitive `try-catch` blocks from controller methods.
- Ensures all errors return a predictable, uniform JSON structure (`timestamp`, `status`, `error`, `message`, `path`).

---

### What challenges did you encounter and how did you resolve them?

1. **Broken Composite Key (`AppointmentKey`):**
   - *Problem:* The legacy code had `@GeneratedValue` inside an `@Embeddable` key class without implementing `Serializable`, `equals()`, or `hashCode()`, causing JPA runtime errors and requiring the client to supply internal key components.
   - *Fix:* Replaced `AppointmentKey` with a standard auto-increment primary key (`@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long appointmentId`).
2. **Incorrect Entity Relationship (`Patient` <-> `Appointment`):**
   - *Problem:* The original code mapped `Patient` to `Appointment` as `@OneToOne`, artificially limiting a patient to a single lifetime appointment.
   - *Fix:* Corrected to `@OneToMany` on `Patient` and `@ManyToOne` on `Appointment`.
3. **Hardcoded Database Credentials:**
   - *Problem:* Database credentials were hardcoded in `application.properties`.
   - *Fix:* Configured environment variables with fallbacks (`${DB_PASSWORD:password}`), created `application.properties.example`, and added `.gitignore`.
4. **Environment Portability for Testing:**
   - *Problem:* Running unit tests initially depended on a live local MySQL database.
   - *Fix:* Added H2 in test scope and configured `src/test/resources/application.properties` with in-memory H2 so tests run and pass anywhere with 0 external dependencies.

---

## 3. Deep Dive into Key Classes and Methods

### 1. `AppointmentService.java`
- `bookAppointment(email, token, request)`: The core scheduling engine. Authenticates patient, verifies doctor existence, verifies slot is in future, verifies slot is unoccupied, creates appointment with status `BOOKED`.
- `cancelAppointment(appointmentId, email, token)`: Soft-cancels an appointment by verifying ownership and transitioning status to `CANCELLED`.
- `updateAppointment(appointmentId, email, token, request)`: Reschedules appointment time with collision detection.

### 2. `PatientService.java`
- `signUp(signUpDto)`: Validates email uniqueness, encodes password with BCrypt, and persists patient.
- `signIn(signInDto)`: Checks email, validates BCrypt hash, issues a fresh `AuthenticationToken`.
- `signOut(email, token)`: Invalidates the token in the database.

### 3. `AuthenticationService.java`
- `getAuthenticatedPatient(email, token)`: Null-safe helper that fetches the token, verifies the email matches, and returns the managed `Patient` entity. Throws `UnauthorizedAccessException` on failure.

### 4. `DoctorService.java`
- `addDoctor(request)`: Persists new doctor.
- `getDoctorsBySpecialization(spec)`: Queries repository for doctors filtered by specialization enum.
- `getMyAppointments(docId)`: Fetches upcoming schedule for a given doctor.

---

## 4. Sample Interview Dialogue / Mock Q&A

**Interviewer:** *"I see you worked on a Doctor's Appointment Management System. Can you walk me through how you prevent two patients from booking the same doctor at the same time?"*

**Candidate Answer:**
> *"Certainly! In `AppointmentService`, we enforce validation before any appointment record is written. When a booking request comes in with a `doctorId` and an `appointmentTime`:
> First, we check if the appointment time is in the future.
> Then, we execute a query on our `IAppointmentRepo` called `existsByDoctorAndAppointmentTimeAndStatus(doctor, appointmentTime, AppointmentStatus.BOOKED)`.
> If this returns true, it means that the doctor already has an active, confirmed booking at that specific hour. We immediately throw an `AppointmentConflictException`, which our `GlobalExceptionHandler` translates to an HTTP 409 Conflict with a clear error message.
> Furthermore, if an appointment was previously cancelled, its status is `CANCELLED`, so the query returns false, allowing that slot to be re-booked by another patient."*

**Interviewer:** *"Why did you use DTOs instead of passing entity objects straight to your controllers?"*

**Candidate Answer:**
> *"There are three primary reasons:
> 1. Security: Our `Patient` entity contains a `patientPassword` field. If we return the entity directly, even if hashed, we risk leaking sensitive data in the JSON response.
> 2. Avoiding Jackson Serialization Cycles: A `Doctor` has a list of `Appointment`s, and each `Appointment` has a reference back to `Doctor`. Direct serialization causes a circular loop and `StackOverflowError`.
> 3. Separation of Concerns: Our database schema shouldn't dictate our external REST API contract. With DTOs, we can validate inputs specifically for that request using Jakarta Bean Validation and evolve our DB schema without breaking clients."*

**Interviewer:** *"How did you test your business logic?"*

**Candidate Answer:**
> *"I wrote 29 automated unit tests using JUnit 5 and Mockito. I focused on isolating service logic by mocking the JPA repositories and AuthenticationService. I wrote positive and negative test cases for patient registration, duplicate emails, valid and invalid BCrypt login attempts, appointment conflict detection, past-date booking rejections, and unauthorized cancellation attempts. All 29 tests run and pass cleanly via `mvn clean test` using an in-memory H2 database for test execution."*
