-- 商户充值功能依赖 balance 列；已有库需手动执行一次（新库由 schema.sql 建表已含该列）
USE singularity_merchant;

ALTER TABLE merchant
    ADD COLUMN balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额' AFTER avatar;
