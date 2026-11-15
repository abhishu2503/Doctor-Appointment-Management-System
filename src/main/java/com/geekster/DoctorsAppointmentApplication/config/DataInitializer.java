package com.geekster.DoctorsAppointmentApplication.config;

import com.geekster.DoctorsAppointmentApplication.model.Doctor;
import com.geekster.DoctorsAppointmentApplication.model.Specialization;
import com.geekster.DoctorsAppointmentApplication.repository.IDoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IDoctorRepo doctorRepo;

    @Override
    public void run(String... args) {
        if (doctorRepo.count() == 0) {
            Doctor d1 = Doctor.builder()
                    .doctorName("Dr. Anita Roy")
                    .specialization(Specialization.Cardiology)
                    .doctorContact("9876543210")
                    .doctorEmail("anita.roy@hospital.com")
                    .qualification("MBBS, MD (Cardiology)")
                    .consultationFee(800.0)
                    .build();

            Doctor d2 = Doctor.builder()
                    .doctorName("Dr. Rajesh Kumar")
                    .specialization(Specialization.General_Physician)
                    .doctorContact("9876543211")
                    .doctorEmail("rajesh.kumar@hospital.com")
                    .qualification("MBBS, MD (Medicine)")
                    .consultationFee(500.0)
                    .build();

            Doctor d3 = Doctor.builder()
                    .doctorName("Dr. Sneha Patel")
                    .specialization(Specialization.Pediatrics)
                    .doctorContact("9876543212")
                    .doctorEmail("sneha.patel@hospital.com")
                    .qualification("MBBS, DCH (Pediatrics)")
                    .consultationFee(650.0)
                    .build();

            Doctor d4 = Doctor.builder()
                    .doctorName("Dr. Vikram Mehta")
                    .specialization(Specialization.Dermatology)
                    .doctorContact("9876543213")
                    .doctorEmail("vikram.mehta@hospital.com")
                    .qualification("MBBS, DVD, MD (Dermatology)")
                    .consultationFee(700.0)
                    .build();

            doctorRepo.saveAll(List.of(d1, d2, d3, d4));
        }
    }
}
