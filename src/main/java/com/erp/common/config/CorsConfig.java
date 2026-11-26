package com.erp.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置。
 *
 * <p>App 端（uni-app）走原生 HTTP，不受同源策略限制，本配置对 App 无影响；
 * 这里的目的是让 erp-web 的 H5 调试、以及 uni-app 运行到浏览器时不必额外处理跨域。
 * 生产环境如需收紧，改为按域名白名单配置 allowedOriginPatterns。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /** 上传文件对外访问：/image/** 映射到项目根目录的 upload/，与 UploadServiceImpl 返回的 URL 对应 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**").addResourceLocations("file:./upload/");
    }
}
