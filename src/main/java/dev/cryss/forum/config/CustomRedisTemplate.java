package dev.cryss.forum.config;

import dev.cryss.forum.config.validacao.CustomRedisOperations;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.SerializationUtils;
import reactor.util.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

public class CustomRedisTemplate<K, V> extends RedisTemplate  <K, V> implements CustomRedisOperations<K> {


    @Nullable
    private RedisSerializer customKeySerializer = null;




    @Override
    public Cursor<String> scan(ScanOptions scanOptions) {
        Assert.notNull (scanOptions, "ScanOptions must not be null");

        return  this.executeWithStickyConnection ((connection) ->{
            return new ConvertingCursor<> (connection.scan (scanOptions), new Converter<byte[], String> () {
                @Override
                public String convert(byte[] bytes) {
                    try {
                        return new String (bytes, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException (e);
                    }
                }
            });
        });
    }

//    public K deserializeKey(byte[] value) {
////        byte[] data = SerializationUtils.serialize(value);
//        return this.customKeySerializer != null ? this.customKeySerializer.deserialize (value) : (K) SerializationUtils.serialize(value);
//    }

}
