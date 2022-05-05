package com.example.remindlearning.constant;

public class WechatPayConstant{
    public static final String CANCEL_PAY_URL="/v3/pay/transactions/out-trade-no/%s/close";
    public static final String CREATE_PAY_URL="/v3/pay/transactions/native";
    public static final String QUERY_PAY_URL="/v3/pay/transactions/out-trade-no/%s?mchid=%s";
    public static final String TRADE_STATE_SUCCESS="SUCCESS";
}

