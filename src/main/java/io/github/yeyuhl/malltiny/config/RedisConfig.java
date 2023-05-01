package io.github.yeyuhl.malltiny.config;

import io.github.yeyuhl.malltiny.common.config.BaseRedisConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Redis配置类，此处没有什么需要额外配置的
 *
 * @author yeyuhl
 * @date 2023/4/24
 */
@EnableCaching
@Configuration
public class RedisConfig extends BaseRedisConfig {

}
