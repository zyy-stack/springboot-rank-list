package org.example.service.impl;

import org.example.Enum.ActivityRankTimeEnum;
import org.example.constant.RankConstant;
import org.example.pojo.RankItemDTO;
import org.example.service.RankService;
import org.example.util.DateUtil;
import org.example.util.NumUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RankServiceImpl implements RankService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *
     * @return 当天排行榜的key
     */
    private String todayRankKey(){
        return RankConstant.ACTIVITY_SCORE_KEY+DateUtil.formatDate( new Date(),RankConstant.yyyy_MM_dd);
    }

    /**
     *
     * @return 月度排行榜key
     */
    private String monthRankKey(){
        return RankConstant.ACTIVITY_SCORE_KEY+DateUtil.formatDate(new Date(),RankConstant.yyyy_MM);
    }

    private String userActionKey(Long userId){
        return RankConstant.ACTIVITY_SCORE_KEY+userId+DateUtil.formatDate(new Date(),RankConstant.yyyy_MM_dd);
    }

    /**
     * 添加活跃分 (考虑并发情况）
     * @param userId
     * @param activityItem
     */
    @Override
    public void addActivityScore(Long userId, String activityItem) {


        // Redis 键定义
        final String todayRankKey = todayRankKey();
        final String monthRankKey = monthRankKey();
        final String userActionKey = userActionKey(userId);
        final String lockKey = "lock:" + userId + ":" + activityItem;

        // 锁的值，使用 UUID 防止锁误删除
        String lockValue = UUID.randomUUID().toString();

        try {
            // 获取分布式锁，设置过期时间 10 秒
            Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(isLocked)) {
                // 检查活动是否已经处理过，确保幂等性
                Integer existingScore = (Integer) redisTemplate.boundHashOps(userActionKey).get(activityItem);
                if (existingScore != null) {
                    return; // 如果活动已处理过，直接返回
                }

                // 使用 Lua 脚本确保原子性和幂等性
                String luaScript = """
                if redis.call("HGET", KEYS[1], ARGV[1]) == false then
                    -- 设置用户操作记录
                    redis.call("HSET", KEYS[1], ARGV[1], ARGV[2])
                    redis.call("EXPIRE", KEYS[1], ARGV[3])
                    
                    -- 更新日排行榜
                    redis.call("ZINCRBY", KEYS[2], ARGV[2], ARGV[4])
                    
                    -- 更新月排行榜
                    redis.call("ZINCRBY", KEYS[3], ARGV[2], ARGV[4])
                    
                    -- 设置排行榜有效期
                    if redis.call("TTL", KEYS[2]) < 0 then
                        redis.call("EXPIRE", KEYS[2], ARGV[5])
                    end
                    if redis.call("TTL", KEYS[3]) < 0 then
                        redis.call("EXPIRE", KEYS[3], ARGV[6])
                    end
                    
                    return 1
                else
                    return 0
                end
            """;

                // Lua 脚本参数
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                script.setScriptText(luaScript);
                script.setResultType(Long.class);

                // 执行 Lua 脚本
                Long result = (Long) redisTemplate.execute(script,
                        Arrays.asList(userActionKey, todayRankKey, monthRankKey),
                        activityItem, // 活动项
                        RankConstant.SINGLE_ACTIVITY_SCORE, // 单次活动得分
                        31 * DateUtil.ONE_DAY_SECONDS, // 用户操作记录过期时间（秒）
                        31 * DateUtil.ONE_DAY_SECONDS, // 日排行榜有效期（秒）
                        12 * DateUtil.ONE_MONTH_SECONDS // 月排行榜有效期（秒）
                );

            }
        }catch (Exception e) {
            // 记录异常日志或进行其他处理
            e.printStackTrace();

        } finally {

            // 释放锁，确保锁的值匹配，防止误删
            String currentValue = (String) redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(lockKey);
            }

        }
    }


    /**
     * 添加活跃分 （没有考虑并发情况）
     * @param userId
     * @param activityItem
     */

    public void addActivityScore2(Long userId, String activityItem) {
        if(userId == null){
            return;
        }
        final String todayRankKey = todayRankKey();
        final String monthRankKey = monthRankKey();
        final String userActionKey = userActionKey(userId);
        Integer ans= (Integer) redisTemplate.boundHashOps(userActionKey).get(activityItem);
        if(ans == null){
            //记录用户活跃加分
            redisTemplate.boundHashOps(userActionKey).put(activityItem,RankConstant.SINGLE_ACTIVITY_SCORE);
            //设置用户操作记录的有效期为1个月
            redisTemplate.expire(userActionKey,31 * DateUtil.ONE_DAY_SECONDS, TimeUnit.MINUTES );

            //更新日排行榜
            redisTemplate.boundZSetOps(todayRankKey).incrementScore(String.valueOf(userId),RankConstant.SINGLE_ACTIVITY_SCORE);

            //更新月排行榜
            redisTemplate.boundZSetOps(monthRankKey).incrementScore(String.valueOf(userId),RankConstant.SINGLE_ACTIVITY_SCORE);

            //设置排行榜有效期
            //首次增加活跃度时，才设置有效期
            Long ttl=redisTemplate.getExpire(todayRankKey);
            if(!NumUtil.upZero(ttl)){
                redisTemplate.expire(todayRankKey,31*DateUtil.ONE_DAY_SECONDS,TimeUnit.MINUTES);

            }
            ttl=redisTemplate.getExpire(monthRankKey);
            if(!NumUtil.upZero(ttl)){
                redisTemplate.expire(monthRankKey,12*DateUtil.ONE_MONTH_SECONDS,TimeUnit.MINUTES);

            }
        }
    }



    @Override
    public List<RankItemDTO> queryTopRankList(ActivityRankTimeEnum timeEnum, int topN) {
        String rankKey=timeEnum==ActivityRankTimeEnum.DAY ? todayRankKey():monthRankKey();
        //获取topN用户
        Set<ZSetOperations.TypedTuple<String>> topMembersWithScores =
                redisTemplate.opsForZSet().reverseRangeWithScores(rankKey, 0, topN - 1);

        List<RankItemDTO> rank=topMembersWithScores.stream()
                .map(member -> new RankItemDTO().setUserId(Long.valueOf(member.getValue())).setScore(member.getScore().intValue()))
                .sorted((o1, o2) -> Integer.compare(o2.getScore(), o1.getScore()))
                .collect(Collectors.toList());

        IntStream.range(0, rank.size()).forEach(i -> rank.get(i).setRank(i + 1));
        return rank;
    }


}
