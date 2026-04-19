package com.compass.yuhengapi.common.lang;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class QueryCmd {

    private Integer page = 1;

    private Integer pageSize = 20;


    public Pageable toPageable() {
        return PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by("id"));
    }
}
