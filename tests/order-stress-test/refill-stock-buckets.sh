#!/usr/bin/env bash
# 用法：./tests/order-stress-test/refill-stock-buckets.sh
# 依赖：docker（mysql 容器 singularity-mysql）、curl、网关 8080 可访问
set -euo pipefail
QTY="${1:-99999999}"
MYSQL_C="${MYSQL_CONTAINER:-singularity-mysql}"
BASE="${STOCK_BASE_URL:-http://localhost:8080}"

echo "[1/4] MySQL singularity_product PROD_001 / PROD_002 (price=99, on-shelf)"
docker exec "${MYSQL_C}" mysql -uroot -proot -e "
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
"

echo "[2/4] Clear product detail cache and restart product containers"
REDIS_C="${REDIS_CONTAINER:-singularity-redis}"
docker exec "${REDIS_C}" redis-cli DEL \
  product:detail:PROD_001 product:detail:PROD_002 \
  product:s0:detail:PROD_001 product:s0:detail:PROD_002 \
  product:s1:detail:PROD_001 product:s1:detail:PROD_002 \
  product:s2:detail:PROD_001 product:s2:detail:PROD_002 \
  product:s3:detail:PROD_001 product:s3:detail:PROD_002 \
  >/dev/null || true
while IFS= read -r cid; do
  [ -n "${cid}" ] && docker restart "${cid}" >/dev/null
done < <(docker ps -q --filter "name=singularity-singularity-product")

echo "[3/4] MySQL singularity_stock PROD_001 / PROD_002 -> ${QTY}"
docker exec "${MYSQL_C}" mysql -uroot -proot -e "
USE singularity_stock;
INSERT INTO stock (product_id, available_quantity, reserved_quantity, total_quantity, version)
VALUES
  ('PROD_001', ${QTY}, 0, ${QTY}, 0),
  ('PROD_002', ${QTY}, 0, ${QTY}, 0)
ON DUPLICATE KEY UPDATE
  available_quantity = VALUES(available_quantity),
  reserved_quantity = 0,
  total_quantity = VALUES(total_quantity);
"

echo "[4/4] Redis preheat via ${BASE}/api/stock/slots/preheat"
curl -fsS -X POST "${BASE}/api/stock/slots/preheat" \
  -H "Content-Type: application/json" \
  -d "{\"slotId\":\"bucket-1\",\"redisKey\":\"stock:bucket-1\",\"quantity\":${QTY},\"overwrite\":true}"
echo
curl -fsS -X POST "${BASE}/api/stock/slots/preheat" \
  -H "Content-Type: application/json" \
  -d "{\"slotId\":\"bucket-2\",\"redisKey\":\"stock:bucket-2\",\"quantity\":${QTY},\"overwrite\":true}"
echo
echo "Done."
