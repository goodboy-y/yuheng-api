package com.compass.yuhengapi.repo;

import com.compass.yuhengapi.model.entities.ApiAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiAccountRepository extends JpaRepository<ApiAccount, Long> {

    ApiAccount findByUsername(String username);

}
