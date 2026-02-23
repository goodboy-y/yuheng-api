package com.compass.yuhengapi.common.util;


import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;


public final class PageList<T> {

    /**
     * -- GETTER --
     * 返回当前分页信息
     */
    @Getter
    private final PageInfo pageInfo;

    /**
     * -- GETTER --
     * 返回当前页的排序信息
     */
    @Getter
    private final List<Sort> sortInfos = new ArrayList<>();

    @Getter
    private final List<T> data;

    public PageList(int currentPage, int rowsPerPage, long totalRows, List<Sort> sortInfos, List<T> data) {
        this.pageInfo = new PageInfo(currentPage, data.size(), rowsPerPage, totalRows);
        if (sortInfos != null) {
            this.sortInfos.addAll(sortInfos);
        }
        this.data = data;
    }


    public static <T> PageList<T> of(Page<T> page, Pageable pageable) {
        return new PageList<>(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            page.getTotalPages(),
            null,
            page.getContent()
        );
    }

}
