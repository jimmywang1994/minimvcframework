package mvcframework.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WWDispatcherServlet extends HttpServlet {

    private Properties contextProperties=new Properties();
    private List<String> classNames=new ArrayList<String>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req,resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));



        //2.扫描并加载相关的类
        doScanner(contextProperties.getProperty("scanPackage"));


        //3.初始化IOC容器
        doInstance();


        //4.反射依赖注入(自动赋值)
        doAutowired();


        //5.构建HandlerMapping,将url和method建立一对一关系
        doHandlerMapping();
    }

    private void doHandlerMapping() {
    }

    private void doAutowired() {
    }

    private void doInstance() {
        if(classNames.isEmpty()){return;}
        try {
            for (String className : classNames) {
                Class<?> clazz=Class.forName(className);

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void doScanner(String scanPackage) {

        URL url=this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File classDir=new File(url.getFile());
        for (File file:classDir.listFiles()){
            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                if(!file.getName().endsWith(".class")){continue;}
                String className=(scanPackage+"."+file.getName().replace("class",""));
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is=this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextProperties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(null!=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void a(){}
}
