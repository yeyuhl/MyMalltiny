# MyMalltiny
> 源项目：[mall-tiny](https://github.com/macrozheng/mall-tiny)

# 1. 项目功能

mall-tiny是一个小型项目，着重于mall项目的权限管理功能，即后台查看用户列表

# 2. 项目框架

```
src
├── common -- 用于存放通用代码
| ├── response -- 通用结果集封装类
| ├── config -- 通用配置类
| ├── domain -- 通用封装对象
| ├── exception -- 全局异常处理相关类
| └── service -- 通用业务类
├── config -- SpringBoot中的Java配置
├── domain -- 共用封装对象
├── generator -- MyBatis-Plus代码生成器
├── modules -- 存放业务代码的基础包
| └── ums -- 权限管理模块业务代码
| ├── controller -- 该模块相关接口
| ├── dto -- 该模块数据传输封装对象
| ├── mapper -- 该模块相关Mapper接口
| ├── model -- 该模块相关实体类
| └── service -- 该模块相关业务处理类
└── security -- SpringSecurity认证授权相关代码
 ├── annotation -- 相关注解
 ├── aspect -- 相关切面
 ├── component -- 认证授权相关组件
 ├── config -- 相关配置
 └── util -- 相关工具类
```

# 3. 代码实现

### common

首先是response包内的各种类，一共四个类，IErrorCode和ResultCode与API的响应码有关，而CommonPage和CommonResult则是对返回的数据进行处理。

然后针对特定的异常进行处理，对于不同的参数解析方式，Spring 抛出的异常也不同。由于设计原因，跟参数相关的异常主要有三个需要手动处理。

- org.springframework.validation.**BindException**
- org.springframework.web.bind.**MethodArgumentNotValidException**
- javax.validation.**ConstraintViolationException**

此外加上自定义的Api异常也需要手动处理。

接着是进行redis和swagger的config进行配置，在service包下编写redis相关操作的代码，使用opsForValue()实现redis的一些操作。

## config

注解添加@Configuration，配置好各个组件。先配置全局跨域处理，然后设置mybatis，redis，swagger的配置。最后由于MallSecurityConfig涉及一些业务，所以要放到后面再来写。

## 数据库建表

![](https://camo.githubusercontent.com/9dd4659f40b9d5e48d6528464db60677c23b0382274e6007231a6790ac4cdc16/687474703a2f2f696d672e6d6163726f7a68656e672e636f6d2f6d616c6c2f70726f6a6563742f6d616c6c5f74696e795f73746172745f30312e706e67)

## generator

根据数据库的数据和自己编写的模板，使用mybatis-plus-generator来一键生成对应数据的entity和mapper。后期根据自己的业务需求，对相应的mapper定义方法，并在xml中对其实现。使用MyBatisPlusGenerator类后输入要放入的包名以及要生成的表的名字，即:

ums_admin,ums_admin_login_log,ums_admin_role_relation,ums_menu,ums_resource,ums_resource_category,ums_role,ums_role_menu_relation,ums_role_resource_relation

## security

由于我们涉及到登录操作，因此需要考虑其安全性。先编写security包下的util包内的工具类，一共有两个，一个是关于JwtToken的工具类，一个是关于Spring的工具类。然后考虑annotation，自定义注解来解决缓存异常。再编写asepect里关于redis缓存切面，防止redis宕机影响正常业务逻辑（简单方法就是监控目标服务，然后出现异常时抛出）。再考虑component，完成Security的各个部件，比如过滤器和权限管理器，这部分核心目的是验证用户权限。最后就是来完成config，包括配置资源白名单（其实在上一步就配置，验证需要放行白名单）和SpringSecurity的配置。

## ums

现在我们的ums包内已经一键生成了四个包及其里面的代码，但是里面的代码大多都是空壳，我们还需要手动编写。值得注意的是，一键生成的mapper和service记得自己加上相应的注解，使Spring能查找到该bean。首先编写dto包，封装数据传输对象。接着编写service包，将相关业务处理类完成。这一中先实现各个service的接口，再实现具体实现类。注意中间涉及到domain包里的AdminUserDetails类，这个类继承UserDetails，自己来包装用户详细信息。最后，就是编写controller类，来实现模块的接口，与前端对接。

## 最后

记得config还未完成的MallSecurityConfig，其中一个关键就是重写DynamicSecurityService中的loadDataSource()，不然运行程序会报错。

# 4.调试运行

进入[Swagger UI](http://localhost:8080/swagger-ui)，查看是否正常运行。

![](https://yeyu-1313730906.cos.ap-guangzhou.myqcloud.com/PicGo/20230501145026.png)

在Sql文件中已经预设了测试用的帐号密码，在登录接口登录并获取token。

![](https://yeyu-1313730906.cos.ap-guangzhou.myqcloud.com/PicGo/20230501145317.png)

由于tokenHead设置为'Bearer '，因此在验证时记得还要输入tokenHead。登录成功后就可以调试各个接口，查看功能是否正常。
