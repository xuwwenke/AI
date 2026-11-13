package com.eversec.llmops.service.service.impl;

import com.eversec.llmops.service.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis String 类型操作服务实现
 * 线程安全，所有操作都是原子的
 */
@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void set(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
            log.debug("Redis set key: {}, value length: {}", key, value != null ? value.length() : 0);
        } catch (Exception e) {
            log.error("Redis set error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public void setInt(String key, int value) {
        try {
            stringRedisTemplate.opsForValue().set(key, String.valueOf(value));
            log.debug("Redis setInt key: {}, value: {}", key, value);
        } catch (Exception e) {
            log.error("Redis setInt error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public void setInt(String key, int value, long timeout, TimeUnit timeUnit) {
        try {
            stringRedisTemplate.opsForValue().set(key, String.valueOf(value), timeout, timeUnit);
            log.debug("Redis setInt key: {}, value: {}, timeout: {} {}", key, value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis setInt with timeout error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public void setInt(String key, int value, long seconds) {
        setInt(key, value, seconds, TimeUnit.SECONDS);
    }

    @Override
    public Integer getInt(String key) {
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Redis getInt key: {}, value is null", key);
                return null;
            }
            Integer result = Integer.parseInt(value);
            log.debug("Redis getInt key: {}, value: {}", key, result);
            return result;
        } catch (NumberFormatException e) {
            log.warn("Redis getInt parse error, key: {}, value is not an integer", key);
            return null;
        } catch (Exception e) {
            log.error("Redis getInt error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public int getIntOrDefault(String key, int defaultValue) {
        Integer value = getInt(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            log.debug("Redis set key: {}, value length: {}, timeout: {} {}", 
                    key, value != null ? value.length() : 0, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis set with timeout error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public void set(String key, String value, long seconds) {
        set(key, value, seconds, TimeUnit.SECONDS);
    }

    @Override
    public String get(String key) {
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            log.debug("Redis get key: {}, value exists: {}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("Redis get error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean setIfAbsent(String key, String value) {
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value);
            boolean success = Boolean.TRUE.equals(result);
            log.debug("Redis setIfAbsent key: {}, success: {}", key, success);
            return success;
        } catch (Exception e) {
            log.error("Redis setIfAbsent error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit timeUnit) {
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
            boolean success = Boolean.TRUE.equals(result);
            log.debug("Redis setIfAbsent key: {}, success: {}, timeout: {} {}", 
                    key, success, timeout, timeUnit);
            return success;
        } catch (Exception e) {
            log.error("Redis setIfAbsent with timeout error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public boolean setIfPresent(String key, String value) {
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfPresent(key, value);
            boolean success = Boolean.TRUE.equals(result);
            log.debug("Redis setIfPresent key: {}, success: {}", key, success);
            return success;
        } catch (Exception e) {
            log.error("Redis setIfPresent error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public boolean setIfPresent(String key, String value, long timeout, TimeUnit timeUnit) {
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfPresent(key, value, timeout, timeUnit);
            boolean success = Boolean.TRUE.equals(result);
            log.debug("Redis setIfPresent key: {}, success: {}, timeout: {} {}", 
                    key, success, timeout, timeUnit);
            return success;
        } catch (Exception e) {
            log.error("Redis setIfPresent with timeout error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public long increment(String key) {
        try {
            Long result = stringRedisTemplate.opsForValue().increment(key);
            log.debug("Redis increment key: {}, result: {}", key, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis increment error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public long increment(String key, long delta) {
        try {
            Long result = stringRedisTemplate.opsForValue().increment(key, delta);
            log.debug("Redis increment key: {}, delta: {}, result: {}", key, delta, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis increment with delta error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public long decrement(String key) {
        try {
            Long result = stringRedisTemplate.opsForValue().decrement(key);
            log.debug("Redis decrement key: {}, result: {}", key, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis decrement error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public long decrement(String key, long delta) {
        try {
            Long result = stringRedisTemplate.opsForValue().decrement(key, delta);
            log.debug("Redis decrement key: {}, delta: {}, result: {}", key, delta, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis decrement with delta error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public double incrementDouble(String key, double delta) {
        try {
            Double result = stringRedisTemplate.opsForValue().increment(key, delta);
            log.debug("Redis incrementDouble key: {}, delta: {}, result: {}", key, delta, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis incrementDouble error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            Boolean result = stringRedisTemplate.expire(key, timeout, timeUnit);
            boolean success = Boolean.TRUE.equals(result);
            log.debug("Redis expire key: {}, timeout: {} {}, success: {}", key, timeout, timeUnit, success);
            return success;
        } catch (Exception e) {
            log.error("Redis expire error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public boolean expire(String key, long seconds) {
        return expire(key, seconds, TimeUnit.SECONDS);
    }

    @Override
    public long getExpire(String key, TimeUnit timeUnit) {
        try {
            Long result = stringRedisTemplate.getExpire(key, timeUnit);
            log.debug("Redis getExpire key: {}, result: {} {}", key, result, timeUnit);
            return result != null ? result : -1;
        } catch (Exception e) {
            log.error("Redis getExpire error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            Boolean result = stringRedisTemplate.delete(key);
            boolean success = Boolean.TRUE.equals(result);
            log.debug("Redis delete key: {}, success: {}", key, success);
            return success;
        } catch (Exception e) {
            log.error("Redis delete error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean result = stringRedisTemplate.hasKey(key);
            boolean exists = Boolean.TRUE.equals(result);
            log.debug("Redis exists key: {}, result: {}", key, exists);
            return exists;
        } catch (Exception e) {
            log.error("Redis exists error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public long length(String key) {
        try {
            Long result = stringRedisTemplate.opsForValue().size(key);
            log.debug("Redis length key: {}, result: {}", key, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis length error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public long append(String key, String value) {
        try {
            Long result = Long.valueOf(stringRedisTemplate.opsForValue().append(key, value));
            log.debug("Redis append key: {}, value length: {}, result: {}", key, value != null ? value.length() : 0, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis append error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public void setChar(String key, long offset, char value) {
        try {
            stringRedisTemplate.opsForValue().set(key, String.valueOf(value), offset);
            log.debug("Redis setChar key: {}, offset: {}, value: {}", key, offset, value);
        } catch (Exception e) {
            log.error("Redis setChar error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public String getRange(String key, long start, long end) {
        try {
            String result = stringRedisTemplate.opsForValue().get(key, start, end);
            log.debug("Redis getRange key: {}, start: {}, end: {}, result length: {}", 
                    key, start, end, result != null ? result.length() : 0);
            return result;
        } catch (Exception e) {
            log.error("Redis getRange error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    @Override
    public void setRange(String key, long offset, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, offset);
            log.debug("Redis setRange key: {}, offset: {}, value length: {}", key, offset, value != null ? value.length() : 0);
        } catch (Exception e) {
            log.error("Redis setRange error, key: {}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }
}