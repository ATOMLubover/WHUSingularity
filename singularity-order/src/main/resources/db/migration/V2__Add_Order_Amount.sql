ALTER TABLE `order`
    ADD COLUMN `amount` DECIMAL(15, 2) NULL COMMENT '订单金额（下单时商品单价快照）' AFTER `product_id`;
