package com.lubover.singularity.user.service.impl;

import com.lubover.singularity.user.entity.User;
import com.lubover.singularity.user.exception.BusinessException;
import com.lubover.singularity.user.exception.ErrorCode;
import com.lubover.singularity.user.mapper.UserMapper;
import com.lubover.singularity.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(String username, String password, String nickname) {
        validateRegisterRequest(username, password, nickname);

        User existUser = userMapper.selectByUsername(username);
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USER_USERNAME_EXISTS);
        }

        User user = new User(username, passwordEncoder.encode(password), nickname, "normal");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            // Handle race condition under concurrent register requests.
            throw new BusinessException(ErrorCode.USER_USERNAME_EXISTS);
        }
        return user;
    }

    @Override
    public User login(String username, String password) {
        validateLoginRequest(username, password);

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
        }
        return user;
    }

    @Override
    public User getUserById(Long id) {
        String cacheKey = "user:id:" + id;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        
        User user = userMapper.selectById(id);
        if (user != null) {
            redisTemplate.opsForValue().set(cacheKey, user.getId().toString(), 30, TimeUnit.MINUTES);
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    @Override
    @Transactional
    public User updateUser(Long id, String password, String nickname, String role, BigDecimal balance) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (role != null) {
            user.setRole(role);
        }
        if (balance != null) {
            user.setBalance(balance);
        }

        userMapper.updateById(user);
        return userMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return false;
        }
        return userMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean recharge(Long id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM, "Recharge amount must be positive");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM, "User not found");
        }

        BigDecimal newBalance = user.getBalance().add(amount);
        return userMapper.updateBalance(id, newBalance) > 0;
    }

    @Override
    @Transactional
    public boolean deduct(Long id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM, "Deduct amount must be positive");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM, "User not found");
        }

        BigDecimal newBalance = user.getBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM, "Insufficient balance");
        }

        return userMapper.updateBalance(id, newBalance) > 0;
    }

    private void validateRegisterRequest(String username, String password, String nickname) {
        if (isBlank(username) || isBlank(password)) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM);
        }
        if (username.length() < 4 || username.length() > 32) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM);
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM);
        }
        if (password.length() < 8 || password.length() > 64) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM);
        }
        if (nickname != null && (nickname.isBlank() || nickname.length() > 32)) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM);
        }
    }

    private void validateLoginRequest(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new BusinessException(ErrorCode.REQ_INVALID_PARAM);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
