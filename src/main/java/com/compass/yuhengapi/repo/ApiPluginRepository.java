package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiPlugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiPluginRepository extends JpaRepository<ApiPlugin, String> {

    ApiPlugin findByName(String name);
}
