package com.seckill.lab.seckillsystem.service;

import com.seckill.lab.seckillsystem.controller.RegisterController;
import com.seckill.lab.seckillsystem.entity.User;
import com.seckill.lab.seckillsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seckill.lab.seckillsystem.util.MD5Util;

import java.util.Date;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserRepository userRepository;

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public boolean registerUser(String phone,String username, String password) {
        User existingUser = userRepository.findByPhone(phone);

        if (userRepository.findByUsername(username) != null) {
            logger.info("Username " + username + " already exists");
            return false; // 用户名已存在
        }

        if (existingUser != null) {
            logger.info("Phone number " + phone + " already exists");
            return false; // 已存在手机号，不能注册
        }

        if (phone!=null && username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            User newUser = new User();
            newUser.setPhone(phone);
            newUser.setUsername(username);
            newUser.setRegisterDate(new Date());
            // 生成一个盐
            String salt = "1a2b3c4d"; // 如果希望每个用户的盐是随机的，可以使用 UUID 或 SecureRandom 生成
            // 使用盐进行两次加密
            String encryptedPassword = MD5Util.inputPassToDbPass(password, salt);
            // 存储用户对象
            newUser.setPassword(encryptedPassword);
            newUser.setSalt(salt);
            userRepository.save(newUser);
            logger.info("New User " + newUser.toString());
            return true;
        }
        logger.info("Failed to save user");
        return false;
    }

}