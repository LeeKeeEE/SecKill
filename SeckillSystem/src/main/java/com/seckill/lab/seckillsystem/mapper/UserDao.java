package com.seckill.lab.seckillsystem.mapper;

import com.seckill.lab.seckillsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
    User findById(long id);
}
