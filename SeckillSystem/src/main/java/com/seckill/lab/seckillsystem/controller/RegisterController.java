package com.seckill.lab.seckillsystem.controller;


import com.seckill.lab.seckillsystem.result.Result;
import com.seckill.lab.seckillsystem.result.ResultCode;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import com.seckill.lab.seckillsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

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

    // 3. 处理API注册表单提交（POST请求，返回JSON）
    @PostMapping(value = "api/register", produces = "application/json")
    @ResponseBody // 添加此注解使方法返回JSON
    public ResponseEntity<Result<Boolean>> handleApiRegistration(@RequestParam("phone") String phone,
                                                                 @RequestParam("username") String username,
                                                                 @RequestParam("password") String password) {

        logger.info("Accept api request: " + phone + " " + username + " " + password);

        boolean isRegistered = userService.registerUser(phone, username, password);

        if (isRegistered) {
            return ResponseEntity.ok(Result.success(true));
        } else {
            logger.info("Failed to register");
            Result<Boolean> errorResult = Result.error(ResultCode.REGISTER_ERROR.fillArgs("注册失败，手机或用户已存在。"));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResult);
        }
    }
}

