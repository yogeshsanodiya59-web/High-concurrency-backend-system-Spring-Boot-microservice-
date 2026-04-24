package com.grid07.assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data

@Entity
@Table(name = "users")
public class User {


//    gentraied for table
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(nullable = false, unique = true)


    private String username;

    @Column(name = "is_premium")

//
    private boolean isPremium;
}