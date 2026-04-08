package com.lubover.singularity.order.mapper;

import com.lubover.singularity.order.entity.StockChangeLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库存变更日志数据访问层
 */
@Mapper
public interface StockChangeLogMapper {

    @Insert("INSERT INTO stock_change_log (stock_id, order_id, change_type, change_amount, " +
            "before_available, after_available, create_time) " +
            "VALUES (#{stockId}, #{orderId}, #{changeType}, #{changeAmount}, " +
            "#{beforeAvailable}, #{afterAvailable}, #{createTime})")
    int insert(StockChangeLog log);

    @Select("SELECT id, stock_id, order_id, change_type, change_amount, before_available, " +
            "after_available, create_time " +
            "FROM stock_change_log WHERE stock_id = #{stockId} ORDER BY create_time DESC LIMIT #{limit}")
    List<StockChangeLog> selectByStockId(String stockId, int limit);

    @Select("SELECT id, stock_id, order_id, change_type, change_amount, before_available, " +
            "after_available, create_time " +
            "FROM stock_change_log WHERE order_id = #{orderId}")
    List<StockChangeLog> selectByOrderId(String orderId);
}
