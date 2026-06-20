package com.tzy.controller;

import com.tzy.dto.ResponseResult;
import com.tzy.pojo.Navbar;
import com.tzy.service.NavbarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/navbar")
public class NavbarController {

    @Autowired
    private NavbarService navbarService;

    @GetMapping("/list")
    public ResponseEntity<List<Navbar>> getAllNavbars() {
        List<Navbar> navbars = navbarService.getAllNavbars();
        return ResponseEntity.ok(navbars);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Navbar> getNavbarByName(@PathVariable String name) {
        Navbar navbar = navbarService.getNavbarByName(name);
        return navbar != null ? ResponseEntity.ok(navbar) : ResponseEntity.notFound().build();
    }

    @PostMapping("/add")
    public ResponseResult addNavbar(@RequestBody Navbar navbar) {
        return navbarService.addNavbar(navbar)
            ? ResponseResult.success("导航栏添加成功")
            : ResponseResult.error("导航栏添加失败");
    }

    @PutMapping("/update")
    public ResponseResult updateNavbar(@RequestBody Navbar navbar) {
        return navbarService.updateNavbar(navbar)
            ? ResponseResult.success("导航栏更新成功")
            : ResponseResult.error("导航栏更新失败");
    }

    @DeleteMapping("/{name}")
    public ResponseResult deleteNavbar(@PathVariable String name) {
        return navbarService.deleteNavbar(name)
            ? ResponseResult.success("导航栏删除成功")
            : ResponseResult.error("导航栏删除失败");
    }
}
