package com.tzy.service.impl;

import com.tzy.mapper.NavbarMapper;
import com.tzy.pojo.Navbar;
import com.tzy.service.NavbarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NavbarServiceImpl implements NavbarService {
    
    @Autowired
    private NavbarMapper navbarMapper;
    
    @Override
    public List<Navbar> getAllNavbars() {
        return navbarMapper.findAll();
    }
    
    @Override
    public Navbar getNavbarByName(String name) {
        return navbarMapper.findByName(name);
    }
    
    @Override
    public boolean addNavbar(Navbar navbar) {
        return navbarMapper.insert(navbar) > 0;
    }
    
    @Override
    public boolean updateNavbar(Navbar navbar) {
        return navbarMapper.update(navbar) > 0;
    }
    
    @Override
    public boolean deleteNavbar(String name) {
        return navbarMapper.delete(name) > 0;
    }
} 