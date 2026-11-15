package com.geekster.DoctorsAppointmentApplication.controller;

import com.geekster.DoctorsAppointmentApplication.dto.ApiResponse;
import com.geekster.DoctorsAppointmentApplication.dto.AppointmentBookingRequest;
import com.geekster.DoctorsAppointmentApplication.dto.AppointmentResponse;
import com.geekster.DoctorsAppointmentApplication.dto.AppointmentUpdateRequest;
import com.geekster.DoctorsAppointmentApplication.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("appointment")
@RequiredArgsConstructor
@Tag(name = "Appointment Controller", description = "APIs for Booking, Rescheduling, Viewing, and Cancelling Doctor Appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    @Operation(summary = "Book an appointment", description = "Books an appointment with a doctor for an authenticated patient. Validates doctor existence, future slot time, and prevents double booking.")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
            @RequestParam String userEmail,
            @RequestParam String token,
            @Valid @RequestBody AppointmentBookingRequest bookingRequest) {
        AppointmentResponse response = appointmentService.bookAppointment(userEmail, token, bookingRequest);
        return new ResponseEntity<>(ApiResponse.success("Appointment booked successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/my-appointments")
    @Operation(summary = "View patient appointments", description = "Returns all historical and upcoming appointments for the authenticated patient.")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMyAppointments(
            @RequestParam String userEmail,
            @RequestParam String token) {
        List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(userEmail, token);
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved successfully", appointments));
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "View appointment details by ID", description = "Retrieves specific appointment details for an authenticated patient.")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(
            @PathVariable Long appointmentId,
            @RequestParam String userEmail,
            @RequestParam String token) {
        AppointmentResponse appointment = appointmentService.getAppointmentById(appointmentId, userEmail, token);
        return ResponseEntity.ok(ApiResponse.success("Appointment details retrieved successfully", appointment));
    }

    @PutMapping("/{appointmentId}/cancel")
    @Operation(summary = "Cancel an appointment (PUT)", description = "Cancels an existing appointment. Ensures only the booking patient can cancel their appointment.")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointmentPut(
            @PathVariable Long appointmentId,
            @RequestParam String userEmail,
            @RequestParam String token) {
        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId, userEmail, token);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", response));
    }

    @DeleteMapping("/{appointmentId}")
    @Operation(summary = "Cancel an appointment (DELETE)", description = "RESTful DELETE alternative to cancel an existing appointment.")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointmentDelete(
            @PathVariable Long appointmentId,
            @RequestParam String userEmail,
            @RequestParam String token) {
        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId, userEmail, token);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", response));
    }

    @PutMapping("/{appointmentId}")
    @Operation(summary = "Reschedule or update appointment", description = "Updates the appointment time or reason, ensuring no schedule conflicts occur.")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(
            @PathVariable Long appointmentId,
            @RequestParam String userEmail,
            @RequestParam String token,
            @Valid @RequestBody AppointmentUpdateRequest updateRequest) {
        AppointmentResponse response = appointmentService.updateAppointment(appointmentId, userEmail, token, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("Appointment updated successfully", response));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "View doctor appointments", description = "Returns all appointments associated with a specific doctor.")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getDoctorAppointments(@PathVariable Long doctorId) {
        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(doctorId);
        return ResponseEntity.ok(ApiResponse.success("Doctor appointments retrieved successfully", appointments));
    }
}
