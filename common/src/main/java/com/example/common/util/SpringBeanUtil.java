package com.example.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author ponder
 * SpringBean工具类
 *
 * @param <T>
 */
@Component
public class SpringBeanUtil<T> implements ApplicationContextAware {

    private static ApplicationContext context = null;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        if (context == null) {
            context = applicationContext;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }


    /**
     * 根据名称获取Bean
     */
    public static<T> Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 根据Class获取Bean
     */
    public static<T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 根据名称和Class获取bean
     */
    public static<T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}
