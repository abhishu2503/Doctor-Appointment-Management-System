package com.geekster.DoctorsAppointmentApplication.repository;

import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDoctorRepo extends JpaRepository<Doctor, Long> {

    List<Doctor> findBySpecialization(Specialization specialization);
}
