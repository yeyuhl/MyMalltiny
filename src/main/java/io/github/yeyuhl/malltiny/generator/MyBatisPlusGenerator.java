package io.github.yeyuhl.malltiny.generator;


import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.LikeTable;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;
import java.util.Scanner;

/**
 * MyBatisPlus代码生成器
 * 由于是一次性使用的代码，我感觉可以放到test里面
 * 这样既可以正常使用代码，也不会影响到项目架构组成
 *
 * @author yeyuhl
 * @date 2023/4/24
 */
public class MyBatisPlusGenerator {
    public static void main(String[] args) {
        // System.getProperty("user.dir");返回的是当前JVM当前的工作目录
        String projectPath = System.getProperty("user.dir");
        String moduleName = scanner("模块名");
        String[] tableNames = scanner("表名，多个英文逗号分割").split(",");
        // 代码生成器
        AutoGenerator autoGenerator = new AutoGenerator(initDataSourceConfig());
        autoGenerator.global(initGlobalConfig(projectPath));
        autoGenerator.packageInfo(initPackageConfig(projectPath, moduleName));
        autoGenerator.injection(initInjectionConfig(projectPath, moduleName));
        autoGenerator.template(initTemplateConfig());
        autoGenerator.strategy(initStrategyConfig(tableNames));
        autoGenerator.execute(new VelocityTemplateEngine());
    }

    /**
     * 读取控制台内容信息
     */
    private static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(("请输入" + tip + "："));
        if (scanner.hasNext()) {
            String next = scanner.next();
            if (StrUtil.isNotEmpty(next)) {
                return next;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    /**
     * 初始化全局配置
     */
    private static GlobalConfig initGlobalConfig(String projectPath) {
        return new GlobalConfig.Builder()
                // 指定输出目录
                .outputDir(projectPath + "/src/main/java")
                .author("yeyuhl")
                // 禁止打开输出目录
                .disableOpenDir()
                .enableSwagger()
                .fileOverride()
                // 时间策略
                .dateType(DateType.ONLY_DATE)
                .build();
    }

    /**
     * 初始化数据源配置
     */
    private static DataSourceConfig initDataSourceConfig() {
        // 读取generator.properties，然后再获取其中的设置
        Props props = new Props("generator.properties");
        String url = props.getStr("dataSource.url");
        String username = props.getStr("dataSource.username");
        String password = props.getStr("dataSource.password");
        return new DataSourceConfig.Builder(url, username, password)
                // 设置数据库查询
                .dbQuery(new MySqlQuery())
                .build();
    }

    /**
     * 初始化包配置
     */
    private static PackageConfig initPackageConfig(String projectPath, String moduleName) {
        Props props = new Props("generator.properties");
        return new PackageConfig.Builder()
                // 父包模块名
                .moduleName(moduleName)
                // 父包名
                .parent(props.getStr("package.base"))
                // Entity包名
                .entity("model")
                // 路径配置信息
                .pathInfo(Collections.singletonMap(OutputFile.mapperXml, projectPath + "/src/main/resources/mapper" + moduleName))
                .build();
    }

    /**
     * 初始化模版配置
     */
    private static TemplateConfig initTemplateConfig() {
        return new TemplateConfig.Builder().build();
    }

    /**
     * 初始化策略配置
     */
    private static StrategyConfig initStrategyConfig(String[] tableNames) {
        StrategyConfig.Builder builder = new StrategyConfig.Builder();
        builder.entityBuilder()
                .naming(NamingStrategy.underline_to_camel)
                .columnNaming(NamingStrategy.underline_to_camel)
                .enableLombok()
                .formatFileName("%s")
                .mapperBuilder()
                .enableBaseResultMap()
                .formatMapperFileName("%sMapper")
                .formatXmlFileName("%sMapper")
                .serviceBuilder()
                .formatServiceFileName("%sService")
                .formatServiceImplFileName("%sServiceImpl")
                .controllerBuilder()
                .enableRestStyle()
                .formatFileName("%sController");
        // 当表名中带*号时可以启用通配符模式
        if (tableNames.length == 1 && tableNames[0].contains("*")) {
            String[] likeStr = tableNames[0].split("_");
            String likePrefix = likeStr[0] + "_";
            // 模糊表匹配(sql过滤)
            builder.likeTable(new LikeTable(likePrefix));
        } else {
            // 增加表匹配(内存过滤)
            builder.addInclude(tableNames);
        }
        return builder.build();
    }

    /**
     * 初始化注入配置
     */
    private static InjectionConfig initInjectionConfig(String projectPath, String moduleName) {
        return new InjectionConfig.Builder().build();
    }
}
