package org.dotspace.oofp;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;

@Configuration  // 声明这是一个配置类
@ComponentScan(basePackages = "org.dotspace.oofp")
public class TestConfig {

    @Bean
    public Validator getValidator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(
            @Qualifier("acsSpringRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

}
