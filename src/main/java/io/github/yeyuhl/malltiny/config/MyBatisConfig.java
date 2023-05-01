package io.github.yeyuhl.malltiny.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis配置类
 *
 * @author yeyuhl
 * @date 2023/4/24
 */

@Configuration
@EnableTransactionManagement
@MapperScan({"io.github.yeyuhl.malltiny.modules.*.mapper"})
public class MyBatisConfig {
    /**
     * MybatisPlusInterceptor是Mybatis-Plus中的一个拦截器
     * 它可以用来添加各种内部拦截器，例如分页拦截器PaginationInnerInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 添加分页插件并且设置数据库类型
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return mybatisPlusInterceptor;
    }
}
