package dev.cryss.forum;

import dev.cryss.forum.config.CustomRedisTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.util.Objects;

@SpringBootApplication
@EnableSpringDataWebSupport
@EnableCaching
public class ForumApplication  {

	public static void main(String[] args) {
		SpringApplication.run(ForumApplication.class, args);
	}
}
