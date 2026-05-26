package com.develop.mvp.pk.module.system.framework.captcha.core;

import com.anji.captcha.service.CaptchaCacheService;
import lombok.Setter;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 实现验证码的存储
 *
 * @author David
 */
@Setter
public class RedisCaptchaServiceImpl implements CaptchaCacheService {

    private StringRedisTemplate stringRedisTemplate;

    /**
     * 执行 type 对应的业务操作。
     *
     * @return 处理结果
     */
    @Override
    public String type() {
        return "redis";
    }

    /**
     * 设置 set 对应的数据。
     *
     * @param key key 参数
     * @param value value 参数
     * @param expiresInSeconds expiresInSeconds 参数
     */
    @Override
    public void set(String key, String value, long expiresInSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
    }

    /**
     * 执行 exists 对应的业务操作。
     *
     * @param key key 参数
     * @return 处理结果
     */
    @Override
    public boolean exists(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 删除 delete 对应的数据。
     *
     * @param key key 参数
     */
    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 查询 get 对应的数据。
     *
     * @param key key 参数
     * @return 处理结果
     */
    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 执行 increment 对应的业务操作。
     *
     * @param key key 参数
     * @param val val 参数
     * @return 处理结果
     */
    @Override
    public Long increment(String key, long val) {
        return stringRedisTemplate.opsForValue().increment(key,val);
    }

}
