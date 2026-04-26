# singularity-product 后端边界说明

## 1. 模块定位

`singularity-product` 负责商品主数据管理，是独立微服务。

## 2. 当前职责（Phase 0）

- 提供独立启动入口与服务注册能力（Nacos Discovery）。
- 提供基础连通性接口：`GET /api/product/ping`。
- 对齐工程基线：Spring Boot + MyBatis XML + Flyway + Nacos 配置中心。

## 3. 不在当前阶段职责内

- 不处理库存扣减与回补（由 `singularity-stock` 负责）。
- 不处理下单事务（由 `singularity-order` 负责）。
- 不包含商品业务 CRUD（在 Phase 2 实现）。

## 4. 与其他服务交互约束

- 与 `singularity-stock`：Phase 5 做库存视图聚合（读，OpenFeign）。
- 与 `singularity-order`：后续提供商品信息查询能力，不承载订单状态流转。
- 与 `singularity-user`：不直接依赖用户账户逻辑。

## 5. 运行与配置约定

- 服务名：`singularity-product`
- 默认端口：`8087`
- 数据库：`singularity_product`
- Flyway 目录：`classpath:db/migration`
- Mapper 目录：`classpath:mapper/*.xml`

## 6. Phase 1 完成状态

- 已完成商品主表结构与索引设计落地。
- 已完成 Flyway 迁移版本：`V1__Init_Product_Module.sql`、`V2__Create_Product_Tables.sql`。

## 7. Phase 2 API 契约（已实现）

- `POST /api/product`：新增商品
- `GET /api/product/{productId}`：查询商品详情
- `PUT /api/product/{productId}`：更新商品
- `DELETE /api/product/{productId}`：逻辑删除商品
- `GET /api/product/list`：分页查询（支持 `status/category/keyword/pageNo/pageSize`）

统一响应格式：

- 成功：`{ "success": true, "data": ... }`
- 失败：`{ "success": false, "error": { "code": "...", "message": "..." } }`

错误码（Phase 2）：

- `REQ_INVALID_PARAM`
- `PRODUCT_NOT_FOUND`
- `PRODUCT_ALREADY_EXISTS`
- `INTERNAL_ERROR`

## 8. 验证基线

- 通过 Docker Compose 启动 `singularity-product-0`。
- 通过接口验证新增、查询、更新、删除、分页查询。
- 通过数据库回查确认逻辑删除与分页过滤行为正确。

## 9. Phase 3 缓存体系（已实现）

### 架构

Caffeine 本地缓存 + Redis 远程缓存，Cache-Aside 模式。

```
读路径：本地 Caffeine → Redis → DB（回填两级）
写路径：DB 写完后删除 detail 缓存 + 批量失效 list 缓存
```

### 缓存 Key 规范

| 类型 | Key 格式 |
|---|---|
| 商品详情 | `product:detail:{productId}` |
| 商品列表 | `product:list:{queryHash}`（queryHash 为查询参数 hashCode） |

### TTL 配置

| 缓存层 | detail TTL | list TTL | 空值 TTL（防穿透） |
|---|---|---|---|
| Caffeine | 5 分钟（全局写后过期） | 同上 | 同上 |
| Redis | 30 分钟 | 10 分钟 | 60 秒 |

### 穿透保护

- 查询 DB 无结果时，缓存空标记 `__NULL__`，TTL 60s，避免恶意穿透。

### 交付物

- `config/CacheConfig.java`：Caffeine Bean、RedisTemplate Bean、ObjectMapper Bean
- `cache/ProductCacheService.java`：两级缓存读写/失效方法
- `service/impl/ProductServiceImpl.java`：所有读写路径均接入缓存

### 验收标准

- 连续两次 GET 相同 productId，第二次命中本地缓存（日志可见 `cache hit [local]`）。
- 更新/删除商品后，下一次 GET 命中 DB 而非缓存（日志无 `cache hit`）。
- 查询 DB 不存在的 productId，返回 `PRODUCT_NOT_FOUND`；再次查询不打 DB（空值缓存命中）。
