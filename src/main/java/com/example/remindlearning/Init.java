package com.example.remindlearning;

import com.example.remindlearning.job.RemindJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Component
public class Init{
    @Autowired
    private Scheduler scheduler;
    @PostConstruct
    public void addJob() throws SchedulerException{
        //
        //构造JobDetail,核心是任务,其他的可以不设置.
        JobDetail jobDetail1=JobBuilder.newJob(RemindJob.class)
                                      .build();
        Trigger trigger1=TriggerBuilder.newTrigger()
                                      //设置触发器名字和组名
                                      //使用CronSchedule设置时间间隔,重复次数为无数次. 下面是Cron表达式
                                      //每周3,5,7
                                      .withSchedule(CronScheduleBuilder.cronSchedule("0 0 20 ? * 4,6,1"))
                                      .build();
        //绑定定时任务1和触发器1(按照设定执行)
        scheduler.scheduleJob(jobDetail1,trigger1);
        //构造下面要用的startTime
        java.util.Calendar calendar=java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.SECOND,3);
        Date startTime=calendar.getTime();
        JobDetail jobDetail2=JobBuilder.newJob(RemindJob.class)
                                      .build();
        Trigger trigger2=TriggerBuilder.newTrigger()
                                       //设置触发器名字和组名
                                       .withSchedule(SimpleScheduleBuilder.repeatSecondlyForTotalCount(1))
                                       .startAt(startTime)
                                       .build();
        //绑定定时任务2和触发器2(程序运行后3秒执行一次)
        scheduler.scheduleJob(jobDetail2,trigger2);
    }
}
