package com.example.remindlearning.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MyMailSender{
    private static final Logger log=LoggerFactory.getLogger(MyMailSender.class);
    /**
     * 发送者
     */
    @Value("${spring.mail.username}")
    private String from;
    private SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
    @Autowired
    private JavaMailSender mailSender;
    public boolean sendEmail(String subject,String toEmail,String text){
        synchronized(simpleMailMessage){
            simpleMailMessage.setSubject(subject);
            //发送方
            simpleMailMessage.setFrom(from);
            //接收方
            simpleMailMessage.setTo(toEmail);
            //文本内容
            simpleMailMessage.setText(text);
            //发送邮件
            try{
                mailSender.send(simpleMailMessage);
                log.debug("向{}发送邮件成功",toEmail);
                return true;
            }
            catch(MailException e){
                log.warn("向{}发送邮件失败",toEmail);
                return false;
            }
        }
    }
}
