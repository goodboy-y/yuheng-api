package com.compass.yuhengapi.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "api_client", indexes = {@Index(name = "idx_api_client_1", columnList = "clientId", unique = true)})
@Getter
@Setter
public class ApiClient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String note;

    private String clientId;

    private String secret;

    private String expireAt;

    private String accountId;

}
