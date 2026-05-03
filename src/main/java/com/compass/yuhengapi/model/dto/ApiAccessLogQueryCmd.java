package com.compass.yuhengapi.model.dto;

import com.compass.yuhengapi.common.lang.QueryCmd;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApiAccessLogQueryCmd extends QueryCmd {

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime beforeTime;

    private String path;

    private String clientId;


 
}
