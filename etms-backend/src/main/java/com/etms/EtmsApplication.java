package com.etms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 企业员工培训管理系统 - 主启动类
 * ETMS (Enterprise Training Management System)
 * 
 * @author ETMS
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
@MapperScan("com.etms.mapper")
public class EtmsApplication {
    
    public static void main(String[] args) {
        // 设置headless模式，解决服务器环境无图形界面导致的验证码生成问题
        System.setProperty("java.awt.headless", "true");
        SpringApplication.run(EtmsApplication.class, args);
        System.out.println("======================================");
        System.out.println("  企业员工培训管理系统启动成功!");
        System.out.println("  访问地址: http://localhost:8080/api");
        System.out.println("======================================");
    }
}
