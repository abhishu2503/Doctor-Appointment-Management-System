package com.geekster.DoctorsAppointmentApplication.service;

import com.geekster.DoctorsAppointmentApplication.dto.*;
import com.geekster.DoctorsAppointmentApplication.exception.DuplicateResourceException;
import com.geekster.DoctorsAppointmentApplication.exception.InvalidCredentialsException;
import com.geekster.DoctorsAppointmentApplication.model.AuthenticationToken;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.repository.IPatientRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final IPatientRepo patientRepo;
    private final AuthenticationService tokenService;
    private final DoctorService doctorService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpOutput signUp(SignUpInput signUpDto) {
        String normalizedEmail = signUpDto.getUserEmail().trim().toLowerCase();

        // Check if patient already exists with this email
        if (patientRepo.existsByPatientEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Patient already registered with email: " + normalizedEmail);
        }

        // Secure password hashing using BCrypt
        String encodedPassword = passwordEncoder.encode(signUpDto.getUserPassword());

        Patient patient = Patient.builder()
                .patientFirstName(signUpDto.getUserFirstName().trim())
                .patientLastName(signUpDto.getUserLastName().trim())
                .patientEmail(normalizedEmail)
                .patientPassword(encodedPassword)
                .patientContact(signUpDto.getUserContact().trim())
                .build();

        Patient savedPatient = patientRepo.save(patient);

        return SignUpOutput.builder()
                .status("SUCCESS")
                .message("Patient registered successfully")
                .patientId(savedPatient.getPatientId())
                .build();
    }

    @Transactional
    public SignInOutput signIn(SignInInput signInDto) {
        String normalizedEmail = signInDto.getPatientEmail().trim().toLowerCase();

        // Find patient by email
        Patient patient = patientRepo.findFirstByPatientEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Verify password hash with BCrypt
        if (!passwordEncoder.matches(signInDto.getPatientPassword(), patient.getPatientPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Clean up any existing token and create fresh authentication token
        tokenService.deleteTokenByPatient(patient);
        AuthenticationToken token = new AuthenticationToken(patient);
        tokenService.saveToken(token);

        String fullName = patient.getPatientFirstName() + " " + patient.getPatientLastName();

        return SignInOutput.builder()
                .status("Authentication Successful")
                .token(token.getToken())
                .patientEmail(patient.getPatientEmail())
                .patientName(fullName)
                .build();
    }

    @Transactional
    public void signOut(String userEmail, String token) {
        // Authenticate before invalidating
        tokenService.getAuthenticatedPatient(userEmail, token);
        tokenService.deleteToken(token);
    }

    public PatientProfileResponse getProfile(String userEmail, String token) {
        Patient patient = tokenService.getAuthenticatedPatient(userEmail, token);

        return PatientProfileResponse.builder()
                .patientId(patient.getPatientId())
                .patientFirstName(patient.getPatientFirstName())
                .patientLastName(patient.getPatientLastName())
                .patientEmail(patient.getPatientEmail())
                .patientContact(patient.getPatientContact())
                .build();
    }

    @Transactional
    public PatientProfileResponse updateProfile(String userEmail, String token, PatientUpdateRequest updateRequest) {
        Patient patient = tokenService.getAuthenticatedPatient(userEmail, token);

        patient.setPatientFirstName(updateRequest.getPatientFirstName().trim());
        patient.setPatientLastName(updateRequest.getPatientLastName().trim());
        patient.setPatientContact(updateRequest.getPatientContact().trim());

        Patient updatedPatient = patientRepo.save(patient);

        return PatientProfileResponse.builder()
                .patientId(updatedPatient.getPatientId())
                .patientFirstName(updatedPatient.getPatientFirstName())
                .patientLastName(updatedPatient.getPatientLastName())
                .patientEmail(updatedPatient.getPatientEmail())
                .patientContact(updatedPatient.getPatientContact())
                .build();
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorService.getAllDoctors();
    }
}
