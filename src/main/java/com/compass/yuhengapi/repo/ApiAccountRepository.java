package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiAccount;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiAccountRepository extends JpaRepository<ApiAccount, Long> {

    @Cacheable(value = "apiAccount", key = "#username")
    ApiAccount findByUsername(String username);

}
