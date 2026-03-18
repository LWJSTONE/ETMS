package com.etms.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.util.ArrayList;
import java.util.List;

/**
 * Swagger配置类
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
public class SwaggerConfig {
    
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.etms.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("企业员工培训管理系统API")
                .description("ETMS (Enterprise Training Management System) API文档")
                .version("v1.0.0")
                .contact(new Contact("ETMS", "", "admin@etms.com"))
                .build();
    }
    
    private List<SecurityScheme> securitySchemes() {
        List<SecurityScheme> schemes = new ArrayList<>();
        schemes.add(new ApiKey("Bearer", "Authorization", "header"));
        return schemes;
    }
    
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> contexts = new ArrayList<>();
        contexts.add(SecurityContext.builder()
                .securityReferences(defaultAuth())
                .operationSelector(operationContext -> true)
                .build());
        return contexts;
    }
    
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        List<SecurityReference> references = new ArrayList<>();
        references.add(new SecurityReference("Bearer", authorizationScopes));
        return references;
    }
}
