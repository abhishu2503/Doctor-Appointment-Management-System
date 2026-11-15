package com.geekster.DoctorsAppointmentApplication.dto;

import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {
    private Long doctorId;
    private String doctorName;
    private Specialization specialization;
    private String doctorContact;
    private String doctorEmail;
    private String qualification;
    private Double consultationFee;
}
