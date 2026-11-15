package com.geekster.DoctorsAppointmentApplication.service;

import com.geekster.DoctorsAppointmentApplication.dto.AppointmentResponse;
import com.geekster.DoctorsAppointmentApplication.dto.DoctorRequest;
import com.geekster.DoctorsAppointmentApplication.dto.DoctorResponse;
import com.geekster.DoctorsAppointmentApplication.exception.ResourceNotFoundException;
import com.geekster.DoctorsAppointmentApplication.model.Appointment;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentStatus;
import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Patient;
import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import com.geekster.DoctorsAppointmentApplication.repository.IAppointmentRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private IDoctorRepo doctorRepo;

    @Mock
    private IAppointmentRepo appointmentRepo;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor testDoctor;
    private DoctorRequest testDoctorRequest;

    @BeforeEach
    void setUp() {
        testDoctor = Doctor.builder()
                .doctorId(1L)
                .doctorName("Dr. Anita Roy")
                .specialization(Specialization.Cardiology)
                .doctorContact("9123456780")
                .doctorEmail("anita.roy@hospital.com")
                .qualification("MBBS, MD Cardiology")
                .consultationFee(800.0)
                .build();

        testDoctorRequest = DoctorRequest.builder()
                .doctorName("Dr. Anita Roy")
                .specialization(Specialization.Cardiology)
                .doctorContact("9123456780")
                .doctorEmail("anita.roy@hospital.com")
                .qualification("MBBS, MD Cardiology")
                .consultationFee(800.0)
                .build();
    }

    @Test
    @DisplayName("4a. Add Doctor - Success")
    void testAddDoctor_Success() {
        when(doctorRepo.save(any(Doctor.class))).thenReturn(testDoctor);

        DoctorResponse response = doctorService.addDoctor(testDoctorRequest);

        assertNotNull(response);
        assertEquals("Dr. Anita Roy", response.getDoctorName());
        assertEquals(Specialization.Cardiology, response.getSpecialization());
        verify(doctorRepo).save(any(Doctor.class));
    }

    @Test
    @DisplayName("4b. Doctor Retrieval - Get All Doctors")
    void testGetAllDoctors_Success() {
        when(doctorRepo.findAll()).thenReturn(List.of(testDoctor));

        List<DoctorResponse> doctors = doctorService.getAllDoctors();

        assertNotNull(doctors);
        assertEquals(1, doctors.size());
        assertEquals("Dr. Anita Roy", doctors.get(0).getDoctorName());
    }

    @Test
    @DisplayName("4c. Doctor Retrieval - Get Doctor By ID")
    void testGetDoctorById_Success() {
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(testDoctor));

        DoctorResponse response = doctorService.getDoctorById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getDoctorId());
        assertEquals("Dr. Anita Roy", response.getDoctorName());
    }

    @Test
    @DisplayName("4d. Doctor Retrieval - Doctor Not Found Throws ResourceNotFoundException")
    void testGetDoctorById_NotFound_ThrowsException() {
        when(doctorRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorById(99L));
    }

    @Test
    @DisplayName("4e. Search Doctors by Specialization")
    void testGetDoctorsBySpecialization_Success() {
        when(doctorRepo.findBySpecialization(Specialization.Cardiology)).thenReturn(List.of(testDoctor));

        List<DoctorResponse> doctors = doctorService.getDoctorsBySpecialization(Specialization.Cardiology);

        assertNotNull(doctors);
        assertEquals(1, doctors.size());
        assertEquals(Specialization.Cardiology, doctors.get(0).getSpecialization());
    }

    @Test
    @DisplayName("Update Doctor - Success")
    void testUpdateDoctor_Success() {
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorRepo.save(any(Doctor.class))).thenReturn(testDoctor);

        DoctorRequest updateRequest = DoctorRequest.builder()
                .doctorName("Dr. Anita Roy Updated")
                .specialization(Specialization.Cardiology)
                .consultationFee(1000.0)
                .build();

        DoctorResponse updated = doctorService.updateDoctor(1L, updateRequest);

        assertNotNull(updated);
        assertEquals("Dr. Anita Roy Updated", updated.getDoctorName());
        assertEquals(1000.0, updated.getConsultationFee());
    }

    @Test
    @DisplayName("Delete Doctor - Success")
    void testDeleteDoctor_Success() {
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(testDoctor));
        doNothing().when(doctorRepo).delete(testDoctor);

        assertDoesNotThrow(() -> doctorService.deleteDoctor(1L));
        verify(doctorRepo).delete(testDoctor);
    }

    @Test
    @DisplayName("Get Doctor Appointments - Success")
    void testGetMyAppointments_Success() {
        Patient patient = Patient.builder()
                .patientId(1L)
                .patientFirstName("Rahul")
                .patientLastName("Sharma")
                .patientEmail("rahul@example.com")
                .patientContact("9876543210")
                .build();

        Appointment appointment = Appointment.builder()
                .appointmentId(10L)
                .appointmentTime(LocalDateTime.now().plusDays(2))
                .status(AppointmentStatus.BOOKED)
                .reason("Cardiac checkup")
                .doctor(testDoctor)
                .patient(patient)
                .build();

        when(doctorRepo.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepo.findByDoctorOrderByAppointmentTimeAsc(testDoctor)).thenReturn(List.of(appointment));

        List<AppointmentResponse> appointments = doctorService.getMyAppointments(1L);

        assertNotNull(appointments);
        assertEquals(1, appointments.size());
        assertEquals(10L, appointments.get(0).getAppointmentId());
        assertEquals("Dr. Anita Roy", appointments.get(0).getDoctorName());
    }
}
