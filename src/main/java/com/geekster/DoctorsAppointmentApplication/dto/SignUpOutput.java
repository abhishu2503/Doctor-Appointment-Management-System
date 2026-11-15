package com.geekster.DoctorsAppointmentApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpOutput {
    private String status;
    private String message;
    private Long patientId;

    public SignUpOutput(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
