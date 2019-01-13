package myl.panda;

import myl.panda.concurrency.BaseTaskFactory;
import myl.panda.concurrency.redis.ConRedisTemplate;
import myl.panda.timers.TimerService;
import myl.panda.utils.RedisLockUtils;
import myl.panda.utils.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import javax.annotation.PreDestroy;

/**
 * panda的spring配置类
 * create by maoyule on 2019/1/13
 */
@Configuration
public class PandaConfiguration implements BeanPostProcessor, InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(PandaConfiguration.class);
    protected ConfigurableApplicationContext context;

    @Value("${redisLockIdPre:lockId}")
    private String redisLockIdPre;

    public PandaConfiguration() {

    }

    @Override
    public void afterPropertiesSet() {
        SpringUtils.setApplicationContext(context);
        dealTaskFactory();
        dealTimerService();
        dealRedis();
    }

    protected void dealTaskFactory() {
        BaseTaskFactory factory = null;
        try {
            factory = this.context.getBean(BaseTaskFactory.class);
        }catch (Exception e){

        }
        if (factory != null) {
            return;
        }
        factory = BaseTaskFactory.getFactory();
        if (factory == null) {
            // 如果子项目没有复写BaseTaskFactory并将其注入spring，此处new factory，并将其注入spring.
            factory = new BaseTaskFactory();
        }
        this.context.getBeanFactory().registerSingleton(BaseTaskFactory.class.getSimpleName(), factory);
    }

    protected void dealTimerService(){
        TimerService service = null;
        try {
            service = this.context.getBean(TimerService.class);
        }catch (Exception e){

        }
        if(service != null){
            return;
        }
        service = TimerService.getService();
        if (service == null) {
            // 如果子项目没有复写TimerService并将其注入spring，此处new TimerService并将其注入spring.
            service = new TimerService();
        }
        this.context.getBeanFactory().registerSingleton(TimerService.class.getSimpleName(), service);
    }

    protected void dealRedis(){
        RedisLockUtils.setRedisLockIdPre(redisLockIdPre);
        ConRedisTemplate redisTemplate = dealRedisTemplate();
        if(redisTemplate != null){
            RedisLockUtils.setRedisTemplate(redisTemplate);
        }
    }

    protected ConRedisTemplate dealRedisTemplate(){
        ConRedisTemplate redisTemplate = null;
        try {
            this.context.getBean(ConRedisTemplate.class);
        }catch (Exception e){

        }
        if(redisTemplate != null){
            return redisTemplate;
        }
        JedisConnectionFactory connectionFactory = null;
        try {
            connectionFactory = this.context.getBean(JedisConnectionFactory.class);
        }catch (Exception e){

        }
        if(connectionFactory == null){
            logger.warn("connectionFactory not found. redis lock can not be used.");
            return null;
        }
        redisTemplate = new ConRedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.afterPropertiesSet();
        this.context.getBeanFactory().registerSingleton(ConRedisTemplate.class.getSimpleName(), redisTemplate);
        return redisTemplate;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (ConfigurableApplicationContext) applicationContext;
    }

    @PreDestroy
    public void destroy(){
        if(BaseTaskFactory.getFactory() != null){
            BaseTaskFactory.getFactory().dispose();
        }
        if(TimerService.getService() != null){
            TimerService.getService().dispose();
        }
    }
}
