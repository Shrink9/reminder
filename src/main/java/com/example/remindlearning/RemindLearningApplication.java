package com.example.remindlearning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RemindLearningApplication{
    private static final Logger log=LoggerFactory.getLogger(RemindLearningApplication.class);
    public static void main(String[] args){
        SpringApplication.run(RemindLearningApplication.class,args);
        log.info("启动成功!!!");
    }
}
