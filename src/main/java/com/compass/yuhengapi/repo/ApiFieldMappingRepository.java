package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiFieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiFieldMappingRepository extends JpaRepository<ApiFieldMapping, String> {

    List<ApiFieldMapping> findByApiConfigId(String apiConfigId);

    void deleteByApiConfigId(String apiConfigId);

}
