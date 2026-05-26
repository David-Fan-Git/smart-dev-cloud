package com.develop.mvp.pk.module.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 *
 * @author David
 */
@SpringBootApplication
public class SystemServerApplication {

    /**
     * 启动 System 模块服务。
     *
     * @param args args 参数
     */
    public static void main(String[] args) {

        /**
         * 执行 run 对应的业务操作。
         *
         * @param class class 参数
         * @param args args 参数
         * @return 处理结果
         */
        SpringApplication.run(SystemServerApplication.class, args);
    }

}
