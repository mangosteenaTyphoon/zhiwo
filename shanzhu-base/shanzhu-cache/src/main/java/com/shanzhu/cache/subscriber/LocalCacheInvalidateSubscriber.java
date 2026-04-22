package com.shanzhu.cache.subscriber;

import com.shanzhu.cache.enums.RedisTopicEnum;
import com.shanzhu.cache.manager.LocalCacheManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * 本地缓存失效处理器
 */
@Component
public class LocalCacheInvalidateSubscriber {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private LocalCacheManager localCacheManager;

    @PostConstruct
    public void subscribe() {
        RTopic topic = redissonClient.getTopic(RedisTopicEnum.INVALIDATE_LOCAL_CACHE.getValue());
        topic.addListener(String.class, (channel, msg) -> localCacheManager.remove(msg));
    }

}