package com.compass.yuhengapi.conf;

import com.compass.yuhengapi.filter.LoginFilter;
import com.compass.yuhengapi.filter.SecretFilter;
import com.compass.yuhengapi.repo.ApiClientRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@SuppressWarnings("all")
@Configuration
public class FilterConfig {


    @Bean
    public FilterRegistrationBean<SecretFilter> secretFilter(ApiClientRepository apiClientRepository) {
        FilterRegistrationBean<SecretFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecretFilter(apiClientRepository));
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);  // 优先级1，最先执行
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<LoginFilter> loginFilter(StringRedisTemplate stringRedisTemplate) {
        FilterRegistrationBean<LoginFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LoginFilter(stringRedisTemplate));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);  // 优先级1，最先执行
        return registrationBean;
    }


}
