package com.enviro.assessment.junior.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "investor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Builder.Default
    @OneToMany(mappedBy = "investor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvestmentProduct> products = new ArrayList<>();
}