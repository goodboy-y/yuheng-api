package com.compass.yuhengapi.model.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "api_datasource", indexes = {@Index(name = "idx_db_datasource_1", columnList = "name", unique = true)})
@Getter
@Setter
public class ApiDatasource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String note;
    private String url;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String type;
    private String accountId;

}
