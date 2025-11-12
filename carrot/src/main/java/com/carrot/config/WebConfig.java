package com.carrot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry; // 박정대 추가

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads/images}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 하나의 핸들러에 여러 경로를 등록
        registry.addResourceHandler("/images/**")
                .addResourceLocations(
                        "classpath:/static/images/",
                        "file:" + uploadDir + "/"
                );
    }
    @Override //박정대 추가
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/SellerSecondPage").setViewName("SellerSecondPage");
        registry.addViewController("/seller/second").setViewName("SellerSecondPage");
    }
}