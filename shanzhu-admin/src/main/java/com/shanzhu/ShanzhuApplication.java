package com.shanzhu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

// @EnableSnailJob
@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan({"com.shanzhu.**.mapper"})
@ComponentScan({"com.shanzhu.**"})
public class ShanzhuApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShanzhuApplication.class, args);
    }
}
