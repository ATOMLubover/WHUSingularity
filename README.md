# WHUSingularity

高并发抢单系统框架 + Spring Cloud微服务实践

## 项目结构

```
WHUSingularity/
├── singularity-core/      # 核心高并发框架
├── singularity-eureka/    # Eureka服务注册中心 (端口: 8761)
├── singularity-user/      # 用户服务 - 登录注册 (端口: 8082)
└── singularity-order/     # 订单服务 - 高并发抢单 (端口: 8081)
```

## 技术栈

- **框架**: Spring Boot 4.0.3 + Spring Cloud 2025.0.0
- **服务发现**: Netflix Eureka
- **服务调用**: OpenFeign
- **消息队列**: RocketMQ
- **缓存**: Redis
- **数据库**: MySQL
- **ORM**: MyBatis

## 启动顺序

1. 启动 singularity-eureka (Eureka注册中心)
2. 启动 singularity-user (用户服务)
3. 启动 singularity-order (订单服务)

## 用户服务API

### 注册
```
POST /api/user/register
Content-Type: application/json
{
  "username": "test",
  "password": "123456",
  "nickname": "测试用户"
}
```

### 登录
```
POST /api/user/login
Content-Type: application/json
{
  "username": "test",
  "password": "123456"
}
```

### 查询用户
```
GET /api/user/{id}
```

## Spring Cloud接入要点

### 1. 服务注册与发现
- 启动类添加 `@EnableDiscoveryClient`
- 配置文件中添加Eureka服务地址

### 2. 服务间调用
- 使用 `@FeignClient` 注解声明客户端
- 启动类添加 `@EnableFeignClients`
- 注入FeignClient直接调用其他服务

### 3. 应用命名
- 在 application.yml 中配置 `spring.application.name`
