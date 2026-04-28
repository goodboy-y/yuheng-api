package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ApiConfigRepository extends JpaRepository<ApiConfig, String>, JpaSpecificationExecutor<ApiConfig> {

    @Query("select t from ApiConfig t where t.path = ?1")
    ApiConfig selectByPath(String path);

    @Query("select count(1) from ApiConfig where path = ?1")
    Integer selectCountByPath(String path);

    @Query("select count(1) from ApiConfig where path = ?1 and id != ?2")
    Integer selectCountByPathWhenUpdate(String path, String id);

    @Query(value = "select count(1) from api_config where datasource_id = ?1", nativeQuery = true)
    int countByDatasource(String id);

    @Query("select a from ApiConfig a where a.id not in (select ac.apiConfig.id from ApiConfigAccess ac where ac.client.id = ?1)")
    Page<ApiConfig> findUnAuthorizedApiConfigs(String clientId, Pageable pageable);

    @Query("select a from ApiConfig a where a.id not in (select ac.apiConfig.id from ApiConfigAccess ac where ac.client.id = ?1) and a.name like %?2%")
    Page<ApiConfig> findUnAuthorizedApiConfigsByName(String clientId, String name, Pageable pageable);

}
