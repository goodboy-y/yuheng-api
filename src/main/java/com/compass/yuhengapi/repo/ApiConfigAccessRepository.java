package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiClient;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiConfigAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApiConfigAccessRepository
    extends JpaRepository<ApiConfigAccess, String>,
            JpaSpecificationExecutor<ApiConfigAccess> {

    Optional<ApiConfigAccess> findByClientAndApiConfig(ApiClient client, ApiConfig apiConfig);

    List<ApiConfigAccess> findByClient(ApiClient client);

    List<ApiConfigAccess> findByApiConfig(ApiConfig apiConfig);

    @Query("select count(1) from ApiConfigAccess t where t.client.id = ?1 and t.apiConfig.id = ?2")
    int countByClientAndApiConfig(String clientId, String apiConfigId);

    void deleteByClientAndApiConfig(ApiClient client, ApiConfig apiConfig);
}
