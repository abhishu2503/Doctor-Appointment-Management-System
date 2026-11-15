package com.geekster.DoctorsAppointmentApplication.repository;

import com.geekster.DoctorsAppointmentApplication.model.AuthenticationToken;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITokenRepo extends JpaRepository<AuthenticationToken, Long> {

    Optional<AuthenticationToken> findFirstByToken(String token);

    Optional<AuthenticationToken> findByPatient(Patient patient);

    void deleteByPatient(Patient patient);

    void deleteByToken(String token);
}
