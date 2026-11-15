package com.geekster.DoctorsAppointmentApplication.dto;

import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorRequest {

    @NotBlank(message = "Doctor name is mandatory")
    private String doctorName;

    @NotNull(message = "Specialization is mandatory")
    private Specialization specialization;

    private String doctorContact;

    @Email(message = "Invalid doctor email format")
    private String doctorEmail;

    private String qualification;

    @PositiveOrZero(message = "Consultation fee cannot be negative")
    private Double consultationFee;
}
