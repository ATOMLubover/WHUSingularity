package com.lubover.singularity.user.service.impl;

import com.lubover.singularity.user.entity.User;
import com.lubover.singularity.user.mapper.UserMapper;
import com.lubover.singularity.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    @Override
    public User register(String username, String password, String nickname) {
        User existUser = userMapper.selectByUsername(username);
        if (existUser != null) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User(username, password, nickname, "normal");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Password incorrect");
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
            user.setPassword(password);
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
            throw new RuntimeException("Recharge amount must be positive");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        BigDecimal newBalance = user.getBalance().add(amount);
        return userMapper.updateBalance(id, newBalance) > 0;
    }

    @Override
    @Transactional
    public boolean deduct(Long id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deduct amount must be positive");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        BigDecimal newBalance = user.getBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        return userMapper.updateBalance(id, newBalance) > 0;
    }
}
