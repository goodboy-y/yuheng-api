package com.compass.yuhengapi.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "api_plugin", indexes = {
    @Index(name = "idx_api_plugin_name", columnList = "name", unique = true)
})
@Getter
@Setter
public class ApiPlugin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String className;

    private String accountId;
}
