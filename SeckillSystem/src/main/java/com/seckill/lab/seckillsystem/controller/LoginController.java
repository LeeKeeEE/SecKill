package com.seckill.lab.seckillsystem.controller;

import ch.qos.logback.classic.Logger;
import com.seckill.lab.seckillsystem.result.ResultCode;
import com.seckill.lab.seckillsystem.result.Result;
import com.seckill.lab.seckillsystem.service.SeckillUserService;
import com.seckill.lab.seckillsystem.vo.LoginVo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequestMapping("/login")
@Controller
public class LoginController {

    @Autowired
    private SeckillUserService seckillUserService;

    private final Logger logger = (Logger) LoggerFactory.getLogger(Logger.class);

    @RequestMapping("/index")
    public String toLogin() {
        // 返回页面login
        logger.info("tiao zhuan login ...");
        return "login";
    }

    @RequestMapping("/token_test")
    @ResponseBody
    public Result<String> doLoginTest(@RequestParam("phone") String phone,
                                      @RequestParam("password") String password){
        LoginVo loginVo = new LoginVo();
        loginVo.setPhone(phone);
        loginVo.setPassword(password);

        String token = seckillUserService.loginTest(loginVo);

        logger.info("[LoginController] loginTest result: {}", token);

        return Result.success(token);
    }

    @PostMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(@RequestParam("phone") String phone,
                                   @RequestParam("password") String password)
     {

         LoginVo loginVo = new LoginVo();
         loginVo.setPhone(phone);
         loginVo.setPassword(password);

        ResultCode resultCode = seckillUserService.login(loginVo);

        logger.info("[LoginController] login result: {}", resultCode);

        if (resultCode.getCode() == 0) {
            return Result.success(true);
        } else {
            return Result.error(resultCode);
        }
    }
}
