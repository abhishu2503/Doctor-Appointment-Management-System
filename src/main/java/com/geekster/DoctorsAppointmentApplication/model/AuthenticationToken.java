package com.geekster.DoctorsAppointmentApplication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "authentication_tokens")
public class AuthenticationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime tokenCreationDateTime;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "fk_patient_id", unique = true)
    private Patient patient;

    public AuthenticationToken(Patient patient) {
        this.patient = patient;
        this.tokenCreationDateTime = LocalDateTime.now();
        this.token = UUID.randomUUID().toString();
    }
}
