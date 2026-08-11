package hello.login;

import hello.login.web.filter.LogFilter;
import hello.login.web.filter.LoginCheckFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;

@Configuration
public class WebConfig {

    // HTTP 요청 -> WAS -> "FILTER" -> DISPATCHER SERVLET
    @Bean
    public FilterRegistrationBean<Filter> logFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        // 필터 지정
        filterRegistrationBean.setFilter(new LogFilter());
        // 필터 순서
        filterRegistrationBean.setOrder(1);
        // 필터 적용 URL
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }

    // HTTP 요청 -> WAS -> "FILTER" -> DISPATCHER SERVLET
    @Bean
    public FilterRegistrationBean loginCheckFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new LoginCheckFilter());
        filterRegistrationBean.setOrder(2);
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }
}
