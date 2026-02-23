package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiDatasource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ApiDataSourceRepository extends JpaRepository<ApiDatasource, String>, JpaSpecificationExecutor<ApiDatasource> {
    List<ApiDatasource> findByAccountId(String accountId);
}
