package cn.fintecher.pangolin.service.reminder.util;

import org.springframework.context.ApplicationContext;

/**
 * Created by ChenChang on 2017/3/23.
 */
public class SpringContextUtil {
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}

