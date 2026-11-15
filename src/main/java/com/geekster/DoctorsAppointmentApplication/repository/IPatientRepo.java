package com.geekster.DoctorsAppointmentApplication.repository;

import com.geekster.DoctorsAppointmentApplication.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPatientRepo extends JpaRepository<Patient, Long> {

    Optional<Patient> findFirstByPatientEmail(String userEmail);

    boolean existsByPatientEmail(String userEmail);
}
