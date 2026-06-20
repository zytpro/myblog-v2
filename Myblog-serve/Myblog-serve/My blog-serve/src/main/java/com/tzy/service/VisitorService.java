package com.tzy.service;

import com.tzy.mapper.VisitorMapper;
import com.tzy.pojo.Visitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class VisitorService {
    @Autowired
    private VisitorMapper visitorMapper;

    public void updateUniqueVisitors(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        try {
            // 直接尝试插入，Mapper 使用 ON DUPLICATE KEY 保证幂等
            visitorMapper.insertVisitor(ip);
        } catch (Exception ignore) {
            // 兜底：并发极端情况下的重复异常不影响主流程
        }
    }

    public List<Map<String, Object>> getUniqueVisitors() {
        return visitorMapper.countUniqueVisitorsByDate();
    }
}
