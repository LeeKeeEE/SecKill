package com.seckill.lab.seckillsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String salt;

    private String phone;

    private Date registerDate;

    private Date lastLoginDate;

    private Integer loginCount;
}