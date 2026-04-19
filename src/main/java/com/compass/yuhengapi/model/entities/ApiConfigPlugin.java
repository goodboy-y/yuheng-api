package com.compass.yuhengapi.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "api_config_plugin", indexes = {
    @Index(name = "idx_api_config_plugin_1", columnList = "api_config_id,api_plugin_id", unique = true)
})
@Getter
@Setter
public class ApiConfigPlugin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "api_config_id", referencedColumnName = "id")
    @JsonIgnore
    private ApiConfig apiConfig;

    @ManyToOne
    @JoinColumn(name = "api_plugin_id", referencedColumnName = "id")
    private ApiPlugin apiPlugin;

    private String accountId;
}
