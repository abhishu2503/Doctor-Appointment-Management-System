package com.geekster.DoctorsAppointmentApplication.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.geekster.DoctorsAppointmentApplication.model.AppointmentStatus;
import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private Long appointmentId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appointmentTime;

    private AppointmentStatus status;
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime bookingTime;

    private Long doctorId;
    private String doctorName;
    private Specialization doctorSpecialization;

    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String patientContact;
}
