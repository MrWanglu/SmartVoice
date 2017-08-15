package cn.fintecher.pangolin.business.config;

import cn.fintecher.pangolin.business.job.AutowiringSpringBeanJobFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;

/**
 * @Author: PeiShouWen
 * @Description: quartz调度配置
 * @Date 9:54 2017/8/9
 */
@Configuration
@ConditionalOnProperty(name = "quartz.enabled")
public class ConfigureQuartz {


    /**
     * job加入spring容器中
     * @param applicationContext
     * @return
     */
    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     * 初始化调度器
     * @param dataSource
     * @param jobFactory
     * @return
     * @throws IOException
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, JobFactory jobFactory) throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(true);
        factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties());
        return factory;
    }

    /**
     * 加载quartz 配置参数
     * @return
     * @throws IOException
     */
    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    /**
     * 简单的触发器
     * @param triggerGroup 触发器所在组
     * @param triggerName 触发器名称
     * @param beanName bean名称
     * @param description 描述
     * @param jobDetail job
     * @param pollFrequencyMs 触发条件
     * @return
     */
    public static SimpleTriggerFactoryBean createTrigger(String triggerGroup,String triggerName,String beanName,
                                                         String description,JobDetail jobDetail, long pollFrequencyMs,JobDataMap jobDataMap) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setGroup(triggerGroup);
        factoryBean.setName(triggerName);
        factoryBean.setBeanName(beanName);
        factoryBean.setDescription(description);
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setStartDelay(0L);
        factoryBean.setRepeatInterval(pollFrequencyMs);
        factoryBean.setJobDataMap(jobDataMap);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
        return factoryBean;
    }

    /**
     * cron表达式触发器
     * @param triggerGroup 触发器所在组
     * @param triggerName 触发器名称
     * @param beanName bran 名称
     * @param description 描述
     * @param jobDetail job
     * @param cronExpression cron表达式
     * @return
     */
    public static CronTriggerFactoryBean createCronTrigger(String triggerGroup, String triggerName, String beanName,
                                                           String description, JobDetail jobDetail, String cronExpression,JobDataMap jobDataMap) {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setGroup(triggerGroup);
        factoryBean.setName(triggerName);
        factoryBean.setBeanName(beanName);
        factoryBean.setDescription(description);
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setJobDataMap(jobDataMap);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        return factoryBean;
    }

    /**
     *具体的JOB
     * @param jobClass
     * @param group job所在组
     * @param name job名称
     * @param description job描述
     * @return
     */
    public static JobDetail createJobDetail(Class jobClass,String group ,String name,String description) {
        JobDetail jobDetail = newJob(jobClass)
                .withIdentity(name, group) // name "myJob", group "group1"
                .withDescription(description)
                .storeDurably(true) //记录到数据库
                .build();
        return jobDetail;
    }
}
