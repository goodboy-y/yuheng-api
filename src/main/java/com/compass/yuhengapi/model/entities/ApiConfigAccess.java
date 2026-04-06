package com.compass.yuhengapi.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_config_access", indexes = {
    @Index(name = "idx_api_config_access_1", columnList = "client_id,api_config_id", unique = true)
})
@Getter
@Setter
public class ApiConfigAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private ApiClient client;

    @ManyToOne
    @JoinColumn(name = "api_config_id", referencedColumnName = "id")
    private ApiConfig apiConfig;

    private String accountId;

    @CreationTimestamp
    private LocalDateTime createTime;
}
