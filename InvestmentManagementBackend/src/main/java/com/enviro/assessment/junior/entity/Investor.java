package com.enviro.assessment.junior.entity;

import com.enviro.assessment.junior.entity.InvestmentProduct;
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

    // Stored as DATE — age is calculated dynamically, never stored
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    // LAZY = only load products when explicitly accessed
    @OneToMany(mappedBy = "investor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvestmentProduct> products = new ArrayList<>();
}