package com.ling.infrastructure.repository;

import com.ling.domain.user.adapter.IUserRepository;
import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.*;
import com.ling.infrastructure.dao.IUserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @Author: LingRJ
* @Description: 用户仓储实现（Infrastructure层）
* @DateTime: 2025/6/27 9:06
**/
@Repository
@Slf4j
public class UserRepositoryImpl implements IUserRepository {

    @Autowired
    private IUserDao userDao;

    @Override
    public boolean save(User user) {
        try {
            com.ling.infrastructure.dao.po.User userPO = convertToUserPO(user);
            return userDao.insertUser(userPO) > 0;
        } catch (Exception e) {
            log.error("保存用户失败: {} - {}", user.getUsername().getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        try {
            com.ling.infrastructure.dao.po.User userPO = userDao.findByUsernameOrEmail(username.getValue());
            return userPO != null ? Optional.of(convertToUser(userPO)) : Optional.empty();
        } catch (Exception e) {
            log.error("根据用户名查找用户失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        try {
            com.ling.infrastructure.dao.po.User userPO = userDao.findByUsernameOrEmail(email.getValue());
            return userPO != null ? Optional.of(convertToUser(userPO)) : Optional.empty();
        } catch (Exception e) {
            log.error("根据邮箱查找用户失败: {} - {}", email.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByPhoneNumber(PhoneNumber phoneNumber) {
        return Optional.empty();
    }

    @Override
    public boolean existsByUsername(Username username) {
        try {
            return userDao.existsByUsername(username.getValue());
        } catch (Exception e) {
            log.error("检查用户名是否存在失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsByEmail(Email email) {
        try {
            return findByEmail(email).isPresent();
        } catch (Exception e) {
            log.error("检查邮箱是否存在失败: {} - {}", email.getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
        return false;
    }

    @Override
    public boolean existsByEmailExcludeCurrentUser(Email email, Username currentUsername) {
        try {
            return userDao.existsByEmailExcludeCurrentUser(email.getValue(), currentUsername.getValue());
        } catch (Exception e) {
            log.error("检查邮箱是否存在（排除当前用户）失败: {} - {}", email.getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsByPhoneNumberExcludeCurrentUser(PhoneNumber phoneNumber, Username currentUsername) {
        try {
            return userDao.existsByPhoneNumberExcludeCurrentUser(phoneNumber.getValue(), currentUsername.getValue());
        } catch (Exception e) {
            log.error("检查手机号是否存在（排除当前用户）失败: {} - {}", phoneNumber.getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return List.of();
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        return List.of();
    }

    @Override
    public List<User> findAll(int page, int size) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public long countByRole(UserRole role) {
        return 0;
    }

    @Override
    public long countByStatus(UserStatus status) {
        return 0;
    }

    @Override
    public boolean softDelete(User user) {
        try {
            user.changeStatus(UserStatus.DISABLED, "软删除");
            return save(user);
        } catch (Exception e) {
            log.error("软删除用户失败: {} - {}", user.getUsername().getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean hardDelete(Username username) {
        return false;
    }

    /**
     * 将User聚合根转换为UserPO
     */
    private com.ling.infrastructure.dao.po.User convertToUserPO(User user) {
        com.ling.infrastructure.dao.po.User userPO = new com.ling.infrastructure.dao.po.User();
        userPO.setUsername(user.getUsername().getValue());
        userPO.setNickname(user.getNickname());
        userPO.setFullName(user.getFullName());
        userPO.setPassword(user.getPassword().getEncodedValue());
        userPO.setEmail(user.getEmail() != null ? user.getEmail().getValue() : null);
        userPO.setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber().getValue() : null);
        userPO.setAvatarUrl(user.getAvatarUrl());
        userPO.setTitle(user.getTitle());
        userPO.setRole(user.getRole().getCode());
        userPO.setStatus(user.getStatus().getCode());
        userPO.setCreateTime(convertToDate(user.getCreateTime()));
        userPO.setUpdateTime(convertToDate(user.getUpdateTime()));
        return userPO;
    }

    /**
     * 将UserPO转换为User聚合根
     */
    private User convertToUser(com.ling.infrastructure.dao.po.User userPO) {
        return User.builder()
                .username(Username.of(userPO.getUsername()))
                .nickname(userPO.getNickname())
                .fullName(userPO.getFullName())
                .password(Password.fromEncoded(userPO.getPassword()))
                .email(userPO.getEmail() != null ? Email.of(userPO.getEmail()) : null)
                .phoneNumber(userPO.getPhoneNumber() != null ? PhoneNumber.of(userPO.getPhoneNumber()) : null)
                .avatarUrl(userPO.getAvatarUrl())
                .title(userPO.getTitle())
                .role(UserRole.fromCode(userPO.getRole()))
                .status(UserStatus.fromCode(userPO.getStatus()))
                .createTime(convertToLocalDateTime(userPO.getCreateTime()))
                .updateTime(convertToLocalDateTime(userPO.getUpdateTime()))
                .build();
    }

    /**
     * LocalDateTime转Date
     */
    private Date convertToDate(LocalDateTime localDateTime) {
        return localDateTime != null ? 
                Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()) : 
                new Date();
    }

    /**
     * Date转LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date != null ? 
                date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : 
                LocalDateTime.now();
    }
}
