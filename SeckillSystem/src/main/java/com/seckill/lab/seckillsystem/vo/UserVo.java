package com.seckill.lab.seckillsystem.vo;

import com.seckill.lab.seckillsystem.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class UserVo {
    private Long id;

    private String username;

    private String phone;

    public UserVo(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.phone = user.getPhone();
    }
}
