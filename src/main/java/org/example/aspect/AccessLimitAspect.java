package org.example.aspect;


import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.Enum.BaseExceptionEnum;
import org.example.annotation.AccessLimit;
import org.example.exception.BaseException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 防刷切面实现类
 */
@Aspect
@Component
@Slf4j
public class AccessLimitAspect {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 锁住时的key前缀
     */
    public static final String LOCK_PREFIX = "LOCK";

    /**
     * 统计次数时的key前缀
     */
    public static final String COUNT_PREFIX = "COUNT";


    /**
     * 定义切点Pointcut
     */
    @Pointcut("@annotation(org.example.annotation.AccessLimit)")
    public void excudeService() {
    }

    // 前置通知、在切点方法之前执行
    @Before("excudeService()")
    public void doAround(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AccessLimit pd = method.getAnnotation(AccessLimit.class);
        long second = pd.second();
        long maxRequestCount = pd.maxRequestCount();
        long forbiddenTime = pd.forbiddenTime();
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        if (isForbindden(second, maxRequestCount, forbiddenTime, ip, uri)) {
            throw new BaseException(BaseExceptionEnum.OPERATION_TOO_FREQUENT);
        }
    }

    /**
     * 判断某用户访问某接口是否已经被禁用/是否需要禁用
     *
     * @param second        多长时间  单位/秒
     * @param maxRequestCount       最大访问次数
     * @param forbiddenTime 禁用时长 单位/秒
     * @param ip            访问者ip地址
     * @param uri           访问的uri
     * @return ture为需要禁用
     */
    private boolean isForbindden(long second, long maxRequestCount, long forbiddenTime, String ip, String uri) {
        String lockKey = LOCK_PREFIX + ip + uri; //如果此ip访问此uri被禁用时的存在Redis中的 key
        Object isLock = redisTemplate.opsForValue().get(lockKey);
        // 判断此ip用户访问此接口是否已经被禁用
        if (Objects.isNull(isLock)) {
            // 还未被禁用
            String countKey = COUNT_PREFIX + ip + uri;
            Object count = redisTemplate.opsForValue().get(countKey);
            if (Objects.isNull(count)) {

                log.info("首次访问");
                redisTemplate.opsForValue().set(countKey, 1, second, TimeUnit.SECONDS);
            } else {

                if ((Integer) count < maxRequestCount) {
                    redisTemplate.opsForValue().increment(countKey);
                } else {
                    log.info("{}禁用访问{}", ip, uri);
                    log.info(lockKey);
                    // 禁用
                    redisTemplate.opsForValue().set(lockKey, 1, forbiddenTime, TimeUnit.SECONDS);
                    // 删除统计--已经禁用了就没必要存在了
                    redisTemplate.delete(countKey);
                    return true;
                }
            }
        } else {
            // 此用户访问此接口已被禁用
            return true;
        }
        return false;
    }
}

