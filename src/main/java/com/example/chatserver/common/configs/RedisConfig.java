package com.example.chatserver.common.configs;

import com.example.chatserver.chat.service.RedisPubSubService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port; // 6379

    /**
     * 기본 연결 객체 - redis 연결을 위한 연결 객체
     * redis pub sub은 특정 DB에 종속적이지 않음
     */
    @Bean
    @Qualifier("chatPubSub") // connection 객체를 여러개 만들 수 있음(db 별로 사용)
    public RedisConnectionFactory chatPubSubFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        // redis pub sub에서는 특정 데이터베이스에 의존적이지 않음 -> 굳이 데이터베이스 설정 필요 없음
        // configuration.setDatabase(0); // 0번 데이터베이스 사용 (0-9)
        return new LettuceConnectionFactory(configuration);
    }

    /**
     * publish 객체 - 연결 객체 필요
     * 연결 객체가 여러개라면 Qualifier 필요
     * 일반적으로 RedisTemplate<key 데이터 타입, data 데이터 타입> 사용
     * key-value가 아닌 String으로 사용하기위해 StringRedisTemplate 사용
     */
    @Bean
    @Qualifier("chatPubSub") // StringRedisTemplate의 이름 지어주는 것
    public StringRedisTemplate stringRedisTemplate(@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    /**
     * subscribe 객체 - 연결 객체 필요
     * Listen하고 받아온 message를 spring 코드에서 처리해줘야함
     * 처리해주는 MessageListener를 주입해줘야한다.
     * @return
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter
            ){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("chat")); // redis pub sub도 특정 주제에 대해서 진행 -> room 이름 지정

        return container;
    }


    /**
     * edis에서 수신된 메시지를 처리하는 객체 생성
     * Listen하고 해당 메서드에서 처리
     * 수신한 다음 처리할 클래스
     * @param redisPubSubService -> 메시지 수신한 다음 처리할 클래스 주입
     * @return 특정 클래스, 특정 메서드를 지정해서 다시 던져줌
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService) {
        // RedisPubSubService의 특정 메서드가 수신된 메시지를 처리할 수 있도록 지정
        return new MessageListenerAdapter(redisPubSubService, "onMessage");
    }

}
