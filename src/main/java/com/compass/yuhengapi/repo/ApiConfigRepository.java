package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiConfig;
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

}
