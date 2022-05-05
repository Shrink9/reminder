package com.example.remindlearning.controller;

import com.example.remindlearning.constant.WechatPayConstant;
import com.example.remindlearning.entity.RemindedPerson;
import com.example.remindlearning.service.RemindedPersonService;
import com.example.remindlearning.util.MyMailSender;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.HttpClient;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/reminder/callback")
public class CallbackController{
    private static final Logger log=LoggerFactory.getLogger(CallbackController.class);
    @Value("${wechat-pay.api-v3-key}")
    private String apiV3Key;
    @Autowired
    HttpClient httpClient;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private RemindedPersonService remindedPersonService;
    @Autowired
    private MyMailSender myMailSender;
    /**
     * 获取支付codeUrl(二维码链接)
     *
     * @param signalRes
     * @return codeUrl
     */
    @PostMapping("/payment-signal")
    public HashMap<String,String> paymentSignal(@RequestBody Map<String,Object> signalRes){
        log.debug("收到微信回调");
        try{
            Map<String,String> resource=(Map<String,String>)signalRes.get("resource");
            String ciphertext=resource.get("ciphertext");
            String associatedData=resource.get("associated_data");
            String nonce=resource.get("nonce");
            //解密出明文
            String plainText=new AesUtil(apiV3Key.getBytes(StandardCharsets.UTF_8)).decryptToString(associatedData.getBytes(StandardCharsets.UTF_8),nonce.getBytes(StandardCharsets.UTF_8),ciphertext);
            //转换
            HashMap<String,Object> data=new Gson().fromJson(plainText,HashMap.class);
            int id=NumberUtils.toInt(((String)data.get("out_trade_no")).split("-")[0]);
            RemindedPerson person=remindedPersonService.getById(id);
            String[] strings=person.getStatus()
                                   .split("-");
            synchronized(this){
                if(strings[0].equals(""+person.getCount())&&RemindedPerson.NOT_PAIED.equals(strings[1])){
                    RemindedPerson remindedPerson=remindedPersonService.getById(id);
                    String subject="感谢您对Reminder的支持！";
                    String toEmail=remindedPerson.getEmail();
                    String text="尊敬的"+remindedPerson.getName()+"，您好，感谢您的捐赠，祝您生活愉快！\n"+"\n"+"系统邮件，请勿回复！\n"+"如有任何问题请联系管理员，QQ"+"：1921834559。";
                    myMailSender.sendEmail(subject,toEmail,text);
                    RemindedPerson remindedPersonToUpdate=new RemindedPerson();
                    remindedPersonToUpdate.setId(id);
                    remindedPersonToUpdate.setStatus(strings[0]+"-"+RemindedPerson.PAIED);
                    remindedPersonService.updateById(remindedPersonToUpdate);
                }
                else{
                    log.debug("id为{}的提醒已支付,本次回调处理结束.",id);
                }
            }
            return null;
        }
        catch(Exception e){
            log.warn("微信回调信息错误,提示信息:"+e.getMessage());
            HashMap<String,String> map=new HashMap<>();
            map.put("code","FAIL");
            map.put("message","支付失败");
            return map;
        }
    }
}
