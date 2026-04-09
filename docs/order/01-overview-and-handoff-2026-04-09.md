# order 模块概览与交接说明（2026-04-09）

## 1. 文档目的

1. 给 order 分支负责人快速说明当前模块可运行状态。
2. 明确本次跨分支协助改动的范围、目的和影响边界。
3. 给出后续联调优先级，避免重复排查启动问题。

## 2. 模块定位（当前仓库）

1. 模块路径：`singularity-order`。
2. 服务端口：`8081`。
3. 主要职责：承接秒杀下单流程，依赖 core 分配器抽象（Allocator/Registry/ShardPolicy）进行槽位分配，结合 RocketMQ 事务消息与 Redis/MySQL 完成订单链路。
4. 对外协作：通过 Feign 调用 user 服务查询用户信息。

## 3. 运行前置依赖

1. MySQL：`localhost:3306`（`singularity_order`）。
2. Redis：`localhost:6379`。
3. RocketMQ NameServer：`localhost:9876`。
4. Eureka：`localhost:8761`（order 启动后应完成注册）。

## 4. 当前已验证状态（最终回归后）

1. 仓库级构建：`mvn clean package` 成功。
2. 启动链路：`eureka -> user -> order` 已验证可启动。
3. 冒烟结果：
   - `GET http://localhost:8081/` 返回 `404`（根路径未映射，表示 Web 容器可用）。
   - order 实例已注册到 Eureka。

## 5. 本次跨分支协助改动（仅为恢复启动闭环）

### 5.1 代码改动

1. 新增配置文件：`singularity-order/src/main/java/com/lubover/singularity/order/config/AllocatorRuntimeConfig.java`。
2. 新增 `Registry` Bean：
   - 配置项：`order.alloc.slot-count`（默认 `16`）。
   - 行为：构建静态 slot 列表（`slot-0 ... slot-(n-1)`）。
3. 新增 `ShardPolicy` Bean：
   - actorId 为空时回退到第一个 slot。
   - actorId 非空时按 `hash(actorId) % slotSize` 进行槽位选择。
4. 增加内部 `StaticSlot` 实现：
   - `metadata.source=order-runtime`，用于标识来源。

### 5.2 改动目的

1. 消除 order 启动时 `Registry` 缺失导致的依赖注入失败。
2. 让 `DefaultAllocator` 在运行时拿到可用 `Registry/ShardPolicy`，恢复服务启动闭环。

### 5.3 影响边界

1. 本次改动不涉及 order 的业务接口语义调整。
2. 不改变 RocketMQ、Redis、MySQL 业务流程。
3. 不替代后续正式分片策略设计，仅提供可运行的基础装配实现。

## 6. 明确未覆盖项（由 order 负责人后续接续）

1. 在 order 抢单入口接入与 user 对齐的鉴权语义（401/403、身份注入）。
2. 完成跨服务 token 透传与 S10（冒充他人下单）端到端联调验收。
3. 视压测与容量需求，将当前静态 slot + hash 策略演进为可配置/可观测的正式分片策略。

## 7. 建议的最小验证清单（接手后）

1. 启动验证：按 `CLAUDE.md` 顺序启动三服务并确认 Eureka 注册。
2. 功能验证：补充至少一个 order 受保护接口的无 token/越权场景断言。
3. 回归验证：在 order 侧完成鉴权接入后执行一次仓库级 `mvn clean package` + 冒烟。

## 8. 交接结论

1. order 模块当前已从“启动阻断”恢复为“可启动可注册”状态。
2. 后续工作重心应从“启动修复”转移到“鉴权联调与业务安全闭环”。
