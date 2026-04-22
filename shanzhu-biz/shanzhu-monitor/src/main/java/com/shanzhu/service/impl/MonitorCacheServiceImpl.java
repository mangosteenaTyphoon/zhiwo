package com.shanzhu.service.impl;

import com.shanzhu.common.exception.ServiceException;
import com.shanzhu.common.model.bridge.setting.CacheBlackIp;
import com.shanzhu.common.utils.json.JsonUtils;
import com.shanzhu.model.CacheMonitor;
import com.shanzhu.cache.manager.RedisCacheManager;
import com.shanzhu.cache.enums.RedisKeyPrefixEnum;
import com.shanzhu.service.MonitorCacheService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.shanzhu.cache.enums.RedisKeyPrefixEnum.CAPTCHA_TYPE_VALUE_REDIS_PREFIX;

@Service
public class MonitorCacheServiceImpl implements MonitorCacheService {

    @Resource
    private RedisCacheManager redisCacheManager;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;


    @Override
    public String memoryInfo() {
        return redisCacheManager.memoryInfo();
    }

    @Override
    public List<CacheMonitor> cacheKeyGroups() {
        List<RedisKeyPrefixEnum> redisKeyPrefix = RedisKeyPrefixEnum.getRedisKeyPrefix();
        return redisKeyPrefix.stream().map(keyPrefix -> new CacheMonitor(keyPrefix.getValue(), keyPrefix.getLabel())).toList();
    }

    @Override
    public Set<String> cacheKeys(String keyPrefix) {
        if (!"OTHER".equals(keyPrefix)) {
            return redisCacheManager.keys(keyPrefix);
        }

        Set<String> keys = redisCacheManager.keys();
        // 拿到非other的Key
        List<RedisKeyPrefixEnum> redisKeyPrefix = RedisKeyPrefixEnum.getRedisKeyPrefix();
        List<String> notOtherKeys = redisKeyPrefix.stream().map(RedisKeyPrefixEnum::getValue).filter(key -> !"OTHER".equals(key)).toList();
        // 从keys中减去非other的Key，拿到other的key
        return keys
                .stream()
                .filter(k -> {
                    for (String prefix : notOtherKeys)
                        if (k.startsWith(prefix)) return false;
                    return true;
                })
                .collect(Collectors.toSet());
    }

    @SneakyThrows
    @Override
    public CacheMonitor cacheInfo(String key) {
        CacheMonitor cacheMonitor = new CacheMonitor(null, key);
        // 获取key在redis中对应的数据类型
        String redisType = redisCacheManager.getRedisType(key);

        switch (redisType) {
            case "object", "string": {
                cacheMonitor.setValue(JsonUtils.toJson(redisCacheManager.getCacheObject(key, Object.class)));
                break;
            }
            case "list": {
                cacheMonitor.setValue(JsonUtils.toJson(redisCacheManager.getCacheList(key, Object.class)));
                break;
            }
            case "map": {
                cacheMonitor.setValue(JsonUtils.toJson(redisCacheManager.getCacheMap(key, Object.class)));
                break;
            }
            // 当业务需要有其他数据类型时，可在此添加
        }
        cacheMonitor.setExpireMinutes(redisCacheManager.getExpireMinutes(key));
        return cacheMonitor;
    }

    @Override
    public void remove(String keyPrefix) {
        if (keyPrefix.startsWith(CAPTCHA_TYPE_VALUE_REDIS_PREFIX.getValue())) {
            throw new ServiceException("验证码缓存不可删除");
        }

        Set<String> keys = cacheKeys(keyPrefix);
        redisCacheManager.delete(keys);

        // 重新刷新黑名单
        applicationEventPublisher.publishEvent(new CacheBlackIp());
    }
}
