package com.compass.yuhengapi.conf;

import com.compass.yuhengapi.service.ApiPluginService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PluginInitListener implements ApplicationListener<ContextRefreshedEvent> {

    private final ApiPluginService apiPluginService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        apiPluginService.initDefaultPlugins();
    }
}
