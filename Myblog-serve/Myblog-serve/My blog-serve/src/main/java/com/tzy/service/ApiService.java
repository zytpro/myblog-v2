package com.tzy.service;

import com.tzy.pojo.FreeInterface;

import java.util.List;

public interface ApiService {
    //get all api list
    List<FreeInterface> getApiList();

    //add new link
    boolean addLink(String name, String link);

    //delete link by id
    boolean deleteLink(String id);
}
