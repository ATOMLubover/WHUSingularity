package com.lubover.singularity.order.mapper;

import com.lubover.singularity.order.entity.Stock;
import org.apache.ibatis.annotations.*;

/**
 * 库存数据访问层
 */
@Mapper
public interface StockMapper {

    @Insert("INSERT INTO stock_tbl (stock_id, product_id, slot_id, total_stock, available_stock, " +
            "locked_stock, sold_stock, status, create_time, update_time) " +
            "VALUES (#{stockId}, #{productId}, #{slotId}, #{totalStock}, #{availableStock}, " +
            "#{lockedStock}, #{soldStock}, #{status}, #{createTime}, #{updateTime})")
    int insert(Stock stock);

    @Select("SELECT id, stock_id, product_id, slot_id, total_stock, available_stock, locked_stock, " +
            "sold_stock, status, create_time, update_time, is_deleted, version " +
            "FROM stock_tbl WHERE stock_id = #{stockId} AND is_deleted = 0")
    Stock selectByStockId(String stockId);

    @Select("SELECT id, stock_id, product_id, slot_id, total_stock, available_stock, locked_stock, " +
            "sold_stock, status, create_time, update_time, is_deleted, version " +
            "FROM stock_tbl WHERE slot_id = #{slotId} AND is_deleted = 0")
    Stock selectBySlotId(String slotId);

    @Update("UPDATE stock_tbl SET available_stock = available_stock - #{quantity}, sold_stock = sold_stock + #{quantity}, " +
            "update_time = NOW(), version = version + 1 " +
            "WHERE stock_id = #{stockId} AND version = #{version} AND is_deleted = 0")
    int updateStockByVersion(Stock stock, Integer quantity);

    @Update("UPDATE stock_tbl SET " +
            "total_stock = #{totalStock}, available_stock = #{availableStock}, locked_stock = #{lockedStock}, " +
            "sold_stock = #{soldStock}, status = #{status}, update_time = NOW(), version = version + 1 " +
            "WHERE stock_id = #{stockId} AND version = #{version} AND is_deleted = 0")
    int updateByVersion(Stock stock);
}
