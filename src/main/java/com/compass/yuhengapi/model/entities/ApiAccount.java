package com.compass.yuhengapi.model.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_account", indexes = {@Index(name = "idx_account_1", columnList = "username", unique = true)})
@Getter
@Setter
public class ApiAccount implements java.io.Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 或者其他生成策略
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String email;

    private LocalDateTime createDateTime;

    private LocalDateTime updateDateTime;

}