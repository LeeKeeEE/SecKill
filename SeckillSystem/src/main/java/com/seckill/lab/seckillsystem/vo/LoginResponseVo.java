package com.seckill.lab.seckillsystem.vo;

import com.seckill.lab.seckillsystem.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功后返回给前端的数据对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseVo {
    private String token;
    private User user;
}