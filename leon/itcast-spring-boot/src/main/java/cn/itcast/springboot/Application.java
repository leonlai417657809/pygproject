package cn.itcast.springboot;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 如果是springboot工程则必须要添加如下的组合注解
 * 能够扫描当前包及其子包里面的spring 注解
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        //SpringApplication.run(Application.class,args);

        //禁止启动的横幅
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.setBannerMode(Banner.Mode.OFF);
        springApplication.run(args);
    }
}
