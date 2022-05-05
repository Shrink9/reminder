package com.example.remindlearning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 被提醒者
 * @TableName reminded_person
 */
@TableName(value ="reminded_person")
@Data
public class RemindedPerson implements Serializable {
    public static final String NOT_PAIED="未支付";
    public static final String PAIED="已支付";
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *
     */
    private String email;

    /**
     * 提醒文本
     */
    private String remindText;

    /**
     * 提醒者
     */
    private String reminder;

    /**
     * 已经提醒的次数
     */
    private Integer count;

    /**
     * 对被提醒者的称呼
     */
    private String name;

    /**
     *
     */
    private Boolean isDisabled;

    /**
     * 支付状态
格式:count-支付状态
     */
    private String status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
