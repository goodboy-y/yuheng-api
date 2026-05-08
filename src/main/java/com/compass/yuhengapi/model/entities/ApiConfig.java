package com.compass.yuhengapi.model.entities;

import com.alibaba.fastjson2.JSON;
import com.compass.yuhengapi.model.bean.SqlParam;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "api_config", indexes = {@Index(name = "idx_api_config_1", columnList = "path", unique = true)})
@Getter
@Setter
public class ApiConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String note;

    private String path;

    @ManyToOne
    @JoinColumn(name = "datasource_id", referencedColumnName = "id")
    private ApiDatasource datasource;

    private String sql_param;

    private Integer status;

    private String accountId;

    @OneToMany(mappedBy = "apiConfig", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("apiConfig")
    private List<ApiConfigPlugin> plugins;

    public SqlParam getSqlParam() {
        if (sql_param == null) {
            return null;
        }
        return JSON.parseObject(sql_param, SqlParam.class);
    }


}
