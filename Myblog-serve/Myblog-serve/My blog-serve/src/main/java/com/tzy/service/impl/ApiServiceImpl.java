package com.tzy.service.impl;

import com.tzy.mapper.ApiMapper;
import com.tzy.pojo.FreeInterface;
import com.tzy.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiServiceImpl implements ApiService {
    @Autowired
    private ApiMapper apiMapper;
    @Override
    public List<FreeInterface> getApiList() {
        List<FreeInterface> freeInterfaceList = apiMapper.getApiList();
        if (freeInterfaceList != null && !freeInterfaceList.isEmpty()) {
            return freeInterfaceList;
        }
        return freeInterfaceList;
    }

    @Override
    public boolean addLink(String name, String link) {
        Boolean result = apiMapper.addLink(name, link);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean deleteLink(String id) {
        Boolean result = apiMapper.deleteLink(id);
        return Boolean.TRUE.equals(result);
    }

}
