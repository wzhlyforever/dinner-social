package com.imooc.seckill.model;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * @program: food-social-contact-parent
 * @description: redis实现分布式锁
 * @author: Mr.Wang
 * @create: 2022-04-17 21:26
 **/
@Getter
@Setter
public class RedisLock {


  @Resource
  private RedisTemplate redisTemplate;

  @Resource
  private DefaultRedisScript<Long> lockScript;

  @Resource
  private DefaultRedisScript<Long> unlockScript;




  public RedisLock(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;

    lockScript = new DefaultRedisScript<>();
    lockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lock.lua")));
    lockScript.setResultType(Long.class);

    unlockScript = new DefaultRedisScript<>();
    unlockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("unlock.lua")));
    unlockScript.setResultType(Long.class);

  }

  public String tryLock(String lockName, Integer expireTime) {
    String key = UUID.randomUUID().toString();  // 防止jvm进程id发生重复
    final List<String> keys = Collections
        .singletonList(lockName);
    final Long result = (Long) redisTemplate
        .execute(lockScript, keys, key + Thread.currentThread().getId(), expireTime);

    if (result != null || result.intValue() == 1) {
      return key;   // 返回锁的标识
    }
    return null;
  }

  public void unLock(String lockName, String key) {
    redisTemplate.execute(unlockScript,
        Collections.singletonList(lockName),
        key + Thread.currentThread().getId(), null);
  }
}
