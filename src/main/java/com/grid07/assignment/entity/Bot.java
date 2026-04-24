package com.grid07.assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "bots")


public class Bot {

//    This is my bot classs remnbering that bot point i one

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "persona_description", columnDefinition = "TEXT")
    private String personaDescription;
}