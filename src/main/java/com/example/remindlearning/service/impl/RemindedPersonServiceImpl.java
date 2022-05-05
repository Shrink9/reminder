package com.example.remindlearning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.remindlearning.entity.RemindedPerson;
import com.example.remindlearning.service.RemindedPersonService;
import com.example.remindlearning.mapper.RemindedPersonMapper;
import org.springframework.stereotype.Service;

@Service
public class RemindedPersonServiceImpl extends ServiceImpl<RemindedPersonMapper,RemindedPerson> implements RemindedPersonService{
}




