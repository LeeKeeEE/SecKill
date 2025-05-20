package com.seckill.lab.seckillsystem.controller;

import ch.qos.logback.classic.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import com.seckill.lab.seckillsystem.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    // 1. 显示注册页面（GET请求）
    @GetMapping("/register")
    public String registerPage() {
        return "register"; // 映射到 templates/register.html
    }


    // 2. 处理注册表单提交（POST请求）
    @PostMapping("/register")
    public String handleRegistration(@RequestParam("phone") String phone,
                                     @RequestParam("username") String username,
                                     @RequestParam("password") String password,
                                     Model model) {

        boolean isRegistered = userService.registerUser(phone, username, password);

        if (isRegistered) {
            return "redirect:/login/index";
        } else {
            model.addAttribute("errorMessage", "注册失败，手机或用户已存在。");
            return "register";
        }
    }
}

