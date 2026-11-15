package com.geekster.DoctorsAppointmentApplication.service;

import com.geekster.DoctorsAppointmentApplication.dto.AppointmentResponse;
import com.geekster.DoctorsAppointmentApplication.dto.DoctorRequest;
import com.geekster.DoctorsAppointmentApplication.dto.DoctorResponse;
import com.geekster.DoctorsAppointmentApplication.exception.ResourceNotFoundException;
import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import com.geekster.DoctorsAppointmentApplication.repository.IAppointmentRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final IDoctorRepo doctorRepo;
    private final IAppointmentRepo appointmentRepo;

    @Transactional
    public DoctorResponse addDoctor(DoctorRequest request) {
        Doctor doctor = Doctor.builder()
                .doctorName(request.getDoctorName().trim())
                .specialization(request.getSpecialization())
                .doctorContact(request.getDoctorContact() != null ? request.getDoctorContact().trim() : null)
                .doctorEmail(request.getDoctorEmail() != null ? request.getDoctorEmail().trim().toLowerCase() : null)
                .qualification(request.getQualification() != null ? request.getQualification().trim() : null)
                .consultationFee(request.getConsultationFee())
                .build();

        Doctor savedDoctor = doctorRepo.save(doctor);
        return mapToResponse(savedDoctor);
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepo.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DoctorResponse getDoctorById(Long docId) {
        Doctor doctor = findDoctorByIdOrThrow(docId);
        return mapToResponse(doctor);
    }

    public List<DoctorResponse> getDoctorsBySpecialization(Specialization specialization) {
        return doctorRepo.findBySpecialization(specialization).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorResponse updateDoctor(Long docId, DoctorRequest request) {
        Doctor doctor = findDoctorByIdOrThrow(docId);

        doctor.setDoctorName(request.getDoctorName().trim());
        doctor.setSpecialization(request.getSpecialization());
        if (request.getDoctorContact() != null) {
            doctor.setDoctorContact(request.getDoctorContact().trim());
        }
        if (request.getDoctorEmail() != null) {
            doctor.setDoctorEmail(request.getDoctorEmail().trim().toLowerCase());
        }
        if (request.getQualification() != null) {
            doctor.setQualification(request.getQualification().trim());
        }
        if (request.getConsultationFee() != null) {
            doctor.setConsultationFee(request.getConsultationFee());
        }

        Doctor updatedDoctor = doctorRepo.save(doctor);
        return mapToResponse(updatedDoctor);
    }

    @Transactional
    public void deleteDoctor(Long docId) {
        Doctor doctor = findDoctorByIdOrThrow(docId);
        doctorRepo.delete(doctor);
    }

    public List<AppointmentResponse> getMyAppointments(Long docId) {
        Doctor doctor = findDoctorByIdOrThrow(docId);
        List<Appointment> appointments = appointmentRepo.findByDoctorOrderByAppointmentTimeAsc(doctor);

        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    public Doctor findDoctorByIdOrThrow(Long docId) {
        return doctorRepo.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + docId));
    }

    public DoctorResponse mapToResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .doctorId(doctor.getDoctorId())
                .doctorName(doctor.getDoctorName())
                .specialization(doctor.getSpecialization())
                .doctorContact(doctor.getDoctorContact())
                .doctorEmail(doctor.getDoctorEmail())
                .qualification(doctor.getQualification())
                .consultationFee(doctor.getConsultationFee())
                .build();
    }

    public AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .bookingTime(appointment.getBookingTime())
                .doctorId(appointment.getDoctor().getDoctorId())
                .doctorName(appointment.getDoctor().getDoctorName())
                .doctorSpecialization(appointment.getDoctor().getSpecialization())
                .patientId(appointment.getPatient().getPatientId())
                .patientName(appointment.getPatient().getPatientFirstName() + " " + appointment.getPatient().getPatientLastName())
                .patientEmail(appointment.getPatient().getPatientEmail())
                .patientContact(appointment.getPatient().getPatientContact())
                .build();
    }
}
