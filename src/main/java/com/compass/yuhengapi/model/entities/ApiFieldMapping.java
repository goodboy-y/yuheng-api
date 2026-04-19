package com.compass.yuhengapi.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "api_field_mapping", indexes = {
    @Index(name = "idx_api_field_mapping_1", columnList = "api_config_id")
})
@Getter
@Setter
public class ApiFieldMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "api_config_id", nullable = false)
    private String apiConfigId;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "column_width")
    private Integer columnWidth;

}
