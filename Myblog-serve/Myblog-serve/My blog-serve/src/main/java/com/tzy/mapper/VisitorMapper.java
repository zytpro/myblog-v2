package com.tzy.mapper;

import com.tzy.pojo.Visitor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VisitorMapper {
    // 检查当天该 IP 是否已经访问
    @Select("SELECT COUNT(*) FROM visitor " +
        "WHERE ip_address = #{ipAddress} " +
        "AND DATE(visit_date) = CURRENT_DATE()")
    int countByIpAndDate(@Param("ipAddress") String ipAddress);

    // 插入新的访客记录（幂等：重复时不报错）
    @Insert("INSERT INTO visitor (ip_address, visit_date) " +
        "VALUES (#{ipAddress}, CURRENT_DATE()) " +
        "ON DUPLICATE KEY UPDATE visit_date = VALUES(visit_date)")
    void insertVisitor(@Param("ipAddress") String ipAddress);

    // 获取所有访客记录
    @Select("SELECT * FROM visitor")
    List<Map<String, Object>> countUniqueVisitorsByDate();
}
