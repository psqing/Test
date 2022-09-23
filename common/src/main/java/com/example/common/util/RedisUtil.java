package com.example.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author ponder
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    /**
     * 一天 + 1个小时  后面的随机数防止缓存穿透和击穿
     */
    private static final long EXPIRE_TIME = 60L * 60L * 24L + RandomUtil.randomInt(1, 60 * 60);

    private final RedisTemplate<String, Object> redisTemplate;

    // ============================= Redis 消息订阅/发布 ==============

    /**
     * 消息发布
     *
     * @param channel 发布topic
     * @param obj     发送消息
     */
    public boolean publish(String channel, Object obj) {
        redisTemplate.convertAndSend(channel, obj);
        return true;
    }
    // =============================common============================

    /**
     * 获取所有Key
     *
     * @param pattern 为空时 匹配所有key
     * @return
     */
    public Set<String> keys(String pattern) {
        if (StrUtil.isEmpty(pattern)) {
            pattern = "*";
        }
        return redisTemplate.keys(pattern);
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("redis expire cache error", e);
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            if (StrUtil.isNotEmpty(key)) {
                return redisTemplate.hasKey(key);
            }
            return false;
        } catch (Exception e) {
            log.error("redis hasKey error", e);
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollUtil.toList(key));
            }
        }
    }


    /**
     * 删除缓存
     *
     * @param keys 可以传一个值 或多个
     */
    public void del(Set<String> keys) {
        if (keys != null && keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 删除以key为前缀的数据
     *
     * @param key
     */
    public void delPrefix(String key) {
        // 获取Redis中特定前缀
        Set<String> keys = redisTemplate.keys(key + "*");
        redisTemplate.delete(keys);
    }

    /**
     * 删除patternKey数据, 正则表达式
     *
     * @param patternKey
     */
    public void delPattern(String patternKey) {
        Set<String> keys = redisTemplate.keys(patternKey);
        redisTemplate.delete(keys);
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @param clazz  类型
     * @return 值
     */
    public <T> T get(String key, Class<T> clazz){
        Object o = this.get(key);
        return JSONUtil.toBean(JSONUtil.parseObj(o), clazz);
    }

    /**
     * 普通缓存放入
     * 默认为一天
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        return this.set(key, value, EXPIRE_TIME);
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("redis set key:{},value:{},time:{} error", key, value, time, e);
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        Assert.isTrue(delta > 0, "递增因子必须大于0");
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        Assert.isTrue(delta > 0, "递增因子必须大于0");
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取所有value
     *
     * @param key
     * @return
     */
    public List<Object> hgets(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 返回 key 中的数量
     * @param key
     * @return
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }
    /**
     * HashSet 默认为一天
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<String, Object> map) {
        return this.hmset(key, map, EXPIRE_TIME);
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            } else {
                expire(key, EXPIRE_TIME);
            }
            return true;
        } catch (Exception e) {
            log.error("redis hmset key:{},value:{},time:{} error", key, map, time, e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     * 默认为一天
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        return this.hset(key, item, value, EXPIRE_TIME);
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            } else {
                expire(key, EXPIRE_TIME);
            }
            return true;
        } catch (Exception e) {
            log.error("redis hset key:{},item:{},value:{},time:{} error", key, item, value, time, e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public Object hdel(String key, Object... item) {
        return redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     * 默认为一天
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public Double hincr(String key, String item, Double by) {
        Double increment = redisTemplate.opsForHash().increment(key, item, by);
        expire(key, EXPIRE_TIME);
        return increment;
    }

    /**
     * hash递减
     * 默认为一天
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    public Double hdecr(String key, String item, Double by) {
        Double increment = redisTemplate.opsForHash().increment(key, item, -by);
        expire(key, EXPIRE_TIME);
        return increment;
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("redis sGet key:{} error", key, e);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("redis sHasKey key:{},value:{} error", key, value, e);
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     * 默认为一天
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            Long add = redisTemplate.opsForSet().add(key, values);
            expire(key, EXPIRE_TIME);
            return add;
        } catch (Exception e) {
            log.error("redis sSet key:{},value:{} error", key, values, e);
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            } else {
                expire(key, EXPIRE_TIME);
            }
            return count;
        } catch (Exception e) {
            log.error("redis sSetAndTime key:{},value:{},time:{} error", key, values, time, e);
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("redis sGetSetSize key:{}  error", key, e);
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            log.error("redis setRemove key:{},value:{} error", key, values, e);
            return 0;
        }
    }
    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("redis lGet key:{},start:{},end:{} error", key, start, end, e);
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("redis lGetListSize key:{} error", key, e);
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("redis lGetIndex key:{},index:{} error", key, index, e);
            return null;
        }
    }

    /**
     * 将list放入缓存
     * 默认为一天
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        return this.lSet(key, value, EXPIRE_TIME);
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            } else {
                expire(key, EXPIRE_TIME);
            }
            return true;
        } catch (Exception e) {
            log.error("redis lset key:{},value:{},time:{} error", key, value, time, e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     * 默认为一天
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        return this.lSet(key, value, EXPIRE_TIME);
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            } else {
                expire(key, EXPIRE_TIME);
            }
            return true;
        } catch (Exception e) {
            log.error("redis lSet key:{},value:{},time:{} error", key, value, time, e);
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            expire(key, EXPIRE_TIME);
            return true;
        } catch (Exception e) {
            log.error("redis lUpdateIndex key:{},value:{},index:{} error", key, value, index, e);
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove;
            remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            log.error("redis lRemove key:{},value:{},count:{} error", key, value, count, e);
            return 0;
        }
    }
}
