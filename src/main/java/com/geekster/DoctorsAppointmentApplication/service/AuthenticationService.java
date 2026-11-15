package com.geekster.DoctorsAppointmentApplication.service;

import com.geekster.DoctorsAppointmentApplication.exception.UnauthorizedAccessException;
import com.geekster.DoctorsAppointmentApplication.model.AuthenticationToken;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.repository.ITokenRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ITokenRepo tokenRepo;

    @Transactional
    public void saveToken(AuthenticationToken token) {
        tokenRepo.save(token);
    }

    public boolean authenticate(String userEmail, String token) {
        if (userEmail == null || token == null || userEmail.isBlank() || token.isBlank()) {
            return false;
        }
        Optional<AuthenticationToken> authToken = tokenRepo.findFirstByToken(token);
        if (authToken.isEmpty()) {
            return false;
        }
        String expectedEmail = authToken.get().getPatient().getPatientEmail();
        return expectedEmail.equalsIgnoreCase(userEmail.trim());
    }

    public Patient getAuthenticatedPatient(String userEmail, String token) {
        if (userEmail == null || token == null || userEmail.isBlank() || token.isBlank()) {
            throw new UnauthorizedAccessException("Authentication required: userEmail and token must be provided");
        }
        AuthenticationToken authToken = tokenRepo.findFirstByToken(token)
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid or expired authentication token"));

        if (!authToken.getPatient().getPatientEmail().equalsIgnoreCase(userEmail.trim())) {
            throw new UnauthorizedAccessException("Unauthorized: Token does not match the provided email");
        }
        return authToken.getPatient();
    }

    @Transactional
    public void deleteToken(String token) {
        if (token != null && !token.isBlank()) {
            tokenRepo.deleteByToken(token);
        }
    }

    @Transactional
    public void deleteTokenByPatient(Patient patient) {
        tokenRepo.deleteByPatient(patient);
    }
}
