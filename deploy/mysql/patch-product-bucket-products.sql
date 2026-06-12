-- 压测槽位商品 PROD_001 / PROD_002（与 order slot、stock 一致；可与 tests/order-stress-test/refill-stock-buckets.* 配合）
-- 执行：docker exec -i singularity-mysql mysql -uroot -proot < deploy/mysql/patch-product-bucket-products.sql

USE singularity_product;
INSERT INTO product (product_id, name, subtitle, main_image, category, tags, status, price, version, is_deleted)
VALUES
  ('PROD_001', 'Stress Bucket 1', 'order slot bucket-1', '', 'stress', 'stress,bucket-1', 1, 99.00, 0, 0),
  ('PROD_002', 'Stress Bucket 2', 'order slot bucket-2', '', 'stress', 'stress,bucket-2', 1, 99.00, 0, 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  subtitle = VALUES(subtitle),
  category = VALUES(category),
  tags = VALUES(tags),
  status = 1,
  price = VALUES(price),
  is_deleted = 0;
