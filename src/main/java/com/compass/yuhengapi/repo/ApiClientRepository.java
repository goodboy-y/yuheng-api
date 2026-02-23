package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ApiClientRepository extends JpaRepository<ApiClient, String>, JpaSpecificationExecutor<ApiClient> {

    List<ApiClient> findByClientId(String clientId);

}
