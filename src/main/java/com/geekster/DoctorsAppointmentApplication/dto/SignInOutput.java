package com.geekster.DoctorsAppointmentApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignInOutput {
    private String status;
    private String token;
    private String patientEmail;
    private String patientName;

    public SignInOutput(String status, String token) {
        this.status = status;
        this.token = token;
    }
}
