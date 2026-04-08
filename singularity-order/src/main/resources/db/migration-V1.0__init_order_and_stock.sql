-- =====================================================
-- 初始化订单和库存表
-- =====================================================

-- 创建订单表 (order_tbl)
CREATE TABLE IF NOT EXISTS order_tbl (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id VARCHAR(64) NOT NULL UNIQUE COMMENT '订单ID',
    actor_id VARCHAR(64) COMMENT '参与者ID',
    slot_id VARCHAR(64) COMMENT '时间槽ID',
    status INT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待支付, 1-已支付, 2-已发货, 3-已收货, 4-已取消',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标志',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    KEY idx_order_id (order_id),
    KEY idx_actor_id (actor_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 创建库存表 (stock_tbl)
CREATE TABLE IF NOT EXISTS stock_tbl (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    stock_id VARCHAR(64) NOT NULL UNIQUE COMMENT '库存ID',
    product_id VARCHAR(64) NOT NULL COMMENT '产品ID',
    slot_id VARCHAR(64) NOT NULL COMMENT '时间槽ID',
    total_stock INT NOT NULL DEFAULT 0 COMMENT '总库存',
    available_stock INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    locked_stock INT NOT NULL DEFAULT 0 COMMENT '已锁定库存',
    sold_stock INT NOT NULL DEFAULT 0 COMMENT '已售库存',
    status INT NOT NULL DEFAULT 0 COMMENT '库存状态: 0-未开始, 1-进行中, 2-已结束',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标志',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    KEY idx_stock_id (stock_id),
    KEY idx_product_id (product_id),
    KEY idx_slot_id (slot_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- 创建库存变更日志表 (stock_change_log)
CREATE TABLE IF NOT EXISTS stock_change_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    stock_id VARCHAR(64) NOT NULL COMMENT '库存ID',
    order_id VARCHAR(64) COMMENT '订单ID',
    change_type INT NOT NULL COMMENT '变更类型: 1-初始化, 2-锁定, 3-解锁, 4-扣减, 5-退货',
    change_amount INT NOT NULL COMMENT '变更数量',
    before_available INT COMMENT '变更前可用库存',
    after_available INT COMMENT '变更后可用库存',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_stock_id (stock_id),
    KEY idx_order_id (order_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变更日志表';

-- 创建订单库存关联表 (order_stock_relation)
CREATE TABLE IF NOT EXISTS order_stock_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id VARCHAR(64) NOT NULL COMMENT '订单ID',
    stock_id VARCHAR(64) NOT NULL COMMENT '库存ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_order_id (order_id),
    KEY idx_stock_id (stock_id),
    UNIQUE KEY uniq_order_stock (order_id, stock_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单库存关联表';
