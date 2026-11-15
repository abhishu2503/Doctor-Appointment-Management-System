package com.geekster.DoctorsAppointmentApplication.service;

import com.geekster.DoctorsAppointmentApplication.dto.AppointmentBookingRequest;
import com.geekster.DoctorsAppointmentApplication.dto.AppointmentResponse;
import com.geekster.DoctorsAppointmentApplication.dto.AppointmentUpdateRequest;
import com.geekster.DoctorsAppointmentApplication.exception.AppointmentConflictException;
import com.geekster.DoctorsAppointmentApplication.exception.BadRequestException;
import com.geekster.DoctorsAppointmentApplication.exception.ResourceNotFoundException;
import com.geekster.DoctorsAppointmentApplication.exception.UnauthorizedAccessException;
import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentStatus;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.repository.IAppointmentRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final IAppointmentRepo appointmentRepo;
    private final IDoctorRepo doctorRepo;
    private final AuthenticationService tokenService;
    private final DoctorService doctorService;

    @Transactional
    public AppointmentResponse bookAppointment(String userEmail, String token, AppointmentBookingRequest request) {
        // 1. Authenticate patient
        Patient patient = tokenService.getAuthenticatedPatient(userEmail, token);

        // 2. Validate doctor existence
        Doctor doctor = doctorRepo.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot book appointment: Doctor not found with ID: " + request.getDoctorId()));

        // 3. Validate appointment time is in the future
        if (request.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment time must be in the future");
        }

        // 4. Prevent double booking of doctor's time slot
        boolean isSlotTaken = appointmentRepo.existsByDoctorAndAppointmentTimeAndStatus(
                doctor, request.getAppointmentTime(), AppointmentStatus.BOOKED);
        if (isSlotTaken) {
            throw new AppointmentConflictException(
                    "Doctor " + doctor.getDoctorName() + " is already booked at " + request.getAppointmentTime() + ". Please select another slot.");
        }

        // 5. Prevent double booking for patient at the same time slot
        boolean isPatientBusy = appointmentRepo.existsByPatientAndAppointmentTimeAndStatus(
                patient, request.getAppointmentTime(), AppointmentStatus.BOOKED);
        if (isPatientBusy) {
            throw new AppointmentConflictException(
                    "You already have an active appointment scheduled at " + request.getAppointmentTime());
        }

        // 6. Create and persist appointment
        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentTime(request.getAppointmentTime())
                .status(AppointmentStatus.BOOKED)
                .reason(request.getReason().trim())
                .bookingTime(LocalDateTime.now())
                .build();

        Appointment savedAppointment = appointmentRepo.save(appointment);
        return doctorService.mapToAppointmentResponse(savedAppointment);
    }

    public List<AppointmentResponse> getPatientAppointments(String userEmail, String token) {
        // Authenticate patient
        Patient patient = tokenService.getAuthenticatedPatient(userEmail, token);

        List<Appointment> appointments = appointmentRepo.findByPatientOrderByAppointmentTimeAsc(patient);
        return appointments.stream()
                .map(doctorService::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse getAppointmentById(Long appointmentId, String userEmail, String token) {
        // Authenticate patient
        Patient patient = tokenService.getAuthenticatedPatient(userEmail, token);

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        // Ensure patient is accessing their own appointment
        if (!appointment.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new UnauthorizedAccessException("You are not authorized to view this appointment");
        }

        return doctorService.mapToAppointmentResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, String userEmail, String token) {
        // Authenticate patient
        Patient patient = tokenService.getAuthenticatedPatient(userEmail, token);

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        // Verify patient owns this appointment
        if (!appointment.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new UnauthorizedAccessException("You are only allowed to cancel your own appointments");
        }

        // Verify appointment is not already cancelled
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Appointment ID " + appointmentId + " is already cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updatedAppointment = appointmentRepo.save(appointment);

        return doctorService.mapToAppointmentResponse(updatedAppointment);
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long appointmentId, String userEmail, String token, AppointmentUpdateRequest request) {
        // Authenticate patient
        Patient patient = tokenService.getAuthenticatedPatient(userEmail, token);

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        // Verify patient ownership
        if (!appointment.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new UnauthorizedAccessException("You are only allowed to modify your own appointments");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a cancelled appointment");
        }

        if (request.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("New appointment time must be in the future");
        }

        // Check if new time slot is different and already taken for this doctor
        if (!appointment.getAppointmentTime().isEqual(request.getAppointmentTime())) {
            boolean isSlotTaken = appointmentRepo.existsByDoctorAndAppointmentTimeAndStatus(
                    appointment.getDoctor(), request.getAppointmentTime(), AppointmentStatus.BOOKED);
            if (isSlotTaken) {
                throw new AppointmentConflictException(
                        "Doctor " + appointment.getDoctor().getDoctorName() + " is already booked at " + request.getAppointmentTime());
            }
            appointment.setAppointmentTime(request.getAppointmentTime());
        }

        if (request.getReason() != null && !request.getReason().isBlank()) {
            appointment.setReason(request.getReason().trim());
        }

        Appointment updatedAppointment = appointmentRepo.save(appointment);
        return doctorService.mapToAppointmentResponse(updatedAppointment);
    }

    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        return doctorService.getMyAppointments(doctorId);
    }
}
