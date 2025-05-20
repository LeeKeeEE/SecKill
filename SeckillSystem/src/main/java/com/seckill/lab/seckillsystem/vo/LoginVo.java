package com.seckill.lab.seckillsystem.vo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.seckill.lab.seckillsystem.util.IsMobile;

public class LoginVo {
    private String phone;
    private String password;

    @NotNull
    @IsMobile
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    @NotNull
    @Length(min=32)
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }


}
