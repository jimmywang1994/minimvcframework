package com.ww.demo.service.impl;

import com.ww.demo.service.IDemoService;
import mvcframework.annotition.WWService;

//核心业务逻辑
@WWService
public class IDemoServiceImpl implements IDemoService {
    @Override
    public String get(String name) {
        return "hello"+name;
    }
}
