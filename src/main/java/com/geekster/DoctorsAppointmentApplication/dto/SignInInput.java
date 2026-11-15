package com.geekster.DoctorsAppointmentApplication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignInInput {

    @NotBlank(message = "Patient email is mandatory")
    @Email(message = "Invalid email format")
    private String patientEmail;

    @NotBlank(message = "Patient password is mandatory")
    private String patientPassword;
}
