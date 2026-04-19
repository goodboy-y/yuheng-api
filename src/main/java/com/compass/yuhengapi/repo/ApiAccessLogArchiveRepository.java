package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiAccessLogArchive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ApiAccessLogArchiveRepository
    extends JpaRepository<ApiAccessLogArchive, String>,
            JpaSpecificationExecutor<ApiAccessLogArchive> {

    Page<ApiAccessLogArchive> findByAccessTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<ApiAccessLogArchive> findByPathContaining(String path, Pageable pageable);

    Page<ApiAccessLogArchive> findByClientId(String clientId, Pageable pageable);
}