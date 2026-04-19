package com.compass.yuhengapi.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_access_log", indexes = {
    @Index(name = "idx_api_access_log_access_time", columnList = "access_time"),
    @Index(name = "idx_api_access_log_path", columnList = "path"),
    @Index(name = "idx_api_access_log_client_id", columnList = "client_id")
})
@Getter
@Setter
public class ApiAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private LocalDateTime accessTime;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String clientId;

    @Column(columnDefinition = "TEXT")
    private String params;

    private String accountId;
}