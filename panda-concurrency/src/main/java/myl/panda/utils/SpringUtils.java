package myl.panda.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class SpringUtils{
    private static ApplicationContext applicationContext;

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static <T> T getBean(String beanName, Class<T> requiredType){
        return (T)applicationContext.getBean(beanName, requiredType);
    }

    public static <T> T getBean(Class<?> clazz){
        return (T)applicationContext.getBean(clazz);
    }

    public static void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }
}
