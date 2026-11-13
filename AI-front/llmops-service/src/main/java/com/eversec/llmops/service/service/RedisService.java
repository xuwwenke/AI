package com.eversec.llmops.service.service;

import java.util.concurrent.TimeUnit;

/**
 * Redis String 类型操作服务接口
 */
public interface RedisService {

    /**
     * 设置字符串值（永久有效）
     *
     * @param key   键
     * @param value 值
     */
    void set(String key, String value);

    /**
     * 设置字符串值（带过期时间）
     *
     * @param key      键
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    void set(String key, String value, long timeout, TimeUnit timeUnit);

    /**
     * 设置字符串值（带过期时间，单位：秒）
     *
     * @param key     键
     * @param value   值
     * @param seconds 过期时间（秒）
     */
    void set(String key, String value, long seconds);

    /**
     * 设置整数类型值（永久有效）
     *
     * @param key   键
     * @param value 值
     */
    void setInt(String key, int value);

    /**
     * 设置整数类型值（带过期时间）
     *
     * @param key      键
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    void setInt(String key, int value, long timeout, TimeUnit timeUnit);

    /**
     * 设置整数类型值（带过期时间，单位：秒）
     *
     * @param key     键
     * @param value   值
     * @param seconds 过期时间（秒）
     */
    void setInt(String key, int value, long seconds);

    /**
     * 获取整数类型值
     *
     * @param key 键
     * @return 值，如果不存在返回 null
     */
    Integer getInt(String key);

    /**
     * 获取整数类型值（带默认值）
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值，如果不存在返回默认值
     */
    int getIntOrDefault(String key, int defaultValue);

    /**
     * 获取字符串值
     *
     * @param key 键
     * @return 值，如果不存在返回 null
     */
    String get(String key);

    /**
     * 获取字符串值（带默认值）
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值，如果不存在返回默认值
     */
    String getOrDefault(String key, String defaultValue);

    /**
     * 设置值（仅当键不存在时）
     *
     * @param key   键
     * @param value 值
     * @return true 如果设置成功，false 如果键已存在
     */
    boolean setIfAbsent(String key, String value);

    /**
     * 设置值（仅当键不存在时，带过期时间）
     *
     * @param key      键
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return true 如果设置成功，false 如果键已存在
     */
    boolean setIfAbsent(String key, String value, long timeout, TimeUnit timeUnit);

    /**
     * 设置值（仅当键存在时）
     *
     * @param key   键
     * @param value 值
     * @return true 如果设置成功，false 如果键不存在
     */
    boolean setIfPresent(String key, String value);

    /**
     * 设置值（仅当键存在时，带过期时间）
     *
     * @param key      键
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return true 如果设置成功，false 如果键不存在
     */
    boolean setIfPresent(String key, String value, long timeout, TimeUnit timeUnit);

    /**
     * 原子递增（整数）
     *
     * @param key 键
     * @return 递增后的值
     */
    long increment(String key);

    /**
     * 原子递增指定步长（整数）
     *
     * @param key   键
     * @param delta 步长
     * @return 递增后的值
     */
    long increment(String key, long delta);

    /**
     * 原子递减（整数）
     *
     * @param key 键
     * @return 递减后的值
     */
    long decrement(String key);

    /**
     * 原子递减指定步长（整数）
     *
     * @param key   键
     * @param delta 步长
     * @return 递减后的值
     */
    long decrement(String key, long delta);

    /**
     * 原子递增（浮点数）
     *
     * @param key   键
     * @param delta 步长
     * @return 递增后的值
     */
    double incrementDouble(String key, double delta);

    /**
     * 设置过期时间
     *
     * @param key      键
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return true 如果设置成功
     */
    boolean expire(String key, long timeout, TimeUnit timeUnit);

    /**
     * 设置过期时间（单位：秒）
     *
     * @param key     键
     * @param seconds 过期时间（秒）
     * @return true 如果设置成功
     */
    boolean expire(String key, long seconds);

    /**
     * 获取剩余过期时间
     *
     * @param key      键
     * @param timeUnit 时间单位
     * @return 剩余时间，如果键不存在或没有过期时间返回 -1
     */
    long getExpire(String key, TimeUnit timeUnit);

    /**
     * 删除键
     *
     * @param key 键
     * @return true 如果删除成功
     */
    boolean delete(String key);

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return true 如果存在
     */
    boolean exists(String key);

    /**
     * 获取字符串长度
     *
     * @param key 键
     * @return 字符串长度
     */
    long length(String key);

    /**
     * 追加字符串
     *
     * @param key   键
     * @param value 追加的值
     * @return 追加后字符串的总长度
     */
    long append(String key, String value);

    /**
     * 设置指定位置的字符
     *
     * @param key    键
     * @param offset 位置（从0开始）
     * @param value  字符
     */
    void setChar(String key, long offset, char value);

    /**
     * 获取指定范围的子字符串
     *
     * @param key   键
     * @param start 起始位置（从0开始）
     * @param end   结束位置（-1表示最后一个字符）
     * @return 子字符串
     */
    String getRange(String key, long start, long end);

    /**
     * 设置指定范围的字符串
     *
     * @param key   键
     * @param offset 起始位置（从0开始）
     * @param value 要设置的值
     */
    void setRange(String key, long offset, String value);
}