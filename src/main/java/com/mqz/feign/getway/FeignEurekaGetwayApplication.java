package com.mqz.feign.getway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 *
 *  Parameter 0 of method modifyRequestBodyGatewayFilterFactory in org.springframework.cloud.gateway.config.GatewayAutoConfiguration
 *   required a bean of type 'org.springframework.http.codec.ServerCodecConfigurer' that could not be found.
 *
 *    spring cloud gateway server项目是一个spring boot项目，在启动的时候会去加载它的配置，其中有一个叫做GatewayClassPathWarningAutoConfiguration的配置类中有这么一行代码：
 *     在类路径上找到的Spring MVC，此时它与Spring Cloud网关不兼容。请删除spring-boot-start-web依赖项。
 *     因为spring cloud gateway是基于webflux的，如果非要web支持的话需要导入spring-boot-starter-webflux而不是spring-boot-start-web。
 *
 * @author mqz
 */
@SpringBootApplication
@EnableEurekaClient
public class FeignEurekaGetwayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeignEurekaGetwayApplication.class, args);
    }

}
