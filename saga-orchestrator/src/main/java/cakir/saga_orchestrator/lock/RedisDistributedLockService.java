package cakir.saga_orchestrator.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Component
public class RedisDistributedLockService {

    private static final DefaultRedisScript<Long> SAFE_UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final RedisLockProperties properties;

    public RedisDistributedLockService(StringRedisTemplate redisTemplate, RedisLockProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public <T> T executeWithLock(String businessKey, Supplier<T> action) {
        return executeWithLock(businessKey, properties.getWaitTimeout(), properties.getLeaseTime(), action);
    }

    public <T> T executeWithLock(String businessKey, Duration waitTimeout, Duration leaseTime, Supplier<T> action) {
        String lockKey = properties.getKeyPrefix() + ":" + businessKey;
        String lockToken = UUID.randomUUID().toString();

        boolean lockAcquired = tryAcquire(lockKey, lockToken, waitTimeout, leaseTime);
        if (!lockAcquired) {
            throw new IllegalStateException("Could not acquire distributed lock for key: " + businessKey);
        }

        try {
            return action.get();
        } finally {
            releaseSafely(lockKey, lockToken);
        }
    }

    private boolean tryAcquire(String lockKey, String lockToken, Duration waitTimeout, Duration leaseTime) {
        long deadlineNanos = System.nanoTime() + waitTimeout.toNanos();

        while (System.nanoTime() < deadlineNanos) {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, leaseTime);
            if (Boolean.TRUE.equals(acquired)) {
                return true;
            }

            try {
                Thread.sleep(properties.getRetryInterval().toMillis());
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    private void releaseSafely(String lockKey, String lockToken) {
        try {
            redisTemplate.execute(SAFE_UNLOCK_SCRIPT, Collections.singletonList(lockKey), lockToken);
        } catch (RuntimeException exception) {
            log.warn("Failed to release distributed lock key: {}", lockKey, exception);
        }
    }
}
