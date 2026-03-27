# Singularity Core 接口使用指南

## 组织方式

- 调用 Slot.checkValid 函数，检查 slot 是否可用
- 调用 Actor.checkPermission 函数，检查 actor 是否有权抢占 slot
- 调用 Allocater.allocate 函数，将 slot 分配给 actor，这个过程应当是原子的
- 根据 allocate 的结果，决定是否正常的完成了抢占
