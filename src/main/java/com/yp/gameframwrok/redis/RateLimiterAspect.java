package com.yp.gameframwrok.redis;

import com.yp.gameframwrok.annotation.RateLimiter;
import com.yp.gameframwrok.exception.ServiceException;
import com.yp.gameframwrok.utils.RedisRateLimiter;
import java.lang.reflect.Method;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * 限流处理
 *
 * @author Lion Li
 */
@Aspect
@Log4j2
public class RateLimiterAspect {

    // 定义一个常量字符串代替 GlobalConstants.RATE_LIMIT_KEY，解决找不到变量的问题
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    /**
     * 定义spel表达式解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();
    /**
     * 定义spel解析模版
     */
    private final ParserContext parserContext = new TemplateParserContext();
    /**
     * 方法参数解析器
     */
    private final ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();

    @Autowired
    RedisRateLimiter redisRateLimiter;

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
        int time = rateLimiter.time();
        int count = rateLimiter.count();
        String combineKey = getCombineKey(rateLimiter, point);
        long number = this.redisRateLimiter.tryAcquire(combineKey, count, time);
        if (number <= -1) {
            throw new ServiceException(rateLimiter.errCode());
        }
        log.info("限制令牌 => {}, 剩余令牌 => {}, 缓存key => '{}'", count, number, combineKey);
    }

    @Autowired
    BeanFactory beanFactory;
    /**
     * 使用建议：
     * 在注解中直接写SPEL表达式，无需手动添加模板标记
     *
     * @param rateLimiter
     * @param point
     * @return
     * @RateLimiter(key = "user_#userId", time = 10, count = 5)
     * 需要引用Bean时：
     * @RateLimiter(key = "@configService.getLimitKey()", ...)
     * 使用静态常量：
     * @RateLimiter(key = "T(com.example.Constants).LIMIT_KEY", ...)
     */
    private String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
        String key = rateLimiter.key();

        // 判断 key 是否为表达式
        if (StringUtils.isNotBlank(key) && (key.contains("#") || key.contains("T("))) {
            key = evaluateExpression(key, point);
        }
        // point.getSignature().getName() 获取的是方法名称
        String methodName = point.getSignature().getName(); // 这是方法名
        return RATE_LIMIT_KEY_PREFIX +
                methodName +
                ":" +
                key;
    }

    private String evaluateExpression(String expressionKey, JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] args = point.getArgs();

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(null, targetMethod, args, pnd);
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));

        Expression expression;
        if (StringUtils.startsWith(expressionKey, parserContext.getExpressionPrefix())
                && StringUtils.endsWith(expressionKey, parserContext.getExpressionSuffix())) {
            expression = parser.parseExpression(expressionKey, parserContext);
        } else {
            expression = parser.parseExpression(expressionKey);
        }

        return expression.getValue(context, String.class);
    }
}
