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
import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import com.geekster.DoctorsAppointmentApplication.repository.IAppointmentRepo;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private IAppointmentRepo appointmentRepo;

    @Mock
    private IDoctorRepo doctorRepo;

    @Mock
    private AuthenticationService tokenService;

    @Spy
    private DoctorService doctorService = new DoctorService(null, null);

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private Patient anotherPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;
    private AppointmentBookingRequest bookingRequest;
    private LocalDateTime futureSlot;

    @BeforeEach
    void setUp() {
        futureSlot = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0);

        testPatient = Patient.builder()
                .patientId(1L)
                .patientFirstName("Rahul")
                .patientLastName("Sharma")
                .patientEmail("rahul@example.com")
                .patientContact("9876543210")
                .build();

        anotherPatient = Patient.builder()
                .patientId(2L)
                .patientFirstName("Pooja")
                .patientLastName("Verma")
                .patientEmail("pooja@example.com")
                .patientContact("9876543211")
                .build();

        testDoctor = Doctor.builder()
                .doctorId(100L)
                .doctorName("Dr. Anita Roy")
                .specialization(Specialization.Cardiology)
                .build();

        testAppointment = Appointment.builder()
                .appointmentId(1000L)
                .doctor(testDoctor)
                .patient(testPatient)
                .appointmentTime(futureSlot)
                .status(AppointmentStatus.BOOKED)
                .reason("Chest pain evaluation")
                .bookingTime(LocalDateTime.now())
                .build();

        bookingRequest = AppointmentBookingRequest.builder()
                .doctorId(100L)
                .appointmentTime(futureSlot)
                .reason("Chest pain evaluation")
                .build();
    }

    @Test
    @DisplayName("5. Appointment booking - Success")
    void testBookAppointment_Success() {
        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(doctorRepo.findById(100L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepo.existsByDoctorAndAppointmentTimeAndStatus(testDoctor, futureSlot, AppointmentStatus.BOOKED)).thenReturn(false);
        when(appointmentRepo.existsByPatientAndAppointmentTimeAndStatus(testPatient, futureSlot, AppointmentStatus.BOOKED)).thenReturn(false);
        when(appointmentRepo.save(any(Appointment.class))).thenReturn(testAppointment);

        AppointmentResponse response = appointmentService.bookAppointment("rahul@example.com", "token-1", bookingRequest);

        assertNotNull(response);
        assertEquals(1000L, response.getAppointmentId());
        assertEquals(AppointmentStatus.BOOKED, response.getStatus());
        assertEquals("Dr. Anita Roy", response.getDoctorName());
        assertEquals("Rahul Sharma", response.getPatientName());
        verify(appointmentRepo).save(any(Appointment.class));
    }

    @Test
    @DisplayName("6. Duplicate appointment slot prevention (Doctor Double Booking) - Throws AppointmentConflictException")
    void testBookAppointment_DoctorSlotAlreadyTaken_ThrowsException() {
        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(doctorRepo.findById(100L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepo.existsByDoctorAndAppointmentTimeAndStatus(testDoctor, futureSlot, AppointmentStatus.BOOKED)).thenReturn(true);

        assertThrows(AppointmentConflictException.class, () ->
                appointmentService.bookAppointment("rahul@example.com", "token-1", bookingRequest));

        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("6b. Patient Conflicting Slot Prevention - Throws AppointmentConflictException")
    void testBookAppointment_PatientAlreadyHasAppointmentAtSlot_ThrowsException() {
        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(doctorRepo.findById(100L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepo.existsByDoctorAndAppointmentTimeAndStatus(testDoctor, futureSlot, AppointmentStatus.BOOKED)).thenReturn(false);
        when(appointmentRepo.existsByPatientAndAppointmentTimeAndStatus(testPatient, futureSlot, AppointmentStatus.BOOKED)).thenReturn(true);

        assertThrows(AppointmentConflictException.class, () ->
                appointmentService.bookAppointment("rahul@example.com", "token-1", bookingRequest));

        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("8a. Invalid appointment handling - Non-existent Doctor")
    void testBookAppointment_NonExistentDoctor_ThrowsResourceNotFoundException() {
        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(doctorRepo.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                appointmentService.bookAppointment("rahul@example.com", "token-1", bookingRequest));
    }

    @Test
    @DisplayName("8b. Invalid appointment handling - Past Appointment Time")
    void testBookAppointment_PastTime_ThrowsBadRequestException() {
        AppointmentBookingRequest pastRequest = AppointmentBookingRequest.builder()
                .doctorId(100L)
                .appointmentTime(LocalDateTime.now().minusDays(1))
                .reason("Followup")
                .build();

        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(doctorRepo.findById(100L)).thenReturn(Optional.of(testDoctor));

        assertThrows(BadRequestException.class, () ->
                appointmentService.bookAppointment("rahul@example.com", "token-1", pastRequest));
    }

    @Test
    @DisplayName("7. Appointment cancellation - Success")
    void testCancelAppointment_Success() {
        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(appointmentRepo.findById(1000L)).thenReturn(Optional.of(testAppointment));

        Appointment cancelledAppointment = Appointment.builder()
                .appointmentId(1000L)
                .doctor(testDoctor)
                .patient(testPatient)
                .appointmentTime(futureSlot)
                .status(AppointmentStatus.CANCELLED)
                .reason("Chest pain evaluation")
                .bookingTime(LocalDateTime.now())
                .build();

        when(appointmentRepo.save(any(Appointment.class))).thenReturn(cancelledAppointment);

        AppointmentResponse response = appointmentService.cancelAppointment(1000L, "rahul@example.com", "token-1");

        assertNotNull(response);
        assertEquals(AppointmentStatus.CANCELLED, response.getStatus());
        verify(appointmentRepo).save(testAppointment);
    }

    @Test
    @DisplayName("7b. Appointment cancellation - Unauthorized patient cannot cancel another's appointment")
    void testCancelAppointment_UnauthorizedPatient_ThrowsException() {
        when(tokenService.getAuthenticatedPatient("pooja@example.com", "token-2")).thenReturn(anotherPatient);
        when(appointmentRepo.findById(1000L)).thenReturn(Optional.of(testAppointment));

        assertThrows(UnauthorizedAccessException.class, () ->
                appointmentService.cancelAppointment(1000L, "pooja@example.com", "token-2"));

        verify(appointmentRepo, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("7c. Appointment cancellation - Already Cancelled Appointment Throws BadRequestException")
    void testCancelAppointment_AlreadyCancelled_ThrowsException() {
        testAppointment.setStatus(AppointmentStatus.CANCELLED);

        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(appointmentRepo.findById(1000L)).thenReturn(Optional.of(testAppointment));

        assertThrows(BadRequestException.class, () ->
                appointmentService.cancelAppointment(1000L, "rahul@example.com", "token-1"));
    }

    @Test
    @DisplayName("8c. Invalid appointment handling - Non-existent Appointment ID")
    void testCancelAppointment_NotFound_ThrowsResourceNotFoundException() {
        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(appointmentRepo.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                appointmentService.cancelAppointment(9999L, "rahul@example.com", "token-1"));
    }

    @Test
    @DisplayName("View Patient Appointments - Success")
    void testGetPatientAppointments_Success() {
        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(appointmentRepo.findByPatientOrderByAppointmentTimeAsc(testPatient)).thenReturn(List.of(testAppointment));

        List<AppointmentResponse> responses = appointmentService.getPatientAppointments("rahul@example.com", "token-1");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1000L, responses.get(0).getAppointmentId());
    }

    @Test
    @DisplayName("View Appointment Details by ID - Success")
    void testGetAppointmentById_Success() {
        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(appointmentRepo.findById(1000L)).thenReturn(Optional.of(testAppointment));

        AppointmentResponse response = appointmentService.getAppointmentById(1000L, "rahul@example.com", "token-1");

        assertNotNull(response);
        assertEquals(1000L, response.getAppointmentId());
        assertEquals("Rahul Sharma", response.getPatientName());
    }

    @Test
    @DisplayName("Update Appointment - Success")
    void testUpdateAppointment_Success() {
        LocalDateTime newSlot = futureSlot.plusDays(1);
        AppointmentUpdateRequest updateRequest = AppointmentUpdateRequest.builder()
                .appointmentTime(newSlot)
                .reason("Rescheduled due to emergency")
                .build();

        when(tokenService.getAuthenticatedPatient("rahul@example.com", "token-1")).thenReturn(testPatient);
        when(appointmentRepo.findById(1000L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepo.existsByDoctorAndAppointmentTimeAndStatus(testDoctor, newSlot, AppointmentStatus.BOOKED)).thenReturn(false);
        when(appointmentRepo.save(any(Appointment.class))).thenReturn(testAppointment);

        AppointmentResponse response = appointmentService.updateAppointment(1000L, "rahul@example.com", "token-1", updateRequest);

        assertNotNull(response);
        verify(appointmentRepo).save(testAppointment);
    }
}
