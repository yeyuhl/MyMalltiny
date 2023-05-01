package io.github.yeyuhl.malltiny.common.config;

import io.github.yeyuhl.malltiny.common.domain.SwaggerProperties;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger的基础配置
 *
 * @author yeyuhl
 * @date 2023/4/23
 */
public abstract class BaseSwaggerConfig {


    /**
     * SwaggerProperties需要自己定义
     * Docket是Swagger的一个实例，它实现了DocumentationPlugin接口，通过Docket可以配置Swagger
     */
    @Bean
    public Docket createRestApi() {
        SwaggerProperties swaggerProperties = swaggerProperties();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo(swaggerProperties))
                // 对项目中的所有API接口进行选择
                .select()
                // 使用什么样的方式来扫描接口，basePackage()方法是按照接口类所在的包的位置
                .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getApiBasePackage()))
                // 扫描路径，获取controller层的接口
                .paths(PathSelectors.any())
                .build();
        // 查看Swagger是否开起了安全设置
        if (swaggerProperties.isEnableSecurity()) {
            // 增加Authorization请求头，即
            docket.securitySchemes(securitySchemes()).securityContexts(securityContexts());
        }
        return docket;
    }

    /**
     * apiInfo是用来配置API信息
     */
    private ApiInfo apiInfo(SwaggerProperties swaggerProperties) {
        return new ApiInfoBuilder()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .contact(new Contact(swaggerProperties.getContactName(), swaggerProperties.getContactUrl(), swaggerProperties.getContactEmail()))
                .version(swaggerProperties.getVersion())
                .build();
    }


    /**
     * 使用SecurityScheme来定义API的身份验证和授权方案
     */
    private List<SecurityScheme> securitySchemes() {
        // 设置请求头信息
        List<SecurityScheme> result = new ArrayList<>();
        ApiKey apiKey = new ApiKey("Authorization", "Authorization", "header");
        result.add(apiKey);
        return result;
    }


    private List<SecurityContext> securityContexts() {
        // 设置需要登录认证的路径
        List<SecurityContext> result = new ArrayList<>();
        result.add(getContextByPath("/*/.*"));
        return result;
    }

    /**
     * SecurityContext来指定哪些操作需要身份验证，以及哪些安全方案应用于这些操作
     */
    private SecurityContext getContextByPath(String pathRegex) {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex(pathRegex))
                .build();
    }

    /**
     * SecurityReference用于引用在全局components/securitySchemes部分中的安全方案
     * 在SecurityContext中使用SecurityReference可以指定哪些安全方案应用于哪些操作
     */
    private List<SecurityReference> defaultAuth() {
        List<SecurityReference> result = new ArrayList<>();
        // 范围是全局
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        // 采用Authorization方案
        result.add(new SecurityReference("Authorization", authorizationScopes));
        return result;
    }

    /**
     * 自定义Swagger配置
     */
    public abstract SwaggerProperties swaggerProperties();
}
