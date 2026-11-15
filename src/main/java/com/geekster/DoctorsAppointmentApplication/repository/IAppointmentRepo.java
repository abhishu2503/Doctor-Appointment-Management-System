package com.geekster.DoctorsAppointmentApplication.repository;

import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentStatus;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IAppointmentRepo extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientOrderByAppointmentTimeAsc(Patient patient);

    List<Appointment> findByDoctorOrderByAppointmentTimeAsc(Doctor doctor);

    List<Appointment> findByDoctorAndStatus(Doctor doctor, AppointmentStatus status);

    boolean existsByDoctorAndAppointmentTimeAndStatus(Doctor doctor, LocalDateTime appointmentTime, AppointmentStatus status);

    boolean existsByPatientAndAppointmentTimeAndStatus(Patient patient, LocalDateTime appointmentTime, AppointmentStatus status);
}
