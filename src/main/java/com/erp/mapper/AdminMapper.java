package com.erp.mapper;

import com.erp.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 管理员 Mapper，SQL 写在 resources/mapper/AdminMapper.xml。
 */
@Mapper
public interface AdminMapper {

    /** 按登录账号查管理员（只取未删除的），查不到返回 null */
    Admin findByUsername(@Param("username") String username);

    /** 更新密码（明文账号首次登录后升级为 BCrypt 时用） */
    int updatePassword(@Param("id") Long id,
                       @Param("password") String password,
                       @Param("updateTime") LocalDateTime updateTime);
}
