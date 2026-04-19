package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.entities.ApiPlugin;
import com.compass.yuhengapi.service.ApiPluginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apiPlugin")
public class ApiPluginController {

    private final ApiPluginService apiPluginService;

    @GetMapping("/list")
    public Result<List<ApiPlugin>> list() {
        return Result.success(apiPluginService.getAllPlugins());
    }

    @GetMapping("/apiConfig/{apiConfigId}")
    public Result<List<ApiPlugin>> getApiConfigPlugins(@PathVariable String apiConfigId) {
        return Result.success(apiPluginService.getApiConfigPlugins(apiConfigId));
    }

    @PostMapping("/apiConfig/{apiConfigId}")
    public Result<String> saveApiConfigPlugins(@PathVariable String apiConfigId, @RequestBody List<String> pluginIds) {
        apiPluginService.saveApiConfigPlugins(apiConfigId, pluginIds);
        return Result.success("保存成功");
    }
}
