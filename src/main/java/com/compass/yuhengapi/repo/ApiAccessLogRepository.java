package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApiAccessLogRepository
    extends JpaRepository<ApiAccessLog, String>,
            JpaSpecificationExecutor<ApiAccessLog> {

    Page<ApiAccessLog> findByAccessTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<ApiAccessLog> findByPathContaining(String path, Pageable pageable);

    Page<ApiAccessLog> findByClientId(String clientId, Pageable pageable);

    @Query("SELECT l FROM ApiAccessLog l WHERE l.accessTime < :beforeTime")
    Page<ApiAccessLog> findPageByAccessTimeBefore(@Param("beforeTime") LocalDateTime beforeTime, Pageable pageable);

    @Modifying
    @Query("DELETE FROM ApiAccessLog l WHERE l.accessTime < :beforeTime")
    int deleteByAccessTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);

    @Query("SELECT l FROM ApiAccessLog l WHERE l.accessTime < :beforeTime")
    List<ApiAccessLog> findAllByAccessTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);
}