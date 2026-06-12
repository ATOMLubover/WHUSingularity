# 压测前准备：PROD_001/PROD_002 写入 product 库、拉大 stock 库存，并对 Redis bucket 覆盖预热。
# 默认经网关 8080；若直连 stock 可设 -StockBaseUrl http://localhost:8082
param(
    [long]$Quantity = 1000000,
    [long]$RedisQuantity = 1000000,
    [string]$MysqlContainer = "singularity-mysql",
    [string]$StockBaseUrl = "http://localhost:8080"
)
$ErrorActionPreference = "Stop"

$productSql = "USE singularity_product; INSERT INTO product (product_id, name, subtitle, main_image, category, tags, status, price, version, is_deleted) VALUES ('PROD_001', 'Stress Bucket 1', 'order slot bucket-1', '', 'stress', 'stress,bucket-1', 1, 99.00, 0, 0), ('PROD_002', 'Stress Bucket 2', 'order slot bucket-2', '', 'stress', 'stress,bucket-2', 1, 99.00, 0, 0) ON DUPLICATE KEY UPDATE name = VALUES(name), subtitle = VALUES(subtitle), category = VALUES(category), tags = VALUES(tags), status = 1, price = VALUES(price), is_deleted = 0;"

$stockSql = "USE singularity_stock; INSERT INTO stock (product_id, available_quantity, reserved_quantity, total_quantity, version) VALUES ('PROD_001', $Quantity, 0, $Quantity, 0), ('PROD_002', $Quantity, 0, $Quantity, 0) ON DUPLICATE KEY UPDATE available_quantity = VALUES(available_quantity), reserved_quantity = 0, total_quantity = VALUES(total_quantity);"

Write-Host "[1/4] Upserting MySQL product rows PROD_001, PROD_002 (price=99, on-shelf)"
docker exec $MysqlContainer mysql -uroot -proot -e $productSql

Write-Host "[2/4] Clearing product detail cache and restarting product containers"
$redisContainer = if ($env:REDIS_CONTAINER) { $env:REDIS_CONTAINER } else { "singularity-redis" }
docker exec $redisContainer redis-cli DEL product:detail:PROD_001 product:detail:PROD_002 product:s0:detail:PROD_001 product:s0:detail:PROD_002 product:s1:detail:PROD_001 product:s1:detail:PROD_002 product:s2:detail:PROD_001 product:s2:detail:PROD_002 product:s3:detail:PROD_001 product:s3:detail:PROD_002 2>$null | Out-Null
docker ps -q --filter "name=singularity-singularity-product" | ForEach-Object { docker restart $_ | Out-Null }

Write-Host "[3/4] Updating MySQL stock rows PROD_001, PROD_002 -> $Quantity"
docker exec $MysqlContainer mysql -uroot -proot -e $stockSql

$body1 = @{ slotId = "bucket-1"; redisKey = "stock:bucket-1"; quantity = $RedisQuantity; overwrite = $true } | ConvertTo-Json
$body2 = @{ slotId = "bucket-2"; redisKey = "stock:bucket-2"; quantity = $RedisQuantity; overwrite = $true } | ConvertTo-Json

Write-Host "[4/4] Preheating Redis (qty=$RedisQuantity) via $StockBaseUrl/api/stock/slots/preheat"
Invoke-RestMethod -Method Post -Uri "$StockBaseUrl/api/stock/slots/preheat" -ContentType "application/json" -Body $body1 | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$StockBaseUrl/api/stock/slots/preheat" -ContentType "application/json" -Body $body2 | ConvertTo-Json

Write-Host "Done."
