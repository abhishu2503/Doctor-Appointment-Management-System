package com.geekster.DoctorsAppointmentApplication.controller;

import com.geekster.DoctorsAppointmentApplication.dto.*;
import com.geekster.DoctorsAppointmentApplication.service.AuthenticationService;
import com.geekster.DoctorsAppointmentApplication.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("patient")
@RequiredArgsConstructor
@Tag(name = "Patient Controller", description = "APIs for Patient Registration, Authentication, Profile Management, and Doctor Catalog")
public class PatientController {

    private final PatientService patientService;
    private final AuthenticationService authService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new patient", description = "Validates patient input, ensures email uniqueness, hashes password with BCrypt, and registers the patient.")
    public ResponseEntity<ApiResponse<SignUpOutput>> signUp(@Valid @RequestBody SignUpInput signUpDto) {
        SignUpOutput output = patientService.signUp(signUpDto);
        return new ResponseEntity<>(ApiResponse.success("Patient registered successfully", output), HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    @Operation(summary = "Authenticate patient", description = "Verifies patient email and password, creates an active authentication token.")
    public ResponseEntity<ApiResponse<SignInOutput>> signIn(@Valid @RequestBody SignInInput signInDto) {
        SignInOutput output = patientService.signIn(signInDto);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", output));
    }

    @PostMapping("/signout")
    @Operation(summary = "Sign out patient", description = "Invalidates the active authentication token for the logged in patient.")
    public ResponseEntity<ApiResponse<String>> signOut(@RequestParam String userEmail, @RequestParam String token) {
        patientService.signOut(userEmail, token);
        return ResponseEntity.ok(ApiResponse.success("Signed out successfully"));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get patient profile", description = "Retrieves the authenticated patient's profile details.")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getProfile(@RequestParam String userEmail, @RequestParam String token) {
        PatientProfileResponse profile = patientService.getProfile(userEmail, token);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update patient profile", description = "Updates first name, last name, and contact details of the authenticated patient.")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updateProfile(
            @RequestParam String userEmail,
            @RequestParam String token,
            @Valid @RequestBody PatientUpdateRequest updateRequest) {
        PatientProfileResponse profile = patientService.updateProfile(userEmail, token, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @GetMapping("/doctors")
    @Operation(summary = "Get list of all doctors for patients", description = "Returns catalog of available doctors for an authenticated patient.")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAllDoctors(
            @RequestParam String userEmail,
            @RequestParam String token) {
        authService.getAuthenticatedPatient(userEmail, token);
        List<DoctorResponse> doctors = patientService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved successfully", doctors));
    }
}
