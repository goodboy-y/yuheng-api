package com.compass.yuhengapi.common.util;

import lombok.Getter;

import java.io.Serializable;


@Getter
public class Sort implements Serializable {

    protected final String field;

    protected final SortType type;

    public Sort(String field, SortType sortType) {
        this.type = sortType;
        this.field = field;
    }

    public String getTypeString() {
        return type.toString();
    }

}
