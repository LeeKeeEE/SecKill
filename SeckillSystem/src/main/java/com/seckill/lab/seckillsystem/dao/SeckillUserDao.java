//package com.seckill.lab.seckillsystem.dao;
//
//import com.seckill.lab.seckillsystem.entity.User;
//import org.apache.ibatis.annotations.Mapper;
//import org.apache.ibatis.annotations.Param;
//import org.apache.ibatis.annotations.Select;
//import org.apache.ibatis.annotations.Update;
//
//@Mapper
//public interface SeckillUserDao {
//    @Select("select * from user where phone=#{phone}")
//    User getById(@Param("phone") String phone);
//
//    @Update("update user set password=#{password} where phone=#{phone}")
//    void update(User updateUser);
//
//}
