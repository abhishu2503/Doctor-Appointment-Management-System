package com.geekster.DoctorsAppointmentApplication.service;

import com.geekster.DoctorsAppointmentApplication.dto.*;
import com.geekster.DoctorsAppointmentApplication.exception.DuplicateResourceException;
import com.geekster.DoctorsAppointmentApplication.exception.InvalidCredentialsException;
import com.geekster.DoctorsAppointmentApplication.model.AuthenticationToken;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.repository.IPatientRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private IPatientRepo patientRepo;

    @Mock
    private AuthenticationService tokenService;

    @Mock
    private DoctorService doctorService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;
    private SignUpInput signUpInput;
    private SignInInput signInInput;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
                .patientId(1L)
                .patientFirstName("Rahul")
                .patientLastName("Sharma")
                .patientEmail("rahul@example.com")
                .patientPassword("hashedPassword123")
                .patientContact("9876543210")
                .build();

        signUpInput = SignUpInput.builder()
                .userFirstName("Rahul")
                .userLastName("Sharma")
                .userEmail("rahul@example.com")
                .userPassword("Password@123")
                .userContact("9876543210")
                .build();

        signInInput = SignInInput.builder()
                .patientEmail("rahul@example.com")
                .patientPassword("Password@123")
                .build();
    }

    @Test
    @DisplayName("1. Patient registration - Success")
    void testSignUp_Success() {
        when(patientRepo.existsByPatientEmail("rahul@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("hashedPassword123");
        when(patientRepo.save(any(Patient.class))).thenReturn(testPatient);

        SignUpOutput output = patientService.signUp(signUpInput);

        assertNotNull(output);
        assertEquals("SUCCESS", output.getStatus());
        assertEquals(1L, output.getPatientId());
        verify(patientRepo).save(any(Patient.class));
    }

    @Test
    @DisplayName("2. Duplicate email validation - Throws DuplicateResourceException")
    void testSignUp_DuplicateEmail_ThrowsException() {
        when(patientRepo.existsByPatientEmail("rahul@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> patientService.signUp(signUpInput));
        verify(patientRepo, never()).save(any(Patient.class));
    }

    @Test
    @DisplayName("3. Login validation - Success")
    void testSignIn_Success() {
        when(patientRepo.findFirstByPatientEmail("rahul@example.com")).thenReturn(Optional.of(testPatient));
        when(passwordEncoder.matches("Password@123", "hashedPassword123")).thenReturn(true);
        doNothing().when(tokenService).deleteTokenByPatient(testPatient);
        doNothing().when(tokenService).saveToken(any(AuthenticationToken.class));

        SignInOutput output = patientService.signIn(signInInput);

        assertNotNull(output);
        assertEquals("Authentication Successful", output.getStatus());
        assertNotNull(output.getToken());
        assertEquals("rahul@example.com", output.getPatientEmail());
        verify(tokenService).saveToken(any(AuthenticationToken.class));
    }

    @Test
    @DisplayName("3b. Login validation - Invalid Password")
    void testSignIn_InvalidPassword_ThrowsException() {
        when(patientRepo.findFirstByPatientEmail("rahul@example.com")).thenReturn(Optional.of(testPatient));
        when(passwordEncoder.matches("Password@123", "hashedPassword123")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> patientService.signIn(signInInput));
        verify(tokenService, never()).saveToken(any(AuthenticationToken.class));
    }

    @Test
    @DisplayName("3c. Login validation - Non-existent Email")
    void testSignIn_UserNotFound_ThrowsException() {
        when(patientRepo.findFirstByPatientEmail("rahul@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> patientService.signIn(signInInput));
    }

    @Test
    @DisplayName("Patient Signout - Success")
    void testSignOut_Success() {
        when(tokenService.getAuthenticatedPatient(anyString(), anyString())).thenReturn(testPatient);
        doNothing().when(tokenService).deleteToken(anyString());

        assertDoesNotThrow(() -> patientService.signOut("rahul@example.com", "valid-token"));
        verify(tokenService).deleteToken("valid-token");
    }

    @Test
    @DisplayName("Get Profile - Success")
    void testGetProfile_Success() {
        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-123")).thenReturn(testPatient);

        PatientProfileResponse profile = patientService.getProfile("rahul@example.com", "token-123");

        assertNotNull(profile);
        assertEquals(1L, profile.getPatientId());
        assertEquals("Rahul", profile.getPatientFirstName());
        assertEquals("rahul@example.com", profile.getPatientEmail());
    }

    @Test
    @DisplayName("Update Profile - Success")
    void testUpdateProfile_Success() {
        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-123")).thenReturn(testPatient);
        when(patientRepo.save(any(Patient.class))).thenReturn(testPatient);

        PatientUpdateRequest updateRequest = PatientUpdateRequest.builder()
                .patientFirstName("Rahul Kumar")
                .patientLastName("Sharma")
                .patientContact("9999988888")
                .build();

        PatientProfileResponse response = patientService.updateProfile("rahul@example.com", "token-123", updateRequest);

        assertNotNull(response);
        assertEquals("Rahul Kumar", response.getPatientFirstName());
        verify(patientRepo).save(testPatient);
    }
}
