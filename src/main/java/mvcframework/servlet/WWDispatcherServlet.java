package mvcframework.servlet;

import mvcframework.annotition.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class WWDispatcherServlet extends HttpServlet {

    private Properties contextProperties = new Properties();
    private List<String> classNames = new ArrayList<String>();
    private Map<String, Object> ioc = new HashMap<String, Object>();
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
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
        System.out.println("mini Spring MVC is init");
    }

    private void doHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(WWController.class)) {
                continue;
            }
            //class上加的requestMapping注解作为baseUrl
            String baseUrl = "";
            if (clazz.isAnnotationPresent(WWRequestMapping.class)) {
                WWRequestMapping requestMapping = clazz.getAnnotation(WWRequestMapping.class);
                baseUrl = requestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(WWRequestMapping.class)) {
                    continue;
                }
                WWRequestMapping requestMapping = method.getAnnotation(WWRequestMapping.class);

                String url = "/" + baseUrl + "/" + requestMapping.value().replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapped:" + url + "," + method);
            }
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(WWAutowired.class)) {
                    continue;
                }
                WWAutowired autowired = field.getAnnotation(WWAutowired.class);
                String beanName = autowired.value();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                //如果private protected default
                field.setAccessible(true);//强制访问字段
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(WWController.class)) {
                    Object instance = clazz.newInstance();
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(WWService.class)) {
                    //service为接口的话初始化的是它的实现类
                    //1.默认是类名的首字母小写作为key
                    WWService service = clazz.getAnnotation(WWService.class);
                    String beanName = service.value();
                    //2.用户自定义beanName，要优先使用自定义的beanName
                    if ("".equals(beanName)) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //3.赋值的对象是接口，要采用接口的全类名作为key，实现类实例作为值，方便依赖注入时使用
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The beanName" + i.getName() + " is exists!!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String lowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {

        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = (scanPackage + "." + file.getName().replace("class", ""));
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextProperties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
