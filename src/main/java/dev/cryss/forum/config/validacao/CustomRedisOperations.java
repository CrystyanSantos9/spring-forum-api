package dev.cryss.forum.config.validacao;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

public interface CustomRedisOperations<K> {
   Cursor<String> scan(ScanOptions scanOptions);


}
