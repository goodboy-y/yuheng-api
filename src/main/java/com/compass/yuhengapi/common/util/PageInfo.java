package com.compass.yuhengapi.common.util;

public record PageInfo(int currentPage, int rowsInPage, int rowsPerPage, long totalRows) {


    public PageInfo {
        if (currentPage < 0) {
            throw new IllegalArgumentException("currentPage must be greater than or equal to zero.");
        }
        if (rowsPerPage < 0) {
            throw new IllegalArgumentException("rowsPerPage must be greater than or equal to zero.");
        }
    }

    /**
     * 获得page总数
     */
    public int getTotalPages() {
        return (int) Math.ceil((double) totalRows / (double) rowsPerPage);
    }

}
