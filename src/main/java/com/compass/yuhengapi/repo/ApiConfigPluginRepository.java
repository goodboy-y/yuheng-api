package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiConfigPlugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiConfigPluginRepository extends JpaRepository<ApiConfigPlugin, String> {

    List<ApiConfigPlugin> findByApiConfig(ApiConfig apiConfig);

    void deleteByApiConfig(ApiConfig apiConfig);
}
