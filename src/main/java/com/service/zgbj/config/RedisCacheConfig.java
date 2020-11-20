package com.service.zgbj.config;

import com.service.zgbj.constant.Constant;
import com.service.zgbj.im.SocketMessageReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * redis发布订阅模式配置类
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {


    @Autowired
    private SocketMessageReceiver socketPushMessageReceiver;

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //接受消息的key 多个key即添加多个监听, listenerAdapter中的实现类名字要对应
        container.addMessageListener(socketPushMessageListenerAdapter(), new PatternTopic(Constant.RedisKey.im_message_subscribe));
        return container;
    }

    /**
     * 绑定消息监听者和接收监听的方法
     * 多个监听者 则实现ReceiverRedisMessage即可
     * @return
     */
    @Bean
    public MessageListenerAdapter socketPushMessageListenerAdapter() {
        return new MessageListenerAdapter(socketPushMessageReceiver, "receiveMessage");
    }
}
