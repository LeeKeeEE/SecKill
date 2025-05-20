package com.seckill.lab.seckillsystem.repository;

import com.seckill.lab.seckillsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// <User, Long>：User 是实体类名，Long 是主键类型
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByPhone(String phone);

}
