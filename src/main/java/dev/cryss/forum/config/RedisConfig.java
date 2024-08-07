package dev.cryss.forum.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

@Configuration
public class RedisConfig {
    @Value ("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);

        return new LettuceConnectionFactory(configuration);
    }


    @Primary
    @Bean("redisTemplate")
    @DependsOn("redisConnectionFactory")
    public CustomRedisTemplate<String, Object> redisTemplate(@Qualifier("redisConnectionFactory") LettuceConnectionFactory redisConnectionFactory){
        return getRedisTemplate (redisConnectionFactory);
    }


    private CustomRedisTemplate<String, Object> getRedisTemplate(LettuceConnectionFactory redisConnectionFactory){
        CustomRedisTemplate<String, Object> template = new CustomRedisTemplate<> ();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer (StandardCharsets.UTF_8);



        template.setKeySerializer (stringRedisSerializer);
        template.setHashKeySerializer (stringRedisSerializer);

        RedisSerializer redisSerializer = getValueSerializer ();

        template.setValueSerializer (getValueSerializer());
        template.setHashValueSerializer (getValueSerializer());
        template.setDefaultSerializer (getValueSerializer());

        template.setConnectionFactory (redisConnectionFactory);
        template.afterPropertiesSet ();
        return  template;
    }



    //1 - Cria o Serializador Desserializador
    private RedisSerializer<Object> getValueSerializer(){
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<> (Object.class);
        ObjectMapper objectMapper = new ObjectMapper ();
        objectMapper.registerModule(new JavaTimeModule ());
        objectMapper.setSerializationInclusion (JsonInclude.Include.NON_NULL);
        objectMapper.activateDefaultTyping (
                objectMapper.getPolymorphicTypeValidator (),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        objectMapper.setVisibility (PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jackson2JsonRedisSerializer.setObjectMapper (objectMapper);
        return  jackson2JsonRedisSerializer;
    }

    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheConfiguration cacheConfig = myDefaultCacheConfig(Duration.ofMinutes(10)).disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(cacheConfig)
                .withCacheConfiguration("listaDeTopicos", myDefaultCacheConfig(Duration.ofMinutes (10)))
                .withCacheConfiguration("tutorial", myDefaultCacheConfig(Duration.ofMinutes(1)))
                .build();
    }



    private RedisCacheConfiguration myDefaultCacheConfig(Duration duration) {
        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(duration)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(getValueSerializer ()))
                .serializeKeysWith (RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer ()))
                .computePrefixWith (cacheName -> cacheName + ":");
    }


}
