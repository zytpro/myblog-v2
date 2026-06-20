package com.tzy.service;

import com.tzy.pojo.Navbar;
import java.util.List;

public interface NavbarService {
    List<Navbar> getAllNavbars();
    Navbar getNavbarByName(String name);
    boolean addNavbar(Navbar navbar);
    boolean updateNavbar(Navbar navbar);
    boolean deleteNavbar(String name);
} 