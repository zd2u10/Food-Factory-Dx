package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.User;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    int insert(User user);

    Optional<User> findById(@Param("userId") Long userId);

    Optional<User> findByUsername(@Param("username") String username);

    List<User> findAll();

    int updatePasswordHash(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);

    int updateActive(@Param("userId") Long userId, @Param("isActive") boolean isActive);
}
