package com.ww.demo.mvc.action;

import com.ww.demo.service.IDemoService;
import mvcframework.annotition.WWAutowired;
import mvcframework.annotition.WWController;
import mvcframework.annotition.WWRequestMapping;
import mvcframework.annotition.WWRequestParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WWController
@WWRequestMapping("/demo")
public class DemoAction {

    @WWAutowired
    private IDemoService demoService;

    @WWRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp, @WWRequestParameter("name") String name) {
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @WWRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @WWRequestParameter("a")Integer a, @WWRequestParameter("b")Integer b) {
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @WWRequestMapping("/remove")
    public void remove(HttpServletRequest req, HttpServletResponse resp,@WWRequestParameter("id") String id) {

    }
}
