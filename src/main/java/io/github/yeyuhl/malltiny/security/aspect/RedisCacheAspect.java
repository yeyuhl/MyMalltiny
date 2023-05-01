package io.github.yeyuhl.malltiny.security.aspect;

import io.github.yeyuhl.malltiny.security.annotation.CacheException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Redis缓存切面，防止Redis宕机影响正常业务逻辑
 * Order用于定义带注解的组件的排序顺序，越大优先级越高
 *
 * @author yeyuhl
 * @date 2023/4/27
 */
@Aspect
@Component
@Order(2)
public class RedisCacheAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheAspect.class);

    /**
     * 定义切入点
     * Pointcut表达式以切入点指示器（PCD）开头，它是一个关键字，告诉Spring AOP要匹配什么。
     * 比如execution匹配方法执行连接点
     * 其语法为execution(modifiers-pattern? ret-type-pattern declaring-type-pattern? name-pattern(param-pattern) throws-pattern?)
     * modifiers-pattern：方法修饰符，public、private等
     * ret-type-pattern：方法返回类型，用*表示任意类型
     * declaring-type-pattern：方法所在类的全限定名，用*匹配任意类
     * name-pattern：方法名称，可以使用通配符 * 匹配任意方法
     * param-pattern：方法参数列表，可以使用 (..) 匹配任意数量的参数
     * throws-pattern：方法抛出的异常类型
     * 下面的切入点表达式匹配 io.github.yeyuhl.malltiny.modules.ums.model.service.*CacheService 类中所有公共方法的执行
     */
    @Pointcut("execution(public * io.github.yeyuhl.malltiny.modules.ums.model.service.*CacheService.*(..))")
    public void cacheAspect() {

    }

    /**
     * Around注解通常用于在方法执行前后执行一些额外的操作，例如记录日志、性能监控、事务管理等。
     * 它可以让您在不修改目标方法代码的情况下，为目标方法添加额外的功能。
     * 环绕通知方法的第一个参数必须是ProceedingJoinPoint类型。
     * 环绕通知方法必须返回一个Object类型的值，该值将作为目标方法的返回值。
     * 在环绕通知方法中，您可以通过调用pjp.proceed()方法来执行目标方法。您还可以传递一个Object[]类型的参数给proceed()方法，该数组中的值将作为目标方法的参数。
     * 如果您希望目标方法抛出异常，则可以在环绕通知方法中抛出异常。
     */
    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        // 在Spring AOP中，Signature一个接口，它表示连接点的静态部分。例如，在方法调用的连接点，Signature就表示被调用的方法。
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable throwable) {
            //有CacheException注解的方法需要抛出异常
            if (method.isAnnotationPresent(CacheException.class)) {
                throw throwable;
            } else {
                LOGGER.error(throwable.getMessage());
            }
        }
        return result;
    }
}
