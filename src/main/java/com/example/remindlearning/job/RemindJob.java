package com.example.remindlearning.job;

import com.example.remindlearning.constant.WechatPayConstant;
import com.example.remindlearning.entity.RemindedPerson;
import com.example.remindlearning.service.RemindedPersonService;
import com.example.remindlearning.util.MyMailSender;
import com.google.gson.Gson;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class RemindJob extends QuartzJobBean{
    private static final Logger log=LoggerFactory.getLogger(RemindJob.class);
    @Value("${wechat-pay.merchant-id}")
    private String merchantId;
    @Value("${wechat-pay.merchant-serial-number}")
    private String merchantSerialNumber;
    @Value("${wechat-pay.api-v3-key}")
    private String apiV3Key;
    @Value("${wechat-pay.domain}")
    private String domain;
    @Value("${wechat-pay.appid}")
    private String appId;
    @Value("${wechat-pay.notify-url}")
    private String notifyUrl;
    @Autowired
    private HttpClient httpClient;
    @Autowired
    private RemindedPersonService remindedPersonService;
    @Autowired
    private MyMailSender myMailSender;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException{
        //获取指定任务时传递的参数
        String currentTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.debug("现在时间是{},开始本次提醒.",currentTime);
        List<RemindedPerson> remindedPersons=remindedPersonService.list();
        int count=0;
        for(RemindedPerson remindedPerson: remindedPersons){
            RemindedPerson remindedPersonToUpdate=new RemindedPerson();
            remindedPersonToUpdate.setId(remindedPerson.getId());
            remindedPersonToUpdate.setCount(remindedPerson.getCount()+1);
            remindedPersonToUpdate.setStatus((remindedPerson.getCount()+1)+"-"+RemindedPerson.NOT_PAIED);
            remindedPersonService.updateById(remindedPersonToUpdate);
            //获取codeUrl
            String codeUrl=null;
            try{
                codeUrl=createPay(remindedPerson);
                String text=
                        "尊敬的"+remindedPerson.getName()+"，现在时间是"+currentTime+"，您收到了来自"+remindedPerson.getReminder()+
                                "的提醒，这是他/她第"+(remindedPerson.getCount()+1)+"次提醒您：\n"+"\t"+remindedPerson.getRemindText()+"\n"+"提醒周期为每周的周三、周五、周日晚20:00\n"+"如果您觉得本次提醒对您有帮助，可以捐赠Reminder一分钱，支付链接为："+codeUrl+"，请在移动端微信内打开链接进行支付，十分感谢！\n"+"\n"+"系统邮件，请勿回复！\n"+"如有任何问题请联系管理员，QQ：1921834559。";
                String subject="您有一份来自“"+remindedPerson.getReminder()+"”的提醒，快来看看吧！";

                boolean isSendEmail=myMailSender.sendEmail(subject,remindedPerson.getEmail(),text);
                if(isSendEmail){
                    count++;
                }
            }
            catch(Exception e){
                log.warn("提醒{}时获取codeUrl失败,故未进行提醒.",remindedPerson.getName());
            }
        }
        log.debug("本次提醒结束,共提醒{}人.",count);
    }
    private String createPay(RemindedPerson remindedPerson) throws Exception{
        //请求构造
        HttpPost httpPost=new HttpPost(domain+WechatPayConstant.CREATE_PAY_URL);
        //请求体
        //构造数据
        HashMap<String,Object> reqData=new HashMap<>();
        reqData.put("appid",appId);
        reqData.put("mchid",merchantId);
        reqData.put("description",remindedPerson.getName()+"，感谢您！");
        reqData.put("out_trade_no",
                remindedPerson.getId()+"-"+(remindedPerson.getCount()+1)+"-"+new Random().nextInt(9999999));
        reqData.put("notify_url",notifyUrl+"/reminder/callback/payment-signal");
        HashMap<String,Integer> amount=new HashMap<>();
        //单位是分
        amount.put("total",1);
        reqData.put("amount",amount);
        String jsonReqData=new Gson().toJson(reqData);
        StringEntity entity=new StringEntity(jsonReqData,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        //请求头
        httpPost.setHeader("Accept","application/json");
        //完成签名并执行请求
        CloseableHttpResponse response=(CloseableHttpResponse)httpClient.execute(httpPost);
        Map<String,String> dataMap=null;
        try{
            int statusCode=response.getStatusLine()
                                   .getStatusCode();
            //成功
            if(statusCode==200){
                String body=EntityUtils.toString(response.getEntity());
                dataMap=new Gson().fromJson(body,HashMap.class);
            }
            //失败
            else{
                if(statusCode!=204){
                    String body=EntityUtils.toString(response.getEntity());
                    log.error(body);
                    throw new IOException("处理失败");
                }
            }
        }
        finally{
            response.close();
        }
        return dataMap.get("code_url");
    }

}
