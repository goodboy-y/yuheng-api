package com.compass.yuhengapi.common.util;

import com.compass.yuhengapi.common.enumerate.ReturnCodeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Result<T> {

    private Integer code;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PageInfo pageInfo;
    private T data;
    private long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success(T data) {
        return custom(ReturnCodeEnum.RC200.getCode(), ReturnCodeEnum.RC200.getMessage(), data);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return custom(code, message, null);
    }

    public static <T> Result<T> fail() {
        return custom(ReturnCodeEnum.RC999.getCode(), ReturnCodeEnum.RC999.getMessage(), null);
    }

    public static <T> Result<T> fail(String message) {
        return custom(9999, message, null);
    }


    public static <T> Result<T> custom(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
