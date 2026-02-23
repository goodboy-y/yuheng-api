package com.compass.yuhengapi.common.lang;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class QueryCmd {

    private Integer page;

    private Integer pageSize;


    public Pageable toPageable() {
        return PageRequest.of(page, pageSize, Sort.by("id"));
    }
}
