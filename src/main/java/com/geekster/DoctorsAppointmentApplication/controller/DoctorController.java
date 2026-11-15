package com.geekster.DoctorsAppointmentApplication.controller;

import com.geekster.DoctorsAppointmentApplication.dto.ApiResponse;
import com.geekster.DoctorsAppointmentApplication.dto.AppointmentResponse;
import com.geekster.DoctorsAppointmentApplication.dto.DoctorRequest;
import com.geekster.DoctorsAppointmentApplication.dto.DoctorResponse;
import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import com.geekster.DoctorsAppointmentApplication.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("doctor")
@RequiredArgsConstructor
@Tag(name = "Doctor Controller", description = "APIs for Doctor Onboarding, Catalog Search, Profile Updates, and Schedule Inspection")
public class DoctorController {

    private final DoctorService docService;

    @PostMapping
    @Operation(summary = "Add a new doctor", description = "Registers a new doctor profile into the system.")
    public ResponseEntity<ApiResponse<DoctorResponse>> addDoctor(@Valid @RequestBody DoctorRequest doctor) {
        DoctorResponse createdDoctor = docService.addDoctor(doctor);
        return new ResponseEntity<>(ApiResponse.success("Doctor added successfully", createdDoctor), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieves the complete directory of doctors.")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAllDoctors() {
        List<DoctorResponse> doctors = docService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved successfully", doctors));
    }

    @GetMapping("/{docId}")
    @Operation(summary = "Get doctor by ID", description = "Retrieves specific doctor information by their unique ID.")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorById(@PathVariable Long docId) {
        DoctorResponse doctor = docService.getDoctorById(docId);
        return ResponseEntity.ok(ApiResponse.success("Doctor details retrieved successfully", doctor));
    }

    @GetMapping("/specialization/{specialization}")
    @Operation(summary = "Search doctors by specialization", description = "Filters doctors based on medical specialization.")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getDoctorsBySpecialization(@PathVariable Specialization specialization) {
        List<DoctorResponse> doctors = docService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved for specialization " + specialization, doctors));
    }

    @PutMapping("/{docId}")
    @Operation(summary = "Update doctor details", description = "Updates profile, contact, fee, or qualification of an existing doctor.")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctor(
            @PathVariable Long docId,
            @Valid @RequestBody DoctorRequest doctorRequest) {
        DoctorResponse updated = docService.updateDoctor(docId, doctorRequest);
        return ResponseEntity.ok(ApiResponse.success("Doctor updated successfully", updated));
    }

    @DeleteMapping("/{docId}")
    @Operation(summary = "Delete doctor", description = "Removes a doctor from the system.")
    public ResponseEntity<ApiResponse<String>> deleteDoctor(@PathVariable Long docId) {
        docService.deleteDoctor(docId);
        return ResponseEntity.ok(ApiResponse.success("Doctor deleted successfully"));
    }

    @GetMapping("/{docId}/appointments")
    @Operation(summary = "Get doctor appointments", description = "Retrieves the schedule of appointments booked for a specific doctor.")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getDocMyAppointments(@PathVariable Long docId) {
        List<AppointmentResponse> myAppointments = docService.getMyAppointments(docId);
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved successfully for doctor ID " + docId, myAppointments));
    }
}
