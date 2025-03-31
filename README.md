# r-chat-back



## 项目简介

R-Chat是一款跨平台的即时通讯工具，支持单人、多人参与，可以通过网络发送图片、视频和文字等。前端项目地址：https://gitee.com/CodexploRe/r-chat-front



## 开发环境

| 组件  | 版本要求 |
| ----- | -------- |
| JDK   | 8+       |
| MySQL | 8.0+     |
| Redis | 6.2+     |
| Maven | 3.6+     |



## 技术选型

| 技术栈                 | 版本         | 说明                                           |
| ---------------------- | ------------ | ---------------------------------------------- |
| Spring Boot            | 2.6.1        | 核心框架，简化Spring应用开发流程               |
| Spring Boot Validation | 2.6.1        | 参数校验组件，通过注解实现请求参数的合法性校验 |
| Spring Boot Redis      | 2.6.1        | 集成Redis客户端                                |
| Netty                  | 4.1.50.Final | 高性能NIO框架，支持高并发长连接                |
| Mybatis Plus           | 3.5.3.1      | ORM增强工具，简化数据库操作                    |
| Druid                  | 1.2.1        | 数据库连接池                                   |
| Redisson               | 3.12.3       | Redis分布式服务框架，支持订阅发布              |
| Aspectjweaver          | 1.9.4        | AOP切面编程支持                                |
| Hutool                 | 5.8.11       | 工具库集合，减少重复代码                       |
| Fastjson               | 1.2.66       | 高性能JSON解析库                               |
| Okhttp3                | 3.2.0        | 轻量级HTTP客户端，支持连接池                   |
| Knife4j                | 2.0.8        | Swagger增强UI，提供API文档在线调试             |
| Lombok                 | 1.18.32      | 代码简化工具，提升编码效率                     |
| Captcha                | 1.6.2        | 图形验证码生成组件                             |
| Nashorn                | 15.4         | 验证码依赖引擎                                 |



## 项目结构

```text
├─java
│  └─com
│      └─r
│          └─chat
│              │  RChatApplication.java                 # 启动类
│              ├─config                                 # 配置类
│              │      MybatisConfig.java
│              │      RedisConfig.java
│              │      WebMvcConfig.java
│              ├─context                                # ThreadLocal上下文
│              │      AdminContext.java
│              │      UserTokenInfoContext.java
│              ├─controller
│              ├─entity
│              │  ├─constants
│              │  ├─dto
│              │  ├─enums
│              │  ├─notice                              # ws通信信息类
│              │  ├─po
│              │  ├─result                              # 后端统一返回结果
│              │  └─vo
│              ├─exception
│              ├─interceptor                            # 鉴权拦截器
│              │      AdminInterceptor.java
│              │      TokenInterceptor.java
│              ├─mapper
│              ├─properties
│              │      AppProperties.java                # 项目配置属性
│              │      DefaultSysSettingProperties.java  # 应用系统默认设置
│              ├─redis
│              │      RedisOperation.java
│              │      RedisUtils.java
│              ├─service
│              ├─utils
│              │      AvatarOwner.java
│              │      CastUtils.java
│              │      CollUtils.java
│              │      Convert.java
│              │      CopyUtils.java
│              │      FileUtils.java
│              │      JsonUtils.java
│              │      StringUtils.java
│              │      VerifyUtils.java
│              └─websocket
│                  │  NettyWebSocketStarter.java       # netty服务
│                  ├─handler
│                  │      HeartbeatHandler.java        # 心跳handler
│                  │      WebSocketHandler.java        # ws长连接handler
│                  └─utils
│                          ChannelUtils.java           # netty工具类
│                          URLUtils.java
└─resources
    │  application-dev.yaml
    │  application.yaml
    └─mapper
```


